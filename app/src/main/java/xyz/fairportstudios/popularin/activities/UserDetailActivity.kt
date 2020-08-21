package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.RecentFavoriteAdapter
import xyz.fairportstudios.popularin.adapters.RecentReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnfollowUserRequest
import xyz.fairportstudios.popularin.apis.popularin.get.UserDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.post.FollowUserRequest
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

    // View
    private lateinit var mButtonFollow: Button
    private lateinit var mImageProfile: ImageView
    private lateinit var mImageEmptyRecentFavorite: ImageView
    private lateinit var mImageEmptyRecentReview: ImageView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerRecentFavorite: RecyclerView
    private lateinit var mRecyclerRecentReview: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mScrollView: ScrollView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextFullName: TextView
    private lateinit var mTextUsername: TextView
    private lateinit var mTextFollowMe: TextView
    private lateinit var mTextTotalReview: TextView
    private lateinit var mTextTotalFavorite: TextView
    private lateinit var mTextTotalWatchlist: TextView
    private lateinit var mTextTotalFollower: TextView
    private lateinit var mTextTotalFollowing: TextView
    private lateinit var mTextMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        // Context
        mContext = this

        // Binding
        mButtonFollow = findViewById(R.id.button_aud_follow)
        mImageProfile = findViewById(R.id.image_aud_profile)
        mImageEmptyRecentFavorite = findViewById(R.id.image_aud_empty_recent_favorite)
        mImageEmptyRecentReview = findViewById(R.id.image_aud_empty_recent_review)
        mProgressBar = findViewById(R.id.pbr_aud_layout)
        mRecyclerRecentFavorite = findViewById(R.id.recycler_aud_recent_favorite)
        mRecyclerRecentReview = findViewById(R.id.recycler_aud_recent_review)
        mAnchorLayout = findViewById(R.id.anchor_aud_layout)
        mScrollView = findViewById(R.id.scroll_aud_layout)
        mSwipeRefresh = findViewById(R.id.swipe_refresh_aud_layout)
        mTextFullName = findViewById(R.id.text_aud_full_name)
        mTextUsername = findViewById(R.id.text_aud_username)
        mTextFollowMe = findViewById(R.id.text_aud_follow_me)
        mTextTotalReview = findViewById(R.id.text_aud_total_review)
        mTextTotalFavorite = findViewById(R.id.text_aud_total_favorite)
        mTextTotalWatchlist = findViewById(R.id.text_aud_total_watchlist)
        mTextTotalFollower = findViewById(R.id.text_aud_total_follower)
        mTextTotalFollowing = findViewById(R.id.text_aud_total_following)
        mTextMessage = findViewById(R.id.text_aud_message)
        val totalReviewLayout = findViewById<LinearLayout>(R.id.layout_aud_total_review)
        val totalFavoriteLayout = findViewById<LinearLayout>(R.id.layout_aud_total_favorite)
        val totalWatchlistLayout = findViewById<LinearLayout>(R.id.layout_aud_total_watchlist)
        val totalFollowerLayout = findViewById<LinearLayout>(R.id.layout_aud_total_follower)
        val totalFollowingLayout = findViewById<LinearLayout>(R.id.layout_aud_total_following)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_aud_layout)

        // Extra
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)

        // Auth
        val auth = Auth(mContext)
        val isAuth = auth.isAuth()
        mIsSelf = auth.isSelf(userID, auth.getAuthID())
        if (mIsSelf) mButtonFollow.text = getString(R.string.edit_profile)

        // Mendapatkan data
        getUserDetail(userID)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }

        totalReviewLayout.setOnClickListener { gotoUserReview(userID) }

        totalFavoriteLayout.setOnClickListener { gotoUserFavorite(userID) }

        totalWatchlistLayout.setOnClickListener { gotoUserWatchlist(userID) }

        totalFollowerLayout.setOnClickListener { gotoUserSocial(userID, 0) }

        totalFollowingLayout.setOnClickListener { gotoUserSocial(userID, 1) }

        mButtonFollow.setOnClickListener {
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

        mSwipeRefresh.setOnRefreshListener {
            mSwipeRefresh.isRefreshing = true
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
                mTextFollowMe.visibility = when (mIsFollower) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
                if (mIsFollowing) mButtonFollow.text = getString(R.string.following)

                // Isi
                mTotalFollower = userDetail.totalFollower
                mTextFullName.text = userDetail.fullName
                mTextUsername.text = String.format("@%s", userDetail.username)
                mTextTotalReview.text = userDetail.totalReview.toString()
                mTextTotalFavorite.text = userDetail.totalFavorite.toString()
                mTextTotalWatchlist.text = userDetail.totalWatchlist.toString()
                mTextTotalFollower.text = mTotalFollower.toString()
                mTextTotalFollowing.text = userDetail.totalFollowing.toString()
                Glide.with(mContext).load(userDetail.profilePicture).into(mImageProfile)
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.GONE
                mScrollView.visibility = View.VISIBLE
                mIsLoadFirstTimeSuccess = true
            }

            override fun onHasRecentFavorite(recentFavoriteList: ArrayList<RecentFavorite>) {
                mRecentFavoriteList = ArrayList()
                mRecentFavoriteList.addAll(recentFavoriteList)
                setRecentFavoriteAdapter()
                mImageEmptyRecentFavorite.visibility = View.GONE
            }

            override fun onHasRecentReview(recentReviewList: ArrayList<RecentReview>) {
                mRecentReviewList = ArrayList()
                mRecentReviewList.addAll(recentReviewList)
                setRecentReviewAdapter()
                mImageEmptyRecentReview.visibility = View.GONE
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mProgressBar.visibility = View.GONE
                        mTextMessage.visibility = View.VISIBLE
                        mTextMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mSwipeRefresh.isRefreshing = false
    }

    private fun setRecentFavoriteAdapter() {
        val recentFavoriteAdapter = RecentFavoriteAdapter(mContext, mRecentFavoriteList, this)
        mRecyclerRecentFavorite.adapter = recentFavoriteAdapter
        mRecyclerRecentFavorite.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mRecyclerRecentFavorite.hasFixedSize()
        mRecyclerRecentFavorite.visibility = View.VISIBLE
    }

    private fun setRecentReviewAdapter() {
        val recentReviewAdapter = RecentReviewAdapter(mContext, mRecentReviewList, this)
        mRecyclerRecentReview.adapter = recentReviewAdapter
        mRecyclerRecentReview.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mRecyclerRecentReview.hasFixedSize()
        mRecyclerRecentReview.visibility = View.VISIBLE
    }

    private enum class FollowingState {
        FOLLOWING, NOT_FOLLOWING, LOADING
    }

    private fun setFollowingState(state: Boolean) {
        mIsFollowing = state
        when (mIsFollowing) {
            true -> mTotalFollower++
            false -> mTotalFollower--
        }
        mTextTotalFollower.text = mTotalFollower.toString()
    }

    private fun setFollowButtonState(state: Boolean, followingStateEnum: Enum<FollowingState>) {
        mButtonFollow.isEnabled = state
        when (followingStateEnum) {
            FollowingState.FOLLOWING -> mButtonFollow.text = getString(R.string.following)
            FollowingState.NOT_FOLLOWING -> mButtonFollow.text = getString(R.string.follow)
            else -> mButtonFollow.text = getString(R.string.loading)
        }
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
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
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
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
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