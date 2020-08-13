package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.EmptyAccountActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.CommentAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.DeleteCommentRequest
import xyz.fairportstudios.popularin.apis.popularin.get.CommentRequest
import xyz.fairportstudios.popularin.apis.popularin.post.AddCommentRequest
import xyz.fairportstudios.popularin.models.Comment
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class ReviewCommentFragment(private val reviewID: Int) : Fragment(), CommentAdapter.OnClickListener {
    // Variable untuk fitur onResume
    private var mIsResumeFirstTime: Boolean = true

    // Variable untuk fitur load more
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false
    private val mStartPage: Int = 1
    private var mCurrentPage: Int = 1
    private var mTotalPage: Int = 0

    // Variable member
    private var mAuthID: Int = 0
    private lateinit var mContext: Context
    private lateinit var mCommentList: ArrayList<Comment>
    private lateinit var mCommentAdapter: CommentAdapter
    private lateinit var mCommentRequest: CommentRequest
    private lateinit var mInputComment: EditText
    private lateinit var mImageSend: ImageView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerComment: RecyclerView
    private lateinit var mComment: String
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_review_comment, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mInputComment = view.findViewById(R.id.input_frc_comment)
        mImageSend = view.findViewById(R.id.image_frc_send)
        mProgressBar = view.findViewById(R.id.pbr_rr_layout)
        mRecyclerComment = view.findViewById(R.id.recycler_rr_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_rr_layout)
        mTextMessage = view.findViewById(R.id.text_rr_message)

        // Auth
        val auth = Auth(mContext)
        mAuthID = auth.getAuthID()
        val isAuth = auth.isAuth()

        // Text watcher
        mInputComment.addTextChangedListener(commentWatcher)

        // Activity
        mImageSend.setOnClickListener {
            when (isAuth && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    addComment()
                }
                false -> gotoEmptyAccount()
            }
        }

        mRecyclerComment.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsLoading && mCurrentPage <= mTotalPage) {
                        mIsLoading = true
                        mSwipeRefresh.isRefreshing = true
                        getComment(mCurrentPage, false)
                    }
                }
            }
        })

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getComment(mStartPage, true)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data awal
            mIsResumeFirstTime = false
            mCommentList = ArrayList()
            mCommentRequest = CommentRequest(mContext, reviewID)
            getComment(mStartPage, false)
        }
    }

    override fun onCommentProfileClick(position: Int) {
        val currentItem = mCommentList[position]
        gotoUserDetail(currentItem.userID)
    }

    override fun onCommentDeleteClick(position: Int) {
        val currentItem = mCommentList[position]
        if (!mIsLoading) {
            mIsLoading = true
            deleteComment(currentItem.id, position)
        }
    }

    private val commentWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mComment = mInputComment.text.toString()
            when (mComment.isNotEmpty()) {
                true -> mImageSend.visibility = View.VISIBLE
                false -> mImageSend.visibility = View.INVISIBLE
            }
        }
    }

    private fun createAdapter() {
        mCommentAdapter = CommentAdapter(mContext, mAuthID, mCommentList, this)
        mRecyclerComment.adapter = mCommentAdapter
        mRecyclerComment.layoutManager = LinearLayoutManager(mContext)
        mRecyclerComment.visibility = View.VISIBLE
    }

    private fun getComment(page: Int, refreshPage: Boolean) {
        mCommentRequest.sendRequest(page, object : CommentRequest.Callback {
            override fun onSuccess(totalPage: Int, commentList: ArrayList<Comment>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mCommentList.clear()
                            mCommentAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mCommentList.size
                        mCommentList.addAll(insertIndex, commentList)
                        mCommentAdapter.notifyItemRangeInserted(insertIndex, commentList.size)
                        mRecyclerComment.scrollToPosition(insertIndex)
                    }
                    false -> {
                        val insertIndex = mCommentList.size
                        mCommentList.addAll(insertIndex, commentList)
                        createAdapter()
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
                        mCommentList.clear()
                        mCommentAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = R.string.empty_comment.toString()
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
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

    private fun addComment() {
        val addCommentRequest = AddCommentRequest(mContext, reviewID, mComment)
        addCommentRequest.sendRequest(object : AddCommentRequest.Callback {
            override fun onSuccess(comment: Comment) {
                val insertIndex = mCommentList.size
                mCommentList.add(insertIndex, comment)
                if (!mIsLoadFirstTimeSuccess) {
                    createAdapter()
                    mIsLoadFirstTimeSuccess = true
                }
                mCommentAdapter.notifyItemInserted(insertIndex)
                mRecyclerComment.scrollToPosition(insertIndex)
                mTextMessage.visibility = View.GONE
                mInputComment.text.clear()
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
        deleteCommentRequest.sendRequest(object : DeleteCommentRequest.Callback {
            override fun onSuccess() {
                mCommentList.removeAt(position)
                mCommentAdapter.notifyItemRemoved(position)
                if (mCommentList.isEmpty()) {
                    mTextMessage.visibility = View.VISIBLE
                }
            }

            override fun onError(message: String) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
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