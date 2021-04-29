package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.CommentReportedByActivity
import xyz.fairportstudios.popularin.activities.EmptyAccountActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.CommentAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.DeleteCommentRequest
import xyz.fairportstudios.popularin.apis.popularin.get.CommentRequest
import xyz.fairportstudios.popularin.apis.popularin.post.AddCommentRequest
import xyz.fairportstudios.popularin.apis.popularin.post.ReportCommentRequest
import xyz.fairportstudios.popularin.databinding.FragmentReviewCommentBinding
import xyz.fairportstudios.popularin.interfaces.AddCommentRequestCallback
import xyz.fairportstudios.popularin.interfaces.CommentAdapterClickListener
import xyz.fairportstudios.popularin.interfaces.CommentRequestCallback
import xyz.fairportstudios.popularin.interfaces.DeleteCommentRequestCallback
import xyz.fairportstudios.popularin.interfaces.ReportCommentRequestCallback
import xyz.fairportstudios.popularin.models.Comment
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.LoadReportCategory.getAllReportCategory
import xyz.fairportstudios.popularin.statics.Popularin

class ReviewCommentFragment(private val reviewID: Int) : Fragment(), CommentAdapterClickListener {
    // Primitive
    private var mIsAuth = false
    private var mIsResumeFirstTime = true
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mCommentList: ArrayList<Comment>
    private lateinit var mContext: Context
    private lateinit var mCommentAdapter: CommentAdapter
    private lateinit var mCommentRequest: CommentRequest
    private lateinit var mComment: String

    // Binding
    private var _mBinding: FragmentReviewCommentBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = FragmentReviewCommentBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Auth
        mIsAuth = Auth(mContext).isAuth()

        // Text watcher
        mBinding.inputComment.addTextChangedListener(mCommentWatcher)

        // Activity
        mBinding.sendImage.setOnClickListener {
            when (mIsAuth && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    addComment()
                }
                false -> gotoEmptyAccount()
            }
        }

        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsLoading && mCurrentPage <= mTotalPage) {
                        mIsLoading = true
                        mBinding.swipeRefresh.isRefreshing = true
                        getComment(mCurrentPage, false)
                    }
                }
            }
        })

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getComment(mStartPage, true)
        }

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data awal
            mIsResumeFirstTime = false
            mCommentList = ArrayList()
            mCommentRequest = CommentRequest(mContext, reviewID)
            mBinding.isLoading = true
            getComment(mStartPage, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    override fun onCommentProfileClick(position: Int) {
        val currentItem = mCommentList[position]
        gotoUserDetail(currentItem.userID)
    }

    override fun onCommentNSFWBannerClick(position: Int) {
        hideNSFWBanner(position)
    }

    override fun onCommentReportClick(position: Int) {
        gotoCommentReportedBy(mCommentList[position].id)
    }

    override fun onCommentMoreClick(position: Int, anchor: ImageView) {
        showPopupMenu(mCommentList[position].id, anchor)
    }

    override fun onCommentDeleteClick(position: Int) {
        val currentItem = mCommentList[position]
        if (!mIsLoading) {
            mIsLoading = true
            deleteComment(currentItem.id, position)
        }
    }

    private val mCommentWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mComment = mBinding.inputComment.text.toString()
            when (mComment.isNotEmpty()) {
                true -> mBinding.sendImage.visibility = View.VISIBLE
                false -> mBinding.sendImage.visibility = View.INVISIBLE
            }
        }
    }

    private fun getComment(page: Int, refreshPage: Boolean) {
        mCommentRequest.sendRequest(page, object : CommentRequestCallback {
            override fun onSuccess(totalPage: Int, commentList: ArrayList<Comment>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mCommentList.clear()
                            mCommentAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mCommentList.size
                        mCommentList.addAll(insertIndex, commentList)
                        mCommentAdapter.notifyItemRangeInserted(insertIndex, commentList.size)
                    }
                    false -> {
                        val insertIndex = mCommentList.size
                        mCommentList.addAll(insertIndex, commentList)
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
                        mCommentList.clear()
                        mCommentAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.isLoading = false
                }
                mBinding.loadSuccess = false
                mBinding.message = getString(R.string.empty_comment)
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
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
        mCommentAdapter = CommentAdapter(mCommentList, this)
        mBinding.recyclerView.adapter = mCommentAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
    }

    private fun addComment() {
        val addCommentRequest = AddCommentRequest(mContext, reviewID, mComment)
        addCommentRequest.sendRequest(object : AddCommentRequestCallback {
            override fun onSuccess(comment: Comment) {
                val insertIndex = mCommentList.size
                mCommentList.add(insertIndex, comment)
                if (!mIsLoadFirstTimeSuccess) {
                    setAdapter()
                    mIsLoadFirstTimeSuccess = true
                }
                mCommentAdapter.notifyItemInserted(insertIndex)
                mBinding.recyclerView.scrollToPosition(insertIndex)
                mBinding.loadSuccess = true
                mBinding.inputComment.text.clear()
            }

            override fun onFailed(message: String) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun deleteComment(id: Int, position: Int) {
        val deleteCommentRequest = DeleteCommentRequest(mContext, id)
        deleteCommentRequest.sendRequest(object : DeleteCommentRequestCallback {
            override fun onSuccess() {
                mCommentList.removeAt(position)
                mCommentAdapter.notifyItemRemoved(position)
                if (mCommentList.isEmpty()) {
                    mBinding.loadSuccess = false
                }
            }

            override fun onError(message: String) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun hideNSFWBanner(position: Int) {
        mCommentList[position].isNSFW = false
        mCommentAdapter.notifyItemChanged(position)
    }

    private fun showPopupMenu(commentId: Int, anchor: ImageView) {
        val popupMenu = PopupMenu(mContext, anchor)
        popupMenu.inflate(R.menu.comment_guest)
        popupMenu.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.menu_cg_report -> {
                    when (mIsAuth) {
                        true -> showPickReportCategoryDialog(commentId)
                        false -> gotoEmptyAccount()
                    }
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showPickReportCategoryDialog(commentId: Int) {
        val reportCategories = getAllReportCategory(mContext)
        var pickedReportCategory = 0

        MaterialAlertDialogBuilder(mContext)
            .setTitle(getString(R.string.pick_report_category_dialog_title))
            .setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(getString(R.string.pick)) { _, _ ->
                reportComment(commentId, pickedReportCategory + 1)
            }
            .setSingleChoiceItems(reportCategories, pickedReportCategory) { _, which ->
                pickedReportCategory = which
            }
            .show()
    }

    private fun reportComment(commentId: Int, reportCategoryId: Int) {
        val reportCommentRequest = ReportCommentRequest(mContext, commentId, reportCategoryId)
        reportCommentRequest.sendRequest(object : ReportCommentRequestCallback {
            override fun onSuccess() {
                Toast.makeText(mContext, getString(R.string.comment_reported), Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun gotoCommentReportedBy(id: Int) {
        val intent = Intent(mContext, CommentReportedByActivity::class.java)
        intent.putExtra(Popularin.COMMENT_ID, id)
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