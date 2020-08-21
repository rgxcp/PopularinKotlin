package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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

    // View
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLoadMoreBar: ProgressBar
    private lateinit var mRecyclerReview: RecyclerView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.reusable_recycler, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mAnchorLayout = view.findViewById(R.id.anchor_rr_layout)
        mProgressBar = view.findViewById(R.id.pbr_rr_layout)
        mLoadMoreBar = view.findViewById(R.id.lbr_rr_layout)
        mRecyclerReview = view.findViewById(R.id.recycler_rr_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_rr_layout)
        mTextMessage = view.findViewById(R.id.text_rr_message)
        val nestedScrollView = view.findViewById<NestedScrollView>(R.id.nested_scroll_rr_layout)

        // Auth
        mAuth = Auth(mContext)
        mIsAuth = mAuth.isAuth()

        // Handler
        val handler = Handler()

        // Mendapatkan data awal
        mReviewRequest = ReviewRequest(mContext)
        getReview(mStartPage, false)

        // Activity
        nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mLoadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getReview(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getReview(mStartPage, true)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        showFilmModal(currentItem.id, currentItem.title, year, currentItem.poster)
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
                        mProgressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mTextMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = getString(R.string.empty_review)
            }

            override fun onError(message: String) {
                if (!mIsLoadFirstTimeSuccess) {
                    mProgressBar.visibility = View.GONE
                    mTextMessage.visibility = View.VISIBLE
                    mTextMessage.text = getString(R.string.empty_review)
                }
                mLoadMoreBar.visibility = View.GONE
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mSwipeRefresh.isRefreshing = false
        mLoadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun setAdapter() {
        mReviewAdapter = ReviewAdapter(mContext, mReviewList, this)
        mRecyclerReview.adapter = mReviewAdapter
        mRecyclerReview.layoutManager = LinearLayoutManager(mContext)
        mRecyclerReview.visibility = View.VISIBLE
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
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
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
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
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