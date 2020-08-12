package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
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
import xyz.fairportstudios.popularin.apis.popularin.get.LikeFromAllRequest
import xyz.fairportstudios.popularin.fragments.LikeFromAllFragment
import xyz.fairportstudios.popularin.fragments.LikeFromFollowingFragment
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class LikedByActivity : AppCompatActivity(), UserAdapter.OnClickListener {
    // Variable untuk fitur load more
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false
    private val mStartPage: Int = 1
    private var mCurrentPage: Int = 1
    private var mTotalPage: Int = 0

    // Variable member
    private lateinit var mContext: Context
    private lateinit var mUserList: ArrayList<User>
    private lateinit var mLikeFromAllRequest: LikeFromAllRequest
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerUser: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView
    private lateinit var mUserAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding
        mContext = this

        // Extra
        val intent = intent
        val reviewID = intent.getIntExtra(Popularin.REVIEW_ID, 0)

        // Menampilkan layout berdasarkan kondisi
        when (Auth(mContext).isAuth()) {
            true -> {
                setContentView(R.layout.reusable_toolbar_pager)

                // Binding
                val tabLayout: TabLayout = findViewById(R.id.tab_rtp_layout)
                val toolbar: Toolbar = findViewById(R.id.toolbar_rtp_layout)
                val viewPager: ViewPager = findViewById(R.id.pager_rtp_layout)

                // Toolbar
                toolbar.title = R.string.liked_by.toString()

                // Tab pager
                val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
                pagerAdapter.addFragment(LikeFromAllFragment(reviewID), R.string.all.toString())
                pagerAdapter.addFragment(LikeFromFollowingFragment(reviewID), R.string.following.toString())
                viewPager.adapter = pagerAdapter
                tabLayout.setupWithViewPager(viewPager)

                // Activity
                toolbar.setNavigationOnClickListener { onBackPressed() }
            }
            false -> {
                setContentView(R.layout.reusable_toolbar_recycler)

                // Binding
                mProgressBar = findViewById(R.id.pbr_rtr_layout)
                mRecyclerUser = findViewById(R.id.recycler_rtr_layout)
                mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
                mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
                mTextMessage = findViewById(R.id.text_aud_message)
                val toolbar: Toolbar = findViewById(R.id.toolbar_rtr_layout)

                // Toolbar
                toolbar.title = R.string.liked_by.toString()

                // Mendapatkan data awal
                mLikeFromAllRequest = LikeFromAllRequest(mContext, reviewID)
                getLikeFromAll(mStartPage, false)

                // Activity
                toolbar.setNavigationOnClickListener { onBackPressed() }

                mRecyclerUser.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            if (!mIsLoading && mCurrentPage <= mTotalPage) {
                                mIsLoading = true
                                mSwipeRefresh.isRefreshing = true
                                getLikeFromAll(mCurrentPage, false)
                            }
                        }
                    }
                })

                mSwipeRefresh.setOnRefreshListener {
                    mIsLoading = true
                    mSwipeRefresh.isRefreshing = true
                    getLikeFromAll(mStartPage, true)
                }
            }
        }
    }

    override fun onUserItemClick(position: Int) {
        val currentItem = mUserList[position]
        gotoUserDetail(currentItem.id)
    }

    private fun getLikeFromAll(page: Int, refreshPage: Boolean) {
        mLikeFromAllRequest.sendRequest(page, object : LikeFromAllRequest.Callback {
            override fun onSuccess(totalPage: Int, userList: ArrayList<User>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mUserList.clear()
                            mUserAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mUserList.size
                        mUserList.addAll(insertIndex, userList)
                        mUserAdapter.notifyItemRangeInserted(insertIndex, userList.size)
                        mRecyclerUser.scrollToPosition(insertIndex)
                    }
                    false -> {
                        mUserList = ArrayList()
                        val insertIndex = mUserList.size
                        mUserList.addAll(insertIndex, userList)
                        mUserAdapter = UserAdapter(mContext, mUserList, this@LikedByActivity)
                        mRecyclerUser.adapter = mUserAdapter
                        mRecyclerUser.layoutManager = LinearLayoutManager(mContext)
                        mRecyclerUser.visibility = View.VISIBLE
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
                        mUserList.clear()
                        mUserAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = R.string.empty_review_like.toString()
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

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }
}