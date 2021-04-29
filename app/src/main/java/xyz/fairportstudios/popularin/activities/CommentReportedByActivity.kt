package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.ReportAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.CommentReportsRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarRecyclerBinding
import xyz.fairportstudios.popularin.interfaces.CommentReportsRequestCallback
import xyz.fairportstudios.popularin.interfaces.ReportAdapterClickListener
import xyz.fairportstudios.popularin.models.Report
import xyz.fairportstudios.popularin.statics.Popularin

class CommentReportedByActivity : AppCompatActivity(), ReportAdapterClickListener {
    // Primitive
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mCommentReportsRequest: CommentReportsRequest
    private lateinit var mContext: Context
    private lateinit var mReports: MutableList<Report>
    private lateinit var mReportAdapter: ReportAdapter

    // Binding
    private lateinit var mBinding: ReusableToolbarRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ReusableToolbarRecyclerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Context
        mContext = this

        // Extra
        val commentId = intent.getIntExtra(Popularin.COMMENT_ID, 0)

        // Handler
        val handler = Handler()

        // Toolbar
        mBinding.toolbarTitle = getString(R.string.comment_reporter)

        // Mendapatkan data awal
        mCommentReportsRequest = CommentReportsRequest(mContext, commentId)
        mBinding.isLoading = true
        getCommentReports(mStartPage, false)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.isLoadingMore = true
                    handler.postDelayed({
                        getCommentReports(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getCommentReports(mStartPage, true)
        }
    }

    override fun onReportItemClick(position: Int) {
        gotoUserDetail(mReports[position].userId)
    }

    private fun getCommentReports(page: Int, refreshPage: Boolean) {
        mCommentReportsRequest.sendRequest(page, object : CommentReportsRequestCallback {
            override fun onSuccess(totalPage: Int, reports: List<Report>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mReports.clear()
                            mReportAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mReports.size
                        mReports.addAll(insertIndex, reports)
                        mReportAdapter.notifyItemRangeInserted(insertIndex, reports.size)
                        mBinding.isLoadingMore = false
                    }
                    false -> {
                        mReports = mutableListOf()
                        val insertIndex = mReports.size
                        mReports.addAll(insertIndex, reports)
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
                        mReports.clear()
                        mReportAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.isLoading = false
                }
                mBinding.loadSuccess = false
                mBinding.message = getString(R.string.empty_comment_reporter)
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
        mReportAdapter = ReportAdapter(mReports, this)
        mBinding.recyclerView.adapter = mReportAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }
}