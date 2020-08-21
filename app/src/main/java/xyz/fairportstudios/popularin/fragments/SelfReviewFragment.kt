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
import xyz.fairportstudios.popularin.activities.ReviewActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.SelfFilmReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.statics.Popularin

class SelfReviewFragment(private val filmID: Int) : Fragment(), FilmReviewAdapter.OnClickListener {
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
    private lateinit var mContext: Context
    private lateinit var mFilmReviewAdapter: FilmReviewAdapter
    private lateinit var mSelfFilmReviewRequest: SelfFilmReviewRequest

    // View
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLoadMoreBar: ProgressBar
    private lateinit var mRecyclerFilmReview: RecyclerView
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
        mRecyclerFilmReview = view.findViewById(R.id.recycler_rr_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_rr_layout)
        mTextMessage = view.findViewById(R.id.text_rr_message)
        val nestedScrollView = view.findViewById<NestedScrollView>(R.id.nested_scroll_rr_layout)

        // Handler
        val handler = Handler()

        // Activity
        nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mLoadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getSelfFilmReview(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getSelfFilmReview(mStartPage, true)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data awal
            mIsResumeFirstTime = false
            mSelfFilmReviewRequest = SelfFilmReviewRequest(mContext, filmID)
            getSelfFilmReview(mStartPage, false)
        }
    }

    override fun onFilmReviewItemClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        gotoReviewDetail(currentItem.id)
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
        gotoReviewComment(currentItem.id)
    }

    private fun getSelfFilmReview(page: Int, refreshPage: Boolean) {
        mSelfFilmReviewRequest.sendRequest(page, object : SelfFilmReviewRequest.Callback {
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
                mTextMessage.text = getString(R.string.empty_self_film_review)
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
        mFilmReviewAdapter = FilmReviewAdapter(mContext, mFilmReviewList, this)
        mRecyclerFilmReview.adapter = mFilmReviewAdapter
        mRecyclerFilmReview.layoutManager = LinearLayoutManager(mContext)
        mRecyclerFilmReview.visibility = View.VISIBLE
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

    private fun gotoReviewDetail(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, true)
        startActivity(intent)
    }

    private fun gotoReviewComment(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, true)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, 1)
        startActivity(intent)
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }
}