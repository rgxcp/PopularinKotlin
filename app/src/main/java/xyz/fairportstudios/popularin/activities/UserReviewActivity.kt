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
import xyz.fairportstudios.popularin.adapters.UserReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.UserReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarRecyclerBinding
import xyz.fairportstudios.popularin.interfaces.LikeReviewRequestCallback
import xyz.fairportstudios.popularin.interfaces.UnlikeReviewRequestCallback
import xyz.fairportstudios.popularin.interfaces.UserReviewAdapterClickListener
import xyz.fairportstudios.popularin.interfaces.UserReviewRequestCallback
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.UserReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class UserReviewActivity : AppCompatActivity(), UserReviewAdapterClickListener {
    // Primitive
    private var mIsAuth = false
    private var mIsSelf = false
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0
    private var mTotalLike = 0

    // Member
    private lateinit var mUserReviewList: ArrayList<UserReview>
    private lateinit var mContext: Context
    private lateinit var mUserReviewAdapter: UserReviewAdapter
    private lateinit var mUserReviewRequest: UserReviewRequest

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
        mIsAuth = auth.isAuth()
        mIsSelf = auth.isSelf(userID, auth.getAuthID())

        // Handler
        val handler = Handler()

        // Toolbar
        mBinding.toolbarTitle = getString(R.string.review)

        // Mendapatkan data awal
        mUserReviewRequest = UserReviewRequest(mContext, userID)
        mBinding.isLoading = true
        getUserReview(mStartPage, false)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.isLoadingMore = true
                    handler.postDelayed({
                        getUserReview(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getUserReview(mStartPage, true)
        }
    }

    override fun onUserReviewItemClick(position: Int) {
        val currentItem = mUserReviewList[position]
        gotoReviewDetail(currentItem.id)
    }

    override fun onUserReviewFilmPosterClick(position: Int) {
        val currentItem = mUserReviewList[position]
        gotoFilmDetail(currentItem.tmdbID)
    }

    override fun onUserReviewFilmPosterLongClick(position: Int) {
        val currentItem = mUserReviewList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.tmdbID, currentItem.title, year, currentItem.poster)
    }

    override fun onUserReviewNSFWBannerClick(position: Int) {
        hideNSFWBanner(position)
    }

    override fun onUserReviewLikeClick(position: Int) {
        when (mIsAuth) {
            true -> {
                val currentItem = mUserReviewList[position]
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

    override fun onUserReviewCommentClick(position: Int) {
        val currentItem = mUserReviewList[position]
        gotoReviewComment(currentItem.id)
    }

    override fun onUserReviewReportClick(position: Int) {
        gotoReviewReportedBy(mUserReviewList[position].id)
    }

    private fun getUserReview(page: Int, refreshPage: Boolean) {
        mUserReviewRequest.sendRequest(page, object : UserReviewRequestCallback {
            override fun onSuccess(totalPage: Int, userReviewList: ArrayList<UserReview>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mUserReviewList.clear()
                            mUserReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mUserReviewList.size
                        mUserReviewList.addAll(insertIndex, userReviewList)
                        mUserReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mUserReviewAdapter.notifyItemRangeInserted(insertIndex, userReviewList.size)
                        mBinding.isLoadingMore = false
                    }
                    false -> {
                        mUserReviewList = ArrayList()
                        val insertIndex = mUserReviewList.size
                        mUserReviewList.addAll(insertIndex, userReviewList)
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
                        mUserReviewList.clear()
                        mUserReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.isLoading = false
                }
                mBinding.loadSuccess = false
                mBinding.message = when (mIsSelf) {
                    true -> getString(R.string.empty_self_review)
                    false -> getString(R.string.empty_user_review)
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
        mUserReviewAdapter = UserReviewAdapter(mUserReviewList, this)
        mBinding.recyclerView.adapter = mUserReviewAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
    }

    private fun likeReview(id: Int, position: Int) {
        val likeReviewRequest = LikeReviewRequest(mContext, id)
        likeReviewRequest.sendRequest(object : LikeReviewRequestCallback {
            override fun onSuccess() {
                mTotalLike++
                val currentItem = mUserReviewList[position]
                currentItem.isLiked = true
                currentItem.totalLike = mTotalLike
                mUserReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun unlikeReview(id: Int, position: Int) {
        val unlikeReviewRequest = UnlikeReviewRequest(mContext, id)
        unlikeReviewRequest.sendRequest(object : UnlikeReviewRequestCallback {
            override fun onSuccess() {
                mTotalLike--
                val currentItem = mUserReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mUserReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(supportFragmentManager, Popularin.FILM_MODAL)
    }

    private fun hideNSFWBanner(position: Int) {
        mUserReviewList[position].isNSFW = false
        mUserReviewAdapter.notifyItemChanged(position)
    }

    private fun gotoReviewDetail(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, mIsSelf)
        startActivity(intent)
    }

    private fun gotoReviewComment(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, mIsSelf)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, 1)
        startActivity(intent)
    }

    private fun gotoReviewReportedBy(id: Int) {
        val intent = Intent(mContext, ReviewReportedByActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        startActivity(intent)
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}