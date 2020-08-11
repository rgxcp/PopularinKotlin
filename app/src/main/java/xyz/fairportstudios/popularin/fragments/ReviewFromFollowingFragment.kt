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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.ReviewActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.FilmReviewFromFollowingRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class ReviewFromFollowingFragment(private val filmID: Int) : Fragment(), FilmReviewAdapter.OnClickListener {
    // Variable untuk fitur onResume
    private var mIsResumeFirstTime: Boolean = true

    // Variable untuk fitur load more
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false
    private val mStartPage: Int = 1
    private var mCurrentPage: Int = 1
    private var mTotalPage: Int = 0

    // Variable member
    private var mAuthID: Int = 1
    private var mTotalLike: Int = 0
    private lateinit var mContext: Context
    private lateinit var mFilmReviewList: ArrayList<FilmReview>
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mFilmReviewAdapter: FilmReviewAdapter
    private lateinit var mFilmReviewFromFollowingRequest: FilmReviewFromFollowingRequest
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerFilmReview: RecyclerView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.reusable_recycler, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mAnchorLayout = view.findViewById(R.id.anchor_rr_layout)
        mProgressBar = view.findViewById(R.id.pbr_rr_layout)
        mRecyclerFilmReview = view.findViewById(R.id.recycler_rr_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_rr_layout)
        mTextMessage = view.findViewById(R.id.text_rr_message)

        // Auth
        mAuthID = Auth(mContext).getAuthID()

        // Activity
        mRecyclerFilmReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsLoading && mCurrentPage <= mTotalPage) {
                        mIsLoading = true
                        mSwipeRefresh.isRefreshing = true
                        getFilmReviewFromFollowing(mCurrentPage, false)
                    }
                }
            }
        })

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getFilmReviewFromFollowing(mStartPage, true)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data awal
            mIsResumeFirstTime = false
            mFilmReviewFromFollowingRequest = FilmReviewFromFollowingRequest(mContext, filmID)
            getFilmReviewFromFollowing(mStartPage, false)
        }
    }

    override fun onFilmReviewItemClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        val isSelf = currentItem.userID == mAuthID
        gotoReviewDetail(currentItem.id, isSelf)
    }

    override fun onFilmReviewUserProfileClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        gotoUserDetail(currentItem.id)
    }

    override fun onFilmReviewLikeClick(position: Int) {
        val currentItem = mFilmReviewList[position]
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
        val isSelf = currentItem.userID == mAuthID
        gotoReviewComment(currentItem.id, isSelf)
    }

    private fun getFilmReviewFromFollowing(page: Int, refreshPage: Boolean) {
        mFilmReviewFromFollowingRequest.sendRequest(page, object : FilmReviewFromFollowingRequest.Callback {
            override fun onSuccess(totalPage: Int, filmReviewList: ArrayList<FilmReview>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mFilmReviewList.clear()
                            mFilmReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmReviewList.size
                        mFilmReviewList.addAll(insertIndex, filmReviewList)
                        mFilmReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mFilmReviewAdapter.notifyItemRangeInserted(insertIndex, filmReviewList.size)
                        mRecyclerFilmReview.scrollToPosition(insertIndex)
                    }
                    false -> {
                        mFilmReviewList = ArrayList()
                        val insertIndex = mFilmReviewList.size
                        mFilmReviewList.addAll(insertIndex, filmReviewList)
                        mFilmReviewAdapter = FilmReviewAdapter(mContext, mFilmReviewList, this@ReviewFromFollowingFragment)
                        mRecyclerFilmReview.adapter = mFilmReviewAdapter
                        mRecyclerFilmReview.layoutManager = LinearLayoutManager(mContext)
                        mRecyclerFilmReview.visibility = View.VISIBLE
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
                        mFilmReviewList.clear()
                        mFilmReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = R.string.empty_film_review_from_following.toString()
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mProgressBar.visibility = View.GONE
                        mTextMessage.visibility = View.VISIBLE
                        mTextMessage.text = message
                    }
                }
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
                val currentItem = mFilmReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mFilmReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}