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
import xyz.fairportstudios.popularin.activities.ReviewActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.LikedFilmReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class LikedReviewFragment(private val filmID: Int) : Fragment(), FilmReviewAdapter.OnClickListener {
    // Primitive
    private var mIsResumeFirstTime = true
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0
    private var mTotalLike: Int = 0

    // Member
    private lateinit var mFilmReviewList: ArrayList<FilmReview>
    private lateinit var mAuth: Auth
    private lateinit var mContext: Context
    private lateinit var mFilmReviewAdapter: FilmReviewAdapter
    private lateinit var mLikedFilmReviewRequest: LikedFilmReviewRequest

    // Binding
    private var _mBinding: ReusableRecyclerBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Auth
        mAuth = Auth(mContext)

        // Handler
        val handler = Handler()

        // Activity
        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.loadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getLikedFilmReview(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getLikedFilmReview(mStartPage, true)
        }

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data awal
            mIsResumeFirstTime = false
            mLikedFilmReviewRequest = LikedFilmReviewRequest(mContext, filmID)
            getLikedFilmReview(mStartPage, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    override fun onFilmReviewItemClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewDetail(currentItem.id, isSelf)
    }

    override fun onFilmReviewUserProfileClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        gotoUserDetail(currentItem.userID)
    }

    override fun onFilmReviewLikeClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        mTotalLike = currentItem.totalLike
        if (!mIsLoading) {
            mIsLoading = true
            when (currentItem.isLiked) {
                true -> unlikeReview(currentItem.id, position)
                false -> likeReview(currentItem.id, position)
            }
        }
    }

    override fun onFilmReviewCommentClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewComment(currentItem.id, isSelf)
    }

    private fun getLikedFilmReview(page: Int, refreshPage: Boolean) {
        mLikedFilmReviewRequest.sendRequest(page, object : LikedFilmReviewRequest.Callback {
            override fun onSuccess(totalPage: Int, filmReviewList: ArrayList<FilmReview>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mFilmReviewList.clear()
                            mFilmReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmReviewList.size
                        mFilmReviewList.addAll(insertIndex, filmReviewList)
                        mFilmReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mFilmReviewAdapter.notifyItemRangeInserted(insertIndex, filmReviewList.size)
                    }
                    false -> {
                        mFilmReviewList = ArrayList()
                        val insertIndex = mFilmReviewList.size
                        mFilmReviewList.addAll(insertIndex, filmReviewList)
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
                        mFilmReviewList.clear()
                        mFilmReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.progressBar.visibility = View.GONE
                }
                mBinding.errorMessage.visibility = View.VISIBLE
                mBinding.errorMessage.text = getString(R.string.empty_liked_film_review)
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mBinding.loadMoreBar.visibility = View.GONE
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                    false -> {
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.errorMessage.visibility = View.VISIBLE
                        mBinding.errorMessage.text = message
                    }
                }
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
        mFilmReviewAdapter = FilmReviewAdapter(mContext, mFilmReviewList, this)
        mBinding.recyclerView.adapter = mFilmReviewAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
        mBinding.recyclerView.visibility = View.VISIBLE
    }

    private fun likeReview(id: Int, position: Int) {
        val likeReviewRequest = LikeReviewRequest(mContext, id)
        likeReviewRequest.sendRequest(object : LikeReviewRequest.Callback {
            override fun onSuccess() {
                mTotalLike++
                val currentItem = mFilmReviewList[position]
                currentItem.isLiked = true
                currentItem.totalLike = mTotalLike
                mFilmReviewAdapter.notifyItemChanged(position)
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
                val currentItem = mFilmReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mFilmReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
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
}