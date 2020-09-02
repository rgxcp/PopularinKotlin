package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
    private var mIsSelf = false
    private var mIsFollower = false
    private var mIsFollowing = false
    private var mTotalFollower = 0

    // Member
    private lateinit var mRecentFavoriteList: ArrayList<RecentFavorite>
    private lateinit var mRecentReviewList: ArrayList<RecentReview>
    private lateinit var mContext: Context

    // View binding
    private lateinit var mViewBinding: ActivityUserDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        // Context
        mContext = this

        // Extra
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)

        // Auth
        val auth = Auth(mContext)
        val isAuth = auth.isAuth()
        mIsSelf = auth.isSelf(userID, auth.getAuthID())
        if (mIsSelf) mViewBinding.followButton.text = getString(R.string.edit_profile)

        // Mendapatkan data
        getUserDetail(userID)

        // Activity
        mViewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mViewBinding.totalReviewLayout.setOnClickListener { gotoUserReview(userID) }

        mViewBinding.totalFavoriteLayout.setOnClickListener { gotoUserFavorite(userID) }

        mViewBinding.totalWatchlistLayout.setOnClickListener { gotoUserWatchlist(userID) }

        mViewBinding.totalFollowerLayout.setOnClickListener { gotoUserSocial(userID, 0) }

        mViewBinding.totalFollowingLayout.setOnClickListener { gotoUserSocial(userID, 1) }

        mViewBinding.followButton.setOnClickListener {
            when {
                isAuth && !mIsSelf -> {
                    when (mIsFollowing) {
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

        mViewBinding.swipeRefresh.setOnRefreshListener {
            mViewBinding.swipeRefresh.isRefreshing = true
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
                // Following status
                mIsFollower = userDetail.isFollower
                mIsFollowing = userDetail.isFollowing
                mViewBinding.followMe.visibility = when (mIsFollower) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
                if (mIsFollowing) mViewBinding.followButton.text = getString(R.string.following)

                // Isi
                mTotalFollower = userDetail.totalFollower
                mViewBinding.fullName.text = userDetail.fullName
                mViewBinding.username.text = String.format("@%s", userDetail.username)
                mViewBinding.totalReview.text = userDetail.totalReview.toString()
                mViewBinding.totalFavorite.text = userDetail.totalFavorite.toString()
                mViewBinding.totalWatchlist.text = userDetail.totalWatchlist.toString()
                mViewBinding.totalFollower.text = mTotalFollower.toString()
                mViewBinding.totalFollowing.text = userDetail.totalFollowing.toString()
                Glide.with(mContext).load(userDetail.profilePicture).into(mViewBinding.userProfile)
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.errorMessage.visibility = View.GONE
                mViewBinding.scrollView.visibility = View.VISIBLE
                mIsLoadFirstTimeSuccess = true
            }

            override fun onHasRecentFavorite(recentFavoriteList: ArrayList<RecentFavorite>) {
                mRecentFavoriteList = ArrayList()
                mRecentFavoriteList.addAll(recentFavoriteList)
                setRecentFavoriteAdapter()
                mViewBinding.emptyRecentFavoriteImage.visibility = View.GONE
            }

            override fun onHasRecentReview(recentReviewList: ArrayList<RecentReview>) {
                mRecentReviewList = ArrayList()
                mRecentReviewList.addAll(recentReviewList)
                setRecentReviewAdapter()
                mViewBinding.emptyRecentReviewImage.visibility = View.GONE
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mViewBinding.progressBar.visibility = View.GONE
                        mViewBinding.errorMessage.visibility = View.VISIBLE
                        mViewBinding.errorMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mViewBinding.swipeRefresh.isRefreshing = false
    }

    private fun setRecentFavoriteAdapter() {
        val recentFavoriteAdapter = RecentFavoriteAdapter(mContext, mRecentFavoriteList, this)
        mViewBinding.recyclerViewRecentFavorite.adapter = recentFavoriteAdapter
        mViewBinding.recyclerViewRecentFavorite.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mViewBinding.recyclerViewRecentFavorite.hasFixedSize()
        mViewBinding.recyclerViewRecentFavorite.visibility = View.VISIBLE
    }

    private fun setRecentReviewAdapter() {
        val recentReviewAdapter = RecentReviewAdapter(mContext, mRecentReviewList, this)
        mViewBinding.recyclerViewRecentReview.adapter = recentReviewAdapter
        mViewBinding.recyclerViewRecentReview.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mViewBinding.recyclerViewRecentReview.hasFixedSize()
        mViewBinding.recyclerViewRecentReview.visibility = View.VISIBLE
    }

    private fun setFollowingState(state: Boolean) {
        mIsFollowing = state
        when (mIsFollowing) {
            true -> mTotalFollower++
            false -> mTotalFollower--
        }
        mViewBinding.totalFollower.text = mTotalFollower.toString()
    }

    private fun setFollowButtonState(state: Boolean, followingStateEnum: Enum<FollowingState>) {
        mViewBinding.followButton.isEnabled = state
        when (followingStateEnum) {
            FollowingState.FOLLOWING -> mViewBinding.followButton.text = getString(R.string.following)
            FollowingState.NOT_FOLLOWING -> mViewBinding.followButton.text = getString(R.string.follow)
            else -> mViewBinding.followButton.text = getString(R.string.loading)
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
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
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
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
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
        intent.putExtra(Popularin.IS_SELF, mIsSelf)
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