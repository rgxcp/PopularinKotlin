package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.UserReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.UserReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.UserReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class UserReviewActivity : AppCompatActivity(), UserReviewAdapter.OnClickListener {
    // Variable untuk fitur load more
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false
    private val mStartPage: Int = 1
    private var mCurrentPage: Int = 1
    private var mTotalPage: Int = 0

    // Variable member
    private var mIsAuth: Boolean = false
    private var mIsSelf: Boolean = false
    private var mTotalLike: Int = 0
    private lateinit var mContext: Context
    private lateinit var mUserReviewList: ArrayList<UserReview>
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerUserReview: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView
    private lateinit var mUserReviewAdapter: UserReviewAdapter
    private lateinit var mUserReviewRequest: UserReviewRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_toolbar_recycler)

        // Context
        mContext = this

        // Binding
        mProgressBar = findViewById(R.id.pbr_rtr_layout)
        mRecyclerUserReview = findViewById(R.id.recycler_rtr_layout)
        mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
        mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
        mTextMessage = findViewById(R.id.text_aud_message)
        val toolbar: Toolbar = findViewById(R.id.toolbar_rtr_layout)

        // Extra
        val intent = intent
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)

        // Auth
        val auth = Auth(mContext)
        mIsAuth = auth.isAuth()
        mIsSelf = userID == auth.getAuthID()

        // Toolbar
        toolbar.title = R.string.review.toString()

        // Mendapatkan data awal
        mUserReviewRequest = UserReviewRequest(mContext, userID)
        getUserReview(mStartPage, false)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }

        mRecyclerUserReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsLoading && mCurrentPage <= mTotalPage) {
                        mIsLoading = true
                        mSwipeRefresh.isRefreshing = true
                        getUserReview(mCurrentPage, false)
                    }
                }
            }
        })

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getUserReview(mStartPage, true)
        }
    }

    override fun onUserReviewItemClick(position: Int) {
        val currentItem = mUserReviewList[position]
        gotoReviewDetail(currentItem.id)
    }

    override fun onUserReviewFilmPosterClick(position: Int) {
        val currentItem = mUserReviewList[position]
        gotoFilmDetail(currentItem.tmdbID)
    }

    override fun onUserReviewFilmPosterLongClick(position: Int) {
        val currentItem = mUserReviewList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.id, currentItem.title, year, currentItem.poster)
    }

    override fun onUserReviewLikeClick(position: Int) {
        when (mIsAuth) {
            true -> {
                val currentItem = mUserReviewList[position]
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

    override fun onUserReviewCommentClick(position: Int) {
        val currentItem = mUserReviewList[position]
        gotoReviewComment(currentItem.id)
    }

    private fun getUserReview(page: Int, refreshPage: Boolean) {
        mUserReviewRequest.sendRequest(page, object : UserReviewRequest.Callback {
            override fun onSuccess(totalPage: Int, userReviewList: ArrayList<UserReview>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mUserReviewList.clear()
                            mUserReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mUserReviewList.size
                        mUserReviewList.addAll(insertIndex, userReviewList)
                        mUserReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mUserReviewAdapter.notifyItemRangeInserted(insertIndex, userReviewList.size)
                        mRecyclerUserReview.scrollToPosition(insertIndex)
                    }
                    false -> {
                        mUserReviewList = ArrayList()
                        val insertIndex = mUserReviewList.size
                        mUserReviewList.addAll(insertIndex, userReviewList)
                        mUserReviewAdapter = UserReviewAdapter(mContext, mUserReviewList, this@UserReviewActivity)
                        mRecyclerUserReview.adapter = mUserReviewAdapter
                        mRecyclerUserReview.layoutManager = LinearLayoutManager(mContext)
                        mRecyclerUserReview.visibility = View.VISIBLE
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
                        mUserReviewList.clear()
                        mUserReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = when (mIsSelf) {
                    true -> R.string.empty_self_review.toString()
                    false -> R.string.empty_user_review.toString()
                }
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

    private fun gotoReviewDetail(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, mIsSelf)
        startActivity(intent)
    }

    private fun gotoReviewComment(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, mIsSelf)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, 1)
        startActivity(intent)
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(supportFragmentManager, Popularin.FILM_MODAL)
    }

    private fun likeReview(id: Int, position: Int) {
        val likeReviewRequest = LikeReviewRequest(mContext, id)
        likeReviewRequest.sendRequest(object : LikeReviewRequest.Callback {
            override fun onSuccess() {
                mTotalLike++
                val currentItem = mUserReviewList[position]
                currentItem.isLiked = true
                currentItem.totalLike = mTotalLike
                mUserReviewAdapter.notifyItemChanged(position)
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
                val currentItem = mUserReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mUserReviewAdapter.notifyItemChanged(position)
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
}