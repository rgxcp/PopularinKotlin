package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.FilmAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.UserFavoriteRequest
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class UserFavoriteActivity : AppCompatActivity(), FilmAdapter.OnClickListener {
    // Variable untuk fitur load more
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false
    private val mStartPage: Int = 1
    private var mCurrentPage: Int = 1
    private var mTotalPage: Int = 0

    // Variable member
    private var mIsSelf: Boolean = false
    private lateinit var mContext: Context
    private lateinit var mFilmList: ArrayList<Film>
    private lateinit var mFilmAdapter: FilmAdapter
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerFilm: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView
    private lateinit var mUserFavoriteRequest: UserFavoriteRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_toolbar_recycler)

        // Context
        mContext = this

        // Extra
        val intent = intent
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)

        // Auth
        mIsSelf = userID == Auth(mContext).getAuthID()

        // Binding
        mProgressBar = findViewById(R.id.pbr_rtr_layout)
        mRecyclerFilm = findViewById(R.id.recycler_rtr_layout)
        mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
        mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
        mTextMessage = findViewById(R.id.text_aud_message)
        val toolbar: Toolbar = findViewById(R.id.toolbar_rtr_layout)

        // Toolbar
        toolbar.title = R.string.favorite.toString()

        // Mendapatkan data awal
        mUserFavoriteRequest = UserFavoriteRequest(mContext, userID)
        getUserFavorite(mStartPage, false)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }

        mRecyclerFilm.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsLoading && mCurrentPage <= mTotalPage) {
                        mIsLoading = true
                        mSwipeRefresh.isRefreshing = true
                        getUserFavorite(mCurrentPage, false)
                    }
                }
            }
        })

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getUserFavorite(mStartPage, true)
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

    private fun getUserFavorite(page: Int, refreshPage: Boolean) {
        mUserFavoriteRequest.sendRequest(page, object : UserFavoriteRequest.Callback {
            override fun onSuccess(totalPage: Int, filmList: ArrayList<Film>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mFilmList.clear()
                            mFilmAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmAdapter.notifyItemRangeInserted(insertIndex, filmList.size)
                        mRecyclerFilm.scrollToPosition(insertIndex)
                    }
                    false -> {
                        mFilmList = ArrayList()
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmAdapter = FilmAdapter(mContext, mFilmList, this@UserFavoriteActivity)
                        mRecyclerFilm.adapter = mFilmAdapter
                        mRecyclerFilm.layoutManager = LinearLayoutManager(mContext)
                        mRecyclerFilm.visibility = View.VISIBLE
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
                        mFilmList.clear()
                        mFilmAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = when (mIsSelf) {
                    true -> R.string.empty_self_favorite_film.toString()
                    false -> R.string.empty_user_favorite_film.toString()
                }
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
        mIsLoading = false
        mSwipeRefresh.isRefreshing = false
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(supportFragmentManager, Popularin.FILM_MODAL)
    }
}