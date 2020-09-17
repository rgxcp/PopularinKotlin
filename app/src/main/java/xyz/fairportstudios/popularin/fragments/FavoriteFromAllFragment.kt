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
import xyz.fairportstudios.popularin.apis.popularin.get.FavoriteFromAllRequest
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.statics.Popularin

class FavoriteFromAllFragment(private val filmID: Int) : Fragment(), UserAdapter.OnClickListener {
    // Primitive
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mUserList: ArrayList<User>
    private lateinit var mContext: Context
    private lateinit var mFavoriteFromAllRequest: FavoriteFromAllRequest
    private lateinit var mUserAdapter: UserAdapter

    // Binding
    private var _mBinding: ReusableRecyclerBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Handler
        val handler = Handler()

        // Mendapatkan data awal
        mFavoriteFromAllRequest = FavoriteFromAllRequest(mContext, filmID)
        getFavoriteFromAll(mStartPage, false)

        // Activity
        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.loadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getFavoriteFromAll(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getFavoriteFromAll(mStartPage, true)
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

    private fun getFavoriteFromAll(page: Int, refreshPage: Boolean) {
        mFavoriteFromAllRequest.sendRequest(page, object : FavoriteFromAllRequest.Callback {
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
                        mBinding.progressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mBinding.errorMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mUserList.clear()
                        mUserAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.progressBar.visibility = View.GONE
                }
                mBinding.errorMessage.visibility = View.VISIBLE
                mBinding.errorMessage.text = getString(R.string.empty_film_favorite)
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mBinding.loadMoreBar.visibility = View.GONE
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                    false -> {
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.errorMessage.visibility = View.VISIBLE
                        mBinding.errorMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mBinding.swipeRefresh.isRefreshing = false
        mBinding.loadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun setAdapter() {
        mUserAdapter = UserAdapter(mContext, mUserList, this)
        mBinding.recyclerView.adapter = mUserAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
        mBinding.recyclerView.visibility = View.VISIBLE
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }
}