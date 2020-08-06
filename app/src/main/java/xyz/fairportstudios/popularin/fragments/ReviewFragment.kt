package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
    // Variable untuk fitur load more
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false
    private val mStartPage: Int = 1
    private var mCurrentPage: Int = 1
    private var mTotalPage: Int = 0

    // Variable member
    private var mIsAuth: Boolean = false
    private var mAuthID: Int = 0
    private var mTotalLike: Int = 0
    private lateinit var mContext: Context
    private lateinit var mReviewList: ArrayList<Review>
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerReview: RecyclerView
    private lateinit var mReviewAdapter: ReviewAdapter
    private lateinit var mReviewRequest: ReviewRequest
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.reusable_recycler, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mAnchorLayout = view.findViewById(R.id.anchor_rr_layout)
        mProgressBar = view.findViewById(R.id.pbr_rr_layout)
        mRecyclerReview = view.findViewById(R.id.recycler_rr_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_rr_layout)
        mTextMessage = view.findViewById(R.id.text_rr_message)

        // Auth
        val auth = Auth(mContext)
        mIsAuth = auth.isAuth()
        mAuthID = auth.getAuthID()

        // Mendapatkan data awal
        mReviewRequest = ReviewRequest(mContext)
        getReview(mStartPage, false)

        // Activity
        mRecyclerReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsLoading && mCurrentPage <= mTotalPage) {
                        mIsLoading = true
                        mSwipeRefresh.isRefreshing = true
                        getReview(mCurrentPage, false)
                    }
                }
            }
        })

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = false
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
        val isSelf = currentItem.userID == mAuthID
        gotoReviewDetail(currentItem.id, isSelf)
    }

    override fun onReviewUserProfileClick(position: Int) {
        val currentItem = mReviewList[position]
        gotoUserDetail(currentItem.id)
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
        val isSelf = currentItem.userID == mAuthID
        gotoReviewComment(currentItem.id, isSelf)
    }

    private fun getReview(page: Int, refreshPage: Boolean) {
        mReviewRequest.sendRequest(page, object : ReviewRequest.Callback {
            override fun onSuccess(totalPage: Int, reviewList: ArrayList<Review>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mReviewList.clear()
                            mReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mReviewList.size
                        mReviewList.addAll(insertIndex, reviewList)
                        mReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mReviewAdapter.notifyItemRangeInserted(insertIndex, reviewList.size)
                        mRecyclerReview.scrollToPosition(insertIndex)
                    }
                    false -> {
                        mReviewList = ArrayList()
                        val insertIndex = mReviewList.size
                        mReviewList.addAll(insertIndex, reviewList)
                        mReviewAdapter = ReviewAdapter(mContext, mReviewList, this@ReviewFragment)
                        mRecyclerReview.adapter = mReviewAdapter
                        mRecyclerReview.layoutManager = LinearLayoutManager(mContext)
                        mRecyclerReview.visibility = View.VISIBLE
                        mProgressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mTextMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mReviewList.clear()
                        mReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = R.string.empty_review.toString()
            }

            override fun onError(message: String) {
                if (!mIsLoadFirstTimeSuccess) {
                    mProgressBar.visibility = View.GONE
                    mTextMessage.visibility = View.VISIBLE
                    mTextMessage.text = R.string.empty_review.toString()
                }
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        mSwipeRefresh.isRefreshing = false
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

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val fragmentManager = (mContext as FragmentActivity).supportFragmentManager
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(fragmentManager, Popularin.FILM_MODAL)
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

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }

    private fun resetState() {
        mIsLoading = true
        mIsLoadFirstTimeSuccess = false
        mCurrentPage = 1
    }
}