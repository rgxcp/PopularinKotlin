package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.adapters.UserAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.LikeFromAllRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarPagerBinding
import xyz.fairportstudios.popularin.databinding.ReusableToolbarRecyclerBinding
import xyz.fairportstudios.popularin.fragments.LikeFromAllFragment
import xyz.fairportstudios.popularin.fragments.LikeFromFollowingFragment
import xyz.fairportstudios.popularin.interfaces.LikeFromAllRequestCallback
import xyz.fairportstudios.popularin.interfaces.UserAdapterClickListener
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class LikedByActivity : AppCompatActivity(), UserAdapterClickListener {
    // Primitive
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mUserList: ArrayList<User>
    private lateinit var mContext: Context
    private lateinit var mLikeFromAllRequest: LikeFromAllRequest
    private lateinit var mUserAdapter: UserAdapter

    // Binding
    private lateinit var mBinding: ReusableToolbarRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Context
        mContext = this

        // Extra
        val reviewID = intent.getIntExtra(Popularin.REVIEW_ID, 0)

        // Menampilkan layout berdasarkan kondisi
        when (Auth(mContext).isAuth()) {
            true -> {
                val viewBinding = ReusableToolbarPagerBinding.inflate(layoutInflater)
                setContentView(viewBinding.root)

                // Toolbar
                viewBinding.toolbar.title = getString(R.string.liked_by)

                // Tab pager
                val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
                pagerAdapter.addFragment(LikeFromAllFragment(reviewID), getString(R.string.all))
                pagerAdapter.addFragment(LikeFromFollowingFragment(reviewID), getString(R.string.following))
                viewBinding.viewPager.adapter = pagerAdapter
                viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)

                // Activity
                viewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
            }
            false -> {
                mBinding = ReusableToolbarRecyclerBinding.inflate(layoutInflater)
                setContentView(R.layout.reusable_toolbar_recycler)

                // Handler
                val handler = Handler()

                // Toolbar
                mBinding.toolbarTitle = getString(R.string.liked_by)

                // Mendapatkan data awal
                mLikeFromAllRequest = LikeFromAllRequest(mContext, reviewID)
                mBinding.isLoading = true
                getLikeFromAll(mStartPage, false)

                // Activity
                mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

                mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                    if (scrollY > oldScrollY) {
                        if (!mIsLoading && mCurrentPage <= mTotalPage) {
                            mIsLoading = true
                            mBinding.isLoadingMore = true
                            handler.postDelayed({
                                getLikeFromAll(mCurrentPage, false)
                            }, 1000)
                        }
                    }
                }

                mBinding.swipeRefresh.setOnRefreshListener {
                    mIsLoading = true
                    mBinding.swipeRefresh.isRefreshing = true
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
        mLikeFromAllRequest.sendRequest(page, object : LikeFromAllRequestCallback {
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
                        mBinding.isLoadingMore = false
                    }
                    false -> {
                        mUserList = ArrayList()
                        val insertIndex = mUserList.size
                        mUserList.addAll(insertIndex, userList)
                        setAdapter()
                        mTotalPage = totalPage
                        mBinding.isLoading = false
                        mBinding.loadSuccess = true
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mUserList.clear()
                        mUserAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.isLoading = false
                }
                mBinding.loadSuccess = false
                mBinding.message = getString(R.string.empty_review_like)
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mBinding.isLoadingMore = false
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                    false -> {
                        mBinding.isLoading = false
                        mBinding.loadSuccess = false
                        mBinding.message = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mBinding.swipeRefresh.isRefreshing = false
    }

    private fun setAdapter() {
        mUserAdapter = UserAdapter(mUserList, this)
        mBinding.recyclerView.adapter = mUserAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }
}