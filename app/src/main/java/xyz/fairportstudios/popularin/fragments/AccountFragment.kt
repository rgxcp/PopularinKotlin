package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.*
import xyz.fairportstudios.popularin.adapters.RecentFavoriteAdapter
import xyz.fairportstudios.popularin.adapters.RecentReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.AccountDetailRequest
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.AccountDetail
import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class AccountFragment : Fragment(), RecentFavoriteAdapter.OnClickListener, RecentReviewAdapter.OnClickListener {
    // Variable untuk fitur load
    private var mIsLoadFirstTimeSuccess: Boolean = false

    // Variable member
    private lateinit var mContext: Context
    private lateinit var mRecentFavoriteList: ArrayList<RecentFavorite>
    private lateinit var mRecentReviewList: ArrayList<RecentReview>
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mImageProfile: ImageView
    private lateinit var mImageEmptyRecentFavorite: ImageView
    private lateinit var mImageEmptyRecentReview: ImageView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerRecentFavorite: RecyclerView
    private lateinit var mRecyclerRecentReview: RecyclerView
    private lateinit var mScrollView: ScrollView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextFullName: TextView
    private lateinit var mTextUsername: TextView
    private lateinit var mTextTotalReview: TextView
    private lateinit var mTextTotalFavorite: TextView
    private lateinit var mTextTotalWatchlist: TextView
    private lateinit var mTextTotalFollower: TextView
    private lateinit var mTextTotalFollowing: TextView
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_account, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mAnchorLayout = view.findViewById(R.id.anchor_fa_layout)
        mImageProfile = view.findViewById(R.id.image_fa_profile)
        mImageEmptyRecentFavorite = view.findViewById(R.id.image_fa_empty_recent_favorite)
        mImageEmptyRecentReview = view.findViewById(R.id.image_fa_empty_recent_review)
        mProgressBar = view.findViewById(R.id.pbr_fa_layout)
        mRecyclerRecentFavorite = view.findViewById(R.id.recycler_fa_recent_favorite)
        mRecyclerRecentReview = view.findViewById(R.id.recycler_fa_recent_review)
        mScrollView = view.findViewById(R.id.scroll_fa_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_fa_layout)
        mTextFullName = view.findViewById(R.id.text_fa_full_name)
        mTextUsername = view.findViewById(R.id.text_fa_username)
        mTextTotalReview = view.findViewById(R.id.text_fa_total_review)
        mTextTotalFavorite = view.findViewById(R.id.text_fa_total_favorite)
        mTextTotalWatchlist = view.findViewById(R.id.text_fa_total_watchlist)
        mTextTotalFollower = view.findViewById(R.id.text_fa_total_follower)
        mTextTotalFollowing = view.findViewById(R.id.text_fa_total_following)
        mTextMessage = view.findViewById(R.id.text_fa_message)
        val buttonEditProfile: Button = view.findViewById(R.id.button_fa_edit_profile)
        val buttonSignOut: Button = view.findViewById(R.id.button_fa_sign_out)
        val totalReviewLayout: LinearLayout = view.findViewById(R.id.layout_fa_total_review)
        val totalFavoriteLayout: LinearLayout = view.findViewById(R.id.layout_fa_total_favorite)
        val totalWatchlistLayout: LinearLayout = view.findViewById(R.id.layout_fa_total_watchlist)
        val totalFollowerLayout: LinearLayout = view.findViewById(R.id.layout_fa_total_follower)
        val totalFollowingLayout: LinearLayout = view.findViewById(R.id.layout_fa_total_following)

        // Auth
        val auth = Auth(mContext)
        val authID = auth.getAuthID()

        // Mendapatkan data
        getAccountDetail(authID)

        // Activity
        totalReviewLayout.setOnClickListener { gotoAccountReview(authID) }

        totalFavoriteLayout.setOnClickListener { gotoAccountFavorite(authID) }

        totalWatchlistLayout.setOnClickListener { gotoAccountWatchlist(authID) }

        totalFollowerLayout.setOnClickListener { gotoAccountSocial(authID, 0) }

        totalFollowingLayout.setOnClickListener { gotoAccountSocial(authID, 1) }

        buttonEditProfile.setOnClickListener { gotoEditProfile() }

        buttonSignOut.setOnClickListener {
            auth.delAuh()
            signOut()
        }

        mSwipeRefresh.setOnRefreshListener {
            mSwipeRefresh.isRefreshing = true
            getAccountDetail(authID)
        }

        return view
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

    private fun getAccountDetail(id: Int) {
        val accountDetailRequest = AccountDetailRequest(mContext, id)
        accountDetailRequest.sendRequest(object : AccountDetailRequest.Callback {
            override fun onSuccess(accountDetail: AccountDetail) {
                mTextFullName.text = accountDetail.fullName
                mTextUsername.text = String.format("@%s", accountDetail.username)
                mTextTotalReview.text = accountDetail.totalReview.toString()
                mTextTotalFavorite.text = accountDetail.totalFavorite.toString()
                mTextTotalWatchlist.text = accountDetail.totalWatchlist.toString()
                mTextTotalFollower.text = accountDetail.totalFollower.toString()
                mTextTotalFollowing.text = accountDetail.totalFollowing.toString()
                Glide.with(mContext).load(accountDetail.profilePicture).into(mImageProfile)
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.GONE
                mScrollView.visibility = View.VISIBLE
                mIsLoadFirstTimeSuccess = true
            }

            override fun onHasRecentFavorite(recentFavoriteList: ArrayList<RecentFavorite>) {
                mRecentFavoriteList = ArrayList()
                mRecentFavoriteList.addAll(recentFavoriteList)
                val recentFavoriteAdapter = RecentFavoriteAdapter(mContext, mRecentFavoriteList, this@AccountFragment)
                mRecyclerRecentFavorite.adapter = recentFavoriteAdapter
                mRecyclerRecentFavorite.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
                mRecyclerRecentFavorite.hasFixedSize()
                mRecyclerRecentFavorite.visibility = View.VISIBLE
                mImageEmptyRecentFavorite.visibility = View.GONE
            }

            override fun onHasRecentReview(recentReviewList: ArrayList<RecentReview>) {
                mRecentReviewList = ArrayList()
                mRecentReviewList.addAll(recentReviewList)
                val recentReviewAdapter = RecentReviewAdapter(mContext, mRecentReviewList, this@AccountFragment)
                mRecyclerRecentReview.adapter = recentReviewAdapter
                mRecyclerRecentReview.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
                mRecyclerRecentReview.hasFixedSize()
                mRecyclerRecentReview.visibility = View.VISIBLE
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

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoReviewDetail(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, true)
        startActivity(intent)
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val fragmentManager = (mContext as FragmentActivity).supportFragmentManager
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(fragmentManager, Popularin.FILM_MODAL)
    }

    private fun gotoAccountReview(id: Int) {
        val intent = Intent(mContext, UserReviewActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoAccountFavorite(id: Int) {
        val intent = Intent(mContext, UserFavoriteActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoAccountWatchlist(id: Int) {
        val intent = Intent(mContext, UserWatchlistActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoAccountSocial(id: Int, viewPagerIndex: Int) {
        val intent = Intent(mContext, SocialActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, viewPagerIndex)
        startActivity(intent)
    }

    private fun gotoEditProfile() {
        val intent = Intent(mContext, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun signOut() {
        val intent = Intent(mContext, MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}