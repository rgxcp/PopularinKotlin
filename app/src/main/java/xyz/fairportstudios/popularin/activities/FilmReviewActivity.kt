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
    // Primitive
    private var mIsAuth = false
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0
    private var mTotalLike = 0

    // Member
    private lateinit var mFilmReviewList: ArrayList<FilmReview>
    private lateinit var mAuth: Auth
    private lateinit var mContext: Context
    private lateinit var mFilmReviewAdapter: FilmReviewAdapter
    private lateinit var mFilmReviewFromAllRequest: FilmReviewFromAllRequest

    // View
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLoadMoreBar: ProgressBar
    private lateinit var mRecyclerFilmReview: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Context
        mContext = this

        // Extra
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)

        // Auth
        mAuth = Auth(mContext)
        mIsAuth = mAuth.isAuth()

        // Menampilkan layout berdasarkan kondisi
        when (mIsAuth) {
            true -> {
                setContentView(R.layout.reusable_toolbar_pager)

                // Binding
                val tabLayout = findViewById<TabLayout>(R.id.tab_rtp_layout)
                val toolbar = findViewById<Toolbar>(R.id.toolbar_rtp_layout)
                val viewPager = findViewById<ViewPager>(R.id.pager_rtp_layout)

                // Toolbar
                toolbar.title = getString(R.string.review)

                // Tab pager
                val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
                pagerAdapter.addFragment(ReviewFromAllFragment(filmID), getString(R.string.all))
                pagerAdapter.addFragment(ReviewFromFollowingFragment(filmID), getString(R.string.following))
                pagerAdapter.addFragment(LikedReviewFragment(filmID), getString(R.string.liked))
                pagerAdapter.addFragment(SelfReviewFragment(filmID), getString(R.string.self))
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
                mLoadMoreBar = findViewById(R.id.lbr_rtr_layout)
                mRecyclerFilmReview = findViewById(R.id.recycler_rtr_layout)
                mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
                mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
                mTextMessage = findViewById(R.id.text_aud_message)
                val nestedScrollView = findViewById<NestedScrollView>(R.id.nested_scroll_rtr_layout)
                val toolbar = findViewById<Toolbar>(R.id.toolbar_rtr_layout)

                // Handler
                val handler = Handler()

                // Toolbar
                toolbar.title = getString(R.string.review)

                // Mendapatkan data awal
                mFilmReviewFromAllRequest = FilmReviewFromAllRequest(mContext, filmID)
                getFilmReviewFromAll(mStartPage, false)

                // Activity
                toolbar.setNavigationOnClickListener { onBackPressed() }

                nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                    if (scrollY > oldScrollY) {
                        if (!mIsLoading && mCurrentPage <= mTotalPage) {
                            mIsLoading = true
                            mLoadMoreBar.visibility = View.VISIBLE
                            handler.postDelayed({
                                getFilmReviewFromAll(mCurrentPage, false)
                            }, 1000)
                        }
                    }
                }

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
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewDetail(currentItem.id, isSelf)
    }

    override fun onFilmReviewUserProfileClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        gotoUserDetail(currentItem.userID)
    }

    override fun onFilmReviewLikeClick(position: Int) {
        when (mIsAuth) {
            true -> {
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
            false -> gotoEmptyAccount()
        }
    }

    override fun onFilmReviewCommentClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewComment(currentItem.id, isSelf)
    }

    private fun getFilmReviewFromAll(page: Int, refreshPage: Boolean) {
        mFilmReviewFromAllRequest.sendRequest(page, object : FilmReviewFromAllRequest.Callback {
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
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mFilmReviewList.clear()
                        mFilmReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = getString(R.string.empty_film_review)
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

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}