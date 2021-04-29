package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.UserAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.WatchlistFromAllRequest
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
import xyz.fairportstudios.popularin.interfaces.UserAdapterClickListener
import xyz.fairportstudios.popularin.interfaces.WatchlistFromAllRequestCallback
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.statics.Popularin

class WatchlistFromAllFragment(private val filmID: Int) : Fragment(), UserAdapterClickListener {
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

    // Binding
    private var _mBinding: ReusableRecyclerBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Handler
        val handler = Handler()

        // Mendapatkan data awal
        mWatchlistFromAllRequest = WatchlistFromAllRequest(mContext, filmID)
        mBinding.isLoading = true
        getWatchlistFromAll(mStartPage, false)

        // Activity
        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.isLoadingMore = true
                    handler.postDelayed({
                        getWatchlistFromAll(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getWatchlistFromAll(mStartPage, true)
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    override fun onUserItemClick(position: Int) {
        val currentItem = mUserList[position]
        gotoUserDetail(currentItem.id)
    }

    private fun getWatchlistFromAll(page: Int, refreshPage: Boolean) {
        mWatchlistFromAllRequest.sendRequest(page, object : WatchlistFromAllRequestCallback {
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
                mBinding.message = getString(R.string.empty_film_watchlist)
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