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
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.adapters.UserAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.WatchlistFromAllRequest
import xyz.fairportstudios.popularin.fragments.WatchlistFromAllFragment
import xyz.fairportstudios.popularin.fragments.WatchlistFromFollowingFragment
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class WatchlistedByActivity : AppCompatActivity(), UserAdapter.OnClickListener {
    // Primitive
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mUserList: ArrayList<User>
    private lateinit var mContext: Context
    private lateinit var mUserAdapter: UserAdapter
    private lateinit var mWatchlistFromAllRequest: WatchlistFromAllRequest

    // View
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLoadMoreBar: ProgressBar
    private lateinit var mRecyclerUser: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Context
        mContext = this

        // Extra
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)

        // Menampilkan layout berdasarkan kondisi
        when (Auth(mContext).isAuth()) {
            true -> {
                setContentView(R.layout.reusable_toolbar_pager)

                // Binding
                val tabLayout = findViewById<TabLayout>(R.id.tab_rtp_layout)
                val toolbar = findViewById<Toolbar>(R.id.toolbar_rtp_layout)
                val viewPager = findViewById<ViewPager>(R.id.pager_rtp_layout)

                // Toolbar
                toolbar.title = getString(R.string.watchlisted_by)

                // Tab pager
                val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
                pagerAdapter.addFragment(WatchlistFromAllFragment(filmID), getString(R.string.all))
                pagerAdapter.addFragment(WatchlistFromFollowingFragment(filmID), getString(R.string.following))
                viewPager.adapter = pagerAdapter
                tabLayout.setupWithViewPager(viewPager)

                // Activity
                toolbar.setNavigationOnClickListener { onBackPressed() }
            }
            false -> {
                setContentView(R.layout.reusable_toolbar_recycler)

                // Binding
                mProgressBar = findViewById(R.id.pbr_rtr_layout)
                mLoadMoreBar = findViewById(R.id.lbr_rtr_layout)
                mRecyclerUser = findViewById(R.id.recycler_rtr_layout)
                mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
                mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
                mTextMessage = findViewById(R.id.text_aud_message)
                val nestedScrollView = findViewById<NestedScrollView>(R.id.nested_scroll_rtr_layout)
                val toolbar = findViewById<Toolbar>(R.id.toolbar_rtr_layout)

                // Handler
                val handler = Handler()

                // Toolbar
                toolbar.title = getString(R.string.watchlisted_by)

                // Mendapatkan data awal
                mWatchlistFromAllRequest = WatchlistFromAllRequest(mContext, filmID)
                getWatchlistFromAll(mStartPage, false)

                // Activity
                toolbar.setNavigationOnClickListener { onBackPressed() }

                nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                    if (scrollY > oldScrollY) {
                        if (!mIsLoading && mCurrentPage <= mTotalPage) {
                            mIsLoading = true
                            mLoadMoreBar.visibility = View.VISIBLE
                            handler.postDelayed({
                                getWatchlistFromAll(mCurrentPage, false)
                            }, 1000)
                        }
                    }
                }

                mSwipeRefresh.setOnRefreshListener {
                    mIsLoading = true
                    mSwipeRefresh.isRefreshing = true
                    getWatchlistFromAll(mStartPage, true)
                }
            }
        }
    }

    override fun onUserItemClick(position: Int) {
        val currentItem = mUserList[position]
        gotoUserDetail(currentItem.id)
    }

    private fun getWatchlistFromAll(page: Int, refreshPage: Boolean) {
        mWatchlistFromAllRequest.sendRequest(page, object : WatchlistFromAllRequest.Callback {
            override fun onSuccess(totalPage: Int, userList: ArrayList<User>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mUserList.clear()
                            mUserAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mUserList.size
                        mUserList.addAll(insertIndex, userList)
                        mUserAdapter.notifyItemRangeInserted(insertIndex, userList.size)
                    }
                    false -> {
                        mUserList = ArrayList()
                        val insertIndex = mUserList.size
                        mUserList.addAll(insertIndex, userList)
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
                mTextMessage.text = getString(R.string.empty_film_watchlist)
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
        mUserAdapter = UserAdapter(mContext, mUserList, this)
        mRecyclerUser.adapter = mUserAdapter
        mRecyclerUser.layoutManager = LinearLayoutManager(mContext)
        mRecyclerUser.visibility = View.VISIBLE
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }
}