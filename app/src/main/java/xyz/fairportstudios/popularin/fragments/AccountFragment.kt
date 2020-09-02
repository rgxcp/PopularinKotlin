package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.activities.EditProfileActivity
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.activities.MainActivity
import xyz.fairportstudios.popularin.activities.ReviewActivity
import xyz.fairportstudios.popularin.activities.SocialActivity
import xyz.fairportstudios.popularin.activities.UserFavoriteActivity
import xyz.fairportstudios.popularin.activities.UserReviewActivity
import xyz.fairportstudios.popularin.activities.UserWatchlistActivity
import xyz.fairportstudios.popularin.adapters.RecentFavoriteAdapter
import xyz.fairportstudios.popularin.adapters.RecentReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.AccountDetailRequest
import xyz.fairportstudios.popularin.databinding.FragmentAccountBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.AccountDetail
import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class AccountFragment : Fragment(), RecentFavoriteAdapter.OnClickListener, RecentReviewAdapter.OnClickListener {
    // Primitive
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mRecentFavoriteList: ArrayList<RecentFavorite>
    private lateinit var mRecentReviewList: ArrayList<RecentReview>
    private lateinit var mContext: Context

    // View binding
    private var _mViewBinding: FragmentAccountBinding? = null
    private val mViewBinding get() = _mViewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mViewBinding = FragmentAccountBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Auth
        val auth = Auth(mContext)
        val authID = auth.getAuthID()

        // Mendapatkan data
        getAccountDetail(authID)

        // Activity
        mViewBinding.totalReviewLayout.setOnClickListener { gotoAccountReview(authID) }

        mViewBinding.totalFavoriteLayout.setOnClickListener { gotoAccountFavorite(authID) }

        mViewBinding.totalWatchlistLayout.setOnClickListener { gotoAccountWatchlist(authID) }

        mViewBinding.totalFollowerLayout.setOnClickListener { gotoAccountSocial(authID, 0) }

        mViewBinding.totalFollowingLayout.setOnClickListener { gotoAccountSocial(authID, 1) }

        mViewBinding.editProfileButton.setOnClickListener { gotoEditProfile() }

        mViewBinding.signOutButton.setOnClickListener {
            auth.delAuth()
            signOut()
        }

        mViewBinding.swipeRefresh.setOnRefreshListener {
            mViewBinding.swipeRefresh.isRefreshing = true
            getAccountDetail(authID)
        }

        return mViewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mViewBinding = null
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
                mViewBinding.fullName.text = accountDetail.fullName
                mViewBinding.username.text = String.format("@%s", accountDetail.username)
                mViewBinding.totalReview.text = accountDetail.totalReview.toString()
                mViewBinding.totalFavorite.text = accountDetail.totalFavorite.toString()
                mViewBinding.totalWatchlist.text = accountDetail.totalWatchlist.toString()
                mViewBinding.totalFollower.text = accountDetail.totalFollower.toString()
                mViewBinding.totalFollowing.text = accountDetail.totalFollowing.toString()
                Glide.with(mContext).load(accountDetail.profilePicture).into(mViewBinding.userProfile)
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

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(requireFragmentManager(), Popularin.FILM_MODAL)
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