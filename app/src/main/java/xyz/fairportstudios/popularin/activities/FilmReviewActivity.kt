package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.FilmReviewAdapter
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.FilmReviewFromAllRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.fragments.LikedReviewFragment
import xyz.fairportstudios.popularin.fragments.ReviewFromAllFragment
import xyz.fairportstudios.popularin.fragments.ReviewFromFollowingFragment
import xyz.fairportstudios.popularin.fragments.SelfReviewFragment
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class FilmReviewActivity : AppCompatActivity(), FilmReviewAdapter.OnClickListener {
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
    private lateinit var mFilmReviewList: ArrayList<FilmReview>
    private lateinit var mFilmReviewAdapter: FilmReviewAdapter
    private lateinit var mFilmReviewFromAllRequest: FilmReviewFromAllRequest
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerFilmReview: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Context
        mContext = this

        // Extra
        val intent = intent
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)

        // Auth
        val auth = Auth(mContext)
        mIsAuth = auth.isAuth()
        mAuthID = auth.getAuthID()

        // Menampilkan layout berdasarkan kondisi
        when (mIsAuth) {
            true -> {
                setContentView(R.layout.reusable_toolbar_pager)

                // Binding
                val tabLayout: TabLayout = findViewById(R.id.tab_rtp_layout)
                val toolbar: Toolbar = findViewById(R.id.toolbar_rtp_layout)
                val viewPager: ViewPager = findViewById(R.id.pager_rtp_layout)

                // Toolbar
                toolbar.title = R.string.review.toString()

                // Tab pager
                val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
                pagerAdapter.addFragment(ReviewFromAllFragment(filmID), R.string.all.toString())
                pagerAdapter.addFragment(ReviewFromFollowingFragment(filmID), R.string.following.toString())
                pagerAdapter.addFragment(LikedReviewFragment(filmID), R.string.liked.toString())
                pagerAdapter.addFragment(SelfReviewFragment(filmID), R.string.self.toString())
                viewPager.adapter = pagerAdapter
                viewPager.offscreenPageLimit = 4
                tabLayout.tabMode = TabLayout.MODE_AUTO
                tabLayout.setupWithViewPager(viewPager)

                // Activity
                toolbar.setNavigationOnClickListener { onBackPressed() }
            }
            false -> {
                setContentView(R.layout.reusable_toolbar_recycler)

                // Binding
                mProgressBar = findViewById(R.id.pbr_rtr_layout)
                mRecyclerFilmReview = findViewById(R.id.recycler_rtr_layout)
                mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
                mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
                mTextMessage = findViewById(R.id.text_aud_message)
                val toolbar: Toolbar = findViewById(R.id.toolbar_rtr_layout)

                // Toolbar
                toolbar.title = R.string.review.toString()

                // Mendapatkan data awal
                mFilmReviewFromAllRequest = FilmReviewFromAllRequest(mContext, filmID)
                getFilmReviewFromAll(mStartPage, false)

                // Activity
                toolbar.setNavigationOnClickListener { onBackPressed() }

                mRecyclerFilmReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            if (!mIsLoading && mCurrentPage <= mTotalPage) {
                                mIsLoading = true
                                mSwipeRefresh.isRefreshing = true
                                getFilmReviewFromAll(mCurrentPage, false)
                            }
                        }
                    }
                })

                mSwipeRefresh.setOnRefreshListener {
                    mIsLoading = true
                    mSwipeRefresh.isRefreshing = true
                    getFilmReviewFromAll(mStartPage, true)
                }
            }
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
        when (mIsAuth) {
            true -> {
                val currentItem = mFilmReviewList[position]
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

    override fun onFilmReviewCommentClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        val isSelf = currentItem.userID == mAuthID
        gotoReviewComment(currentItem.id, isSelf)
    }

    private fun getFilmReviewFromAll(page: Int, refreshPage: Boolean) {
        mFilmReviewFromAllRequest.sendRequest(page, object : FilmReviewFromAllRequest.Callback {
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
                        mFilmReviewAdapter = FilmReviewAdapter(mContext, mFilmReviewList, this@FilmReviewActivity)
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
                mTextMessage.text = R.string.empty_film_review.toString()
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mProgressBar.visibility = View.GONE
                        mTextMessage.visibility = View.VISIBLE
                        mTextMessage.text = R.string.empty_film_review.toString()
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

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}