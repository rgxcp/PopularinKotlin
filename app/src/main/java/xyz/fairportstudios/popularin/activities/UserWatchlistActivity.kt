package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.FilmAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.UserWatchlistRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarRecyclerBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class UserWatchlistActivity : AppCompatActivity(), FilmAdapter.OnClickListener {
    // Primitive
    private var mIsSelf = false
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mFilmList: ArrayList<Film>
    private lateinit var mContext: Context
    private lateinit var mFilmAdapter: FilmAdapter
    private lateinit var mUserWatchlistRequest: UserWatchlistRequest

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
        mBinding.toolbar.title = getString(R.string.watchlist)

        // Mendapatkan data awal
        mUserWatchlistRequest = UserWatchlistRequest(mContext, userID)
        getUserWatchlist(mStartPage, false)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.loadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getUserWatchlist(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getUserWatchlist(mStartPage, true)
        }
    }

    override fun onFilmItemClick(position: Int) {
        val currentItem = mFilmList[position]
        gotoFilmDetail(currentItem.id)
    }

    override fun onFilmPosterClick(position: Int) {
        val currentItem = mFilmList[position]
        gotoFilmDetail(currentItem.id)
    }

    override fun onFilmPosterLongClick(position: Int) {
        val currentItem = mFilmList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.id, currentItem.originalTitle, year, currentItem.posterPath)
    }

    private fun getUserWatchlist(page: Int, refreshPage: Boolean) {
        mUserWatchlistRequest.sendRequest(page, object : UserWatchlistRequest.Callback {
            override fun onSuccess(totalPage: Int, filmList: ArrayList<Film>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mFilmList.clear()
                            mFilmAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmAdapter.notifyItemRangeInserted(insertIndex, filmList.size)
                    }
                    false -> {
                        mFilmList = ArrayList()
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        setAdapter()
                        mBinding.progressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mBinding.errorMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mFilmList.clear()
                        mFilmAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.progressBar.visibility = View.GONE
                }
                mBinding.errorMessage.visibility = View.VISIBLE
                mBinding.errorMessage.text = when (mIsSelf) {
                    true -> getString(R.string.empty_self_watchlist_film)
                    false -> getString(R.string.empty_user_watchlist)
                }
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mBinding.loadMoreBar.visibility = View.GONE
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                    false -> {
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.errorMessage.visibility = View.VISIBLE
                        mBinding.errorMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mBinding.swipeRefresh.isRefreshing = false
        mBinding.loadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun setAdapter() {
        mFilmAdapter = FilmAdapter(mContext, mFilmList, this)
        mBinding.recyclerView.adapter = mFilmAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
        mBinding.recyclerView.visibility = View.VISIBLE
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
}