package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.EmptyAccountActivity
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.activities.ReviewActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.ReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.ReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Review
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class ReviewFragment : Fragment(), ReviewAdapter.OnClickListener {
    // Primitive
    private var mIsAuth = false
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0
    private var mTotalLike = 0

    // Member
    private lateinit var mReviewList: ArrayList<Review>
    private lateinit var mAuth: Auth
    private lateinit var mContext: Context
    private lateinit var mReviewAdapter: ReviewAdapter
    private lateinit var mReviewRequest: ReviewRequest

    // Binding
    private var _mBinding: ReusableRecyclerBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Auth
        mAuth = Auth(mContext)
        mIsAuth = mAuth.isAuth()

        // Handler
        val handler = Handler()

        // Mendapatkan data awal
        mReviewRequest = ReviewRequest(mContext)
        getReview(mStartPage, false)

        // Activity
        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.loadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getReview(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getReview(mStartPage, true)
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
        resetState()
    }

    override fun onReviewItemClick(position: Int) {
        val currentItem = mReviewList[position]
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewDetail(currentItem.id, isSelf)
    }

    override fun onReviewUserProfileClick(position: Int) {
        val currentItem = mReviewList[position]
        gotoUserDetail(currentItem.userID)
    }

    override fun onReviewFilmPosterClick(position: Int) {
        val currentItem = mReviewList[position]
        gotoFilmDetail(currentItem.tmdbID)
    }

    override fun onReviewFilmPosterLongClick(position: Int) {
        val currentItem = mReviewList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.tmdbID, currentItem.title, year, currentItem.poster)
    }

    override fun onReviewLikeClick(position: Int) {
        when (mIsAuth) {
            true -> {
                val currentItem = mReviewList[position]
                mTotalLike = currentItem.totalLike
                if (!mIsLoading) {
                    mIsLoading = true
                    when (currentItem.isLiked) {
                        true -> unlikeReview(currentItem.id, position)
                        false -> likeReview(currentItem.id, position)
                    }
                }
            }
            false -> gotoEmptyAccount()
        }
    }

    override fun onReviewCommentClick(position: Int) {
        val currentItem = mReviewList[position]
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewComment(currentItem.id, isSelf)
    }

    private fun getReview(page: Int, refreshPage: Boolean) {
        mReviewRequest.sendRequest(page, object : ReviewRequest.Callback {
            override fun onSuccess(totalPage: Int, reviewList: ArrayList<Review>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mReviewList.clear()
                            mReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mReviewList.size
                        mReviewList.addAll(insertIndex, reviewList)
                        mReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mReviewAdapter.notifyItemRangeInserted(insertIndex, reviewList.size)
                    }
                    false -> {
                        mReviewList = ArrayList()
                        val insertIndex = mReviewList.size
                        mReviewList.addAll(insertIndex, reviewList)
                        setAdapter()
                        mBinding.progressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mBinding.errorMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mReviewList.clear()
                        mReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.progressBar.visibility = View.GONE
                }
                mBinding.errorMessage.visibility = View.VISIBLE
                mBinding.errorMessage.text = getString(R.string.empty_review)
            }

            override fun onError(message: String) {
                if (!mIsLoadFirstTimeSuccess) {
                    mBinding.progressBar.visibility = View.GONE
                    mBinding.errorMessage.visibility = View.VISIBLE
                    mBinding.errorMessage.text = getString(R.string.empty_review)
                }
                mBinding.loadMoreBar.visibility = View.GONE
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mBinding.swipeRefresh.isRefreshing = false
        mBinding.loadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun setAdapter() {
        mReviewAdapter = ReviewAdapter(mContext, mReviewList, this)
        mBinding.recyclerView.adapter = mReviewAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
        mBinding.recyclerView.visibility = View.VISIBLE
    }

    private fun resetState() {
        mIsLoading = true
        mIsLoadFirstTimeSuccess = false
        mCurrentPage = 1
    }

    private fun likeReview(id: Int, position: Int) {
        val likeReviewRequest = LikeReviewRequest(mContext, id)
        likeReviewRequest.sendRequest(object : LikeReviewRequest.Callback {
            override fun onSuccess() {
                mTotalLike++
                val currentItem = mReviewList[position]
                currentItem.isLiked = true
                currentItem.totalLike = mTotalLike
                mReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun unlikeReview(id: Int, position: Int) {
        val unlikeReviewRequest = UnlikeReviewRequest(mContext, id)
        unlikeReviewRequest.sendRequest(object : UnlikeReviewRequest.Callback {
            override fun onSuccess() {
                mTotalLike--
                val currentItem = mReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(requireFragmentManager(), Popularin.FILM_MODAL)
    }

    private fun gotoReviewDetail(id: Int, isSelf: Boolean) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, isSelf)
        startActivity(intent)
    }

    private fun gotoReviewComment(id: Int, isSelf: Boolean) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, isSelf)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, 1)
        startActivity(intent)
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}