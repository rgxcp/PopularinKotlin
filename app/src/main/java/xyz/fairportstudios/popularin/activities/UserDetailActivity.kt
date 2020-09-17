package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.RecentFavoriteAdapter
import xyz.fairportstudios.popularin.adapters.RecentReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnfollowUserRequest
import xyz.fairportstudios.popularin.apis.popularin.get.UserDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.post.FollowUserRequest
import xyz.fairportstudios.popularin.databinding.ActivityUserDetailBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.models.UserDetail
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class UserDetailActivity : AppCompatActivity(), RecentFavoriteAdapter.OnClickListener, RecentReviewAdapter.OnClickListener {
    // Primitive
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mRecentFavoriteList: ArrayList<RecentFavorite>
    private lateinit var mRecentReviewList: ArrayList<RecentReview>
    private lateinit var mContext: Context
    private lateinit var mUserDetail: UserDetail

    // Binding
    private lateinit var mBinding: ActivityUserDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Context
        mContext = this

        // Extra
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)

        // Auth
        val isAuth = Auth(mContext).isAuth()

        // Mendapatkan data
        mBinding.isLoading = true
        getUserDetail(userID)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mBinding.totalReviewLayout.setOnClickListener { gotoUserReview(userID) }

        mBinding.totalFavoriteLayout.setOnClickListener { gotoUserFavorite(userID) }

        mBinding.totalWatchlistLayout.setOnClickListener { gotoUserWatchlist(userID) }

        mBinding.totalFollowerLayout.setOnClickListener { gotoUserSocial(userID, 0) }

        mBinding.totalFollowingLayout.setOnClickListener { gotoUserSocial(userID, 1) }

        mBinding.followButton.setOnClickListener {
            when {
                isAuth && !mUserDetail.isSelf -> {
                    when (mUserDetail.isFollowing) {
                        true -> {
                            setFollowButtonState(false, FollowingState.LOADING)
                            unfollowUser(userID)
                        }
                        false -> {
                            setFollowButtonState(false, FollowingState.LOADING)
                            followUser(userID)
                        }
                    }
                }
                isAuth -> gotoEditProfile()
                else -> gotoEmptyAccount()
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mBinding.swipeRefresh.isRefreshing = true
            getUserDetail(userID)
        }
    }

    override fun onRecentFavoriteItemClick(position: Int) {
        val currentItem = mRecentFavoriteList[position]
        gotoFilmDetail(currentItem.tmdbID)
    }

    override fun onRecentFavoriteItemLongClick(position: Int) {
        val currentItem = mRecentFavoriteList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.tmdbID, currentItem.title, year, currentItem.poster)
    }

    override fun onRecentReviewItemClick(position: Int) {
        val currentItem = mRecentReviewList[position]
        gotoReviewDetail(currentItem.id)
    }

    override fun onRecentReviewItemLongClick(position: Int) {
        val currentItem = mRecentReviewList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.tmdbID, currentItem.title, year, currentItem.poster)
    }

    private fun getUserDetail(id: Int) {
        val userDetailRequest = UserDetailRequest(mContext, id)
        userDetailRequest.sendRequest(object : UserDetailRequest.Callback {
            override fun onSuccess(userDetail: UserDetail) {
                mUserDetail = userDetail
                mBinding.userDetail = mUserDetail
                mBinding.isLoading = false
                mBinding.loadSuccess = true
                mIsLoadFirstTimeSuccess = true
                if (mUserDetail.isSelf) mBinding.followButton.text = getString(R.string.edit_profile)
            }

            override fun onHasRecentFavorite(recentFavoriteList: ArrayList<RecentFavorite>) {
                mRecentFavoriteList = ArrayList()
                mRecentFavoriteList.addAll(recentFavoriteList)
                setRecentFavoriteAdapter()
            }

            override fun onHasRecentReview(recentReviewList: ArrayList<RecentReview>) {
                mRecentReviewList = ArrayList()
                mRecentReviewList.addAll(recentReviewList)
                setRecentReviewAdapter()
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mBinding.isLoading = false
                        mBinding.loadSuccess = false
                        mBinding.message = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mBinding.swipeRefresh.isRefreshing = false
    }

    private fun setRecentFavoriteAdapter() {
        val recentFavoriteAdapter = RecentFavoriteAdapter(mContext, mRecentFavoriteList, this)
        mBinding.recyclerViewRecentFavorite.adapter = recentFavoriteAdapter
        mBinding.recyclerViewRecentFavorite.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mBinding.recyclerViewRecentFavorite.hasFixedSize()
    }

    private fun setRecentReviewAdapter() {
        val recentReviewAdapter = RecentReviewAdapter(mContext, mRecentReviewList, this)
        mBinding.recyclerViewRecentReview.adapter = recentReviewAdapter
        mBinding.recyclerViewRecentReview.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mBinding.recyclerViewRecentReview.hasFixedSize()
    }

    private fun setFollowingState(state: Boolean) {
        mUserDetail.isFollowing = state
        when (mUserDetail.isFollowing) {
            true -> mUserDetail.totalFollower++
            false -> mUserDetail.totalFollower--
        }
        mBinding.totalFollower.text = mUserDetail.totalFollower.toString()
    }

    private fun setFollowButtonState(state: Boolean, followingStateEnum: Enum<FollowingState>) {
        mBinding.followButton.isEnabled = state
        when (followingStateEnum) {
            FollowingState.FOLLOWING -> mBinding.followButton.text = getString(R.string.following)
            FollowingState.NOT_FOLLOWING -> mBinding.followButton.text = getString(R.string.follow)
            else -> mBinding.followButton.text = getString(R.string.loading)
        }
    }

    private enum class FollowingState {
        FOLLOWING, NOT_FOLLOWING, LOADING
    }

    private fun followUser(id: Int) {
        val followUserRequest = FollowUserRequest(mContext, id)
        followUserRequest.sendRequest(object : FollowUserRequest.Callback {
            override fun onSuccess() {
                setFollowingState(true)
                setFollowButtonState(true, FollowingState.FOLLOWING)
            }

            override fun onError(message: String) {
                setFollowButtonState(true, FollowingState.NOT_FOLLOWING)
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun unfollowUser(id: Int) {
        val unfollowUserRequest = UnfollowUserRequest(mContext, id)
        unfollowUserRequest.sendRequest(object : UnfollowUserRequest.Callback {
            override fun onSuccess() {
                setFollowingState(false)
                setFollowButtonState(true, FollowingState.NOT_FOLLOWING)
            }

            override fun onError(message: String) {
                setFollowButtonState(true, FollowingState.FOLLOWING)
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(supportFragmentManager, Popularin.FILM_MODAL)
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoReviewDetail(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, mUserDetail.isSelf)
        startActivity(intent)
    }

    private fun gotoUserReview(id: Int) {
        val intent = Intent(mContext, UserReviewActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoUserFavorite(id: Int) {
        val intent = Intent(mContext, UserFavoriteActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoUserWatchlist(id: Int) {
        val intent = Intent(mContext, UserWatchlistActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoUserSocial(id: Int, viewPagerIndex: Int) {
        val intent = Intent(mContext, SocialActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, viewPagerIndex)
        startActivity(intent)
    }

    private fun gotoEditProfile() {
        val intent = Intent(mContext, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}