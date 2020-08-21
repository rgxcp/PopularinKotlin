package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
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
    // Primitive
    private var mIsAuth = false
    private var mIsSelf = false
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0
    private var mTotalLike = 0

    // Member
    private lateinit var mUserReviewList: ArrayList<UserReview>
    private lateinit var mContext: Context
    private lateinit var mUserReviewAdapter: UserReviewAdapter
    private lateinit var mUserReviewRequest: UserReviewRequest

    // View
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLoadMoreBar: ProgressBar
    private lateinit var mRecyclerUserReview: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_toolbar_recycler)

        // Context
        mContext = this

        // Binding
        mProgressBar = findViewById(R.id.pbr_rtr_layout)
        mLoadMoreBar = findViewById(R.id.lbr_rtr_layout)
        mRecyclerUserReview = findViewById(R.id.recycler_rtr_layout)
        mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
        mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
        mTextMessage = findViewById(R.id.text_aud_message)
        val nestedScrollView = findViewById<NestedScrollView>(R.id.nested_scroll_rtr_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_rtr_layout)

        // Extra
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)

        // Auth
        val auth = Auth(mContext)
        mIsAuth = auth.isAuth()
        mIsSelf = auth.isSelf(userID, auth.getAuthID())

        // Handler
        val handler = Handler()

        // Toolbar
        toolbar.title = getString(R.string.review)

        // Mendapatkan data awal
        mUserReviewRequest = UserReviewRequest(mContext, userID)
        getUserReview(mStartPage, false)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }

        nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mLoadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getUserReview(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

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
                            mTotalPage = totalPage
                            mUserReviewList.clear()
                            mUserReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mUserReviewList.size
                        mUserReviewList.addAll(insertIndex, userReviewList)
                        mUserReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mUserReviewAdapter.notifyItemRangeInserted(insertIndex, userReviewList.size)
                    }
                    false -> {
                        mUserReviewList = ArrayList()
                        val insertIndex = mUserReviewList.size
                        mUserReviewList.addAll(insertIndex, userReviewList)
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
                mTextMessage.text = when (mIsSelf) {
                    true -> getString(R.string.empty_self_review)
                    false -> getString(R.string.empty_user_review)
                }
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mLoadMoreBar.visibility = View.GONE
                        Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
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
        if (refreshPage) mSwipeRefresh.isRefreshing = false
        mLoadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun setAdapter() {
        mUserReviewAdapter = UserReviewAdapter(mContext, mUserReviewList, this)
        mRecyclerUserReview.adapter = mUserReviewAdapter
        mRecyclerUserReview.layoutManager = LinearLayoutManager(mContext)
        mRecyclerUserReview.visibility = View.VISIBLE
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

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(supportFragmentManager, Popularin.FILM_MODAL)
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

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}