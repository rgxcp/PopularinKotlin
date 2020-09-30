package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PointAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.UserPointRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarRecyclerBinding
import xyz.fairportstudios.popularin.enums.PointType
import xyz.fairportstudios.popularin.interfaces.PointAdapterClickListener
import xyz.fairportstudios.popularin.interfaces.UserPointRequestCallback
import xyz.fairportstudios.popularin.models.Point
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class PointActivity : AppCompatActivity(), PointAdapterClickListener {
    // Primitive
    private var mIsSelf = false
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mPointList: ArrayList<Point>
    private lateinit var mContext: Context
    private lateinit var mPointAdapter: PointAdapter
    private lateinit var mUserPointRequest: UserPointRequest

    // Binding
    private lateinit var mBinding: ReusableToolbarRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ReusableToolbarRecyclerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Context
        mContext = this

        // Extra
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)

        // Auth
        val auth = Auth(mContext)
        mIsSelf = auth.isSelf(userID, auth.getAuthID())

        // Handler
        val handler = Handler()

        // Toolbar
        mBinding.toolbarTitle = getString(R.string.point)

        // Mendapatkan data awal
        mUserPointRequest = UserPointRequest(mContext, userID)
        mBinding.isLoading = true
        getUserPoint(mStartPage, false)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.isLoadingMore = true
                    handler.postDelayed({
                        getUserPoint(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getUserPoint(mStartPage, true)
        }
    }

    override fun onPointItemClick(position: Int) {
        val currentItem = mPointList[position]
        when (currentItem.type) {
            PointType.FAVORITE -> gotoFilmDetail(currentItem.typeID)
            PointType.REVIEW -> gotoReviewDetail(currentItem.typeID)
            PointType.WATCHLIST -> gotoFilmDetail(currentItem.typeID)
        }
    }

    private fun getUserPoint(page: Int, refreshPage: Boolean) {
        mUserPointRequest.sendRequest(page, object : UserPointRequestCallback {
            override fun onSuccess(totalPage: Int, pointList: ArrayList<Point>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mPointList.clear()
                            mPointAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mPointList.size
                        mPointList.addAll(insertIndex, pointList)
                        mPointAdapter.notifyItemRangeInserted(insertIndex, pointList.size)
                        mBinding.isLoadingMore = false
                    }
                    false -> {
                        mPointList = ArrayList()
                        val insertIndex = mPointList.size
                        mPointList.addAll(insertIndex, pointList)
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
                        mPointList.clear()
                        mPointAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.isLoading = false
                }
                mBinding.loadSuccess = false
                mBinding.message = when (mIsSelf) {
                    true -> getString(R.string.empty_self_point)
                    false -> getString(R.string.empty_user_point)
                }
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
        mPointAdapter = PointAdapter(mPointList, this)
        mBinding.recyclerView.adapter = mPointAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoReviewDetail(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, mIsSelf)
        startActivity(intent)
    }
}