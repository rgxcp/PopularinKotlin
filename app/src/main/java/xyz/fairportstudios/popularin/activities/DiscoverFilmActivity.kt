package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.FilmGridAdapter
import xyz.fairportstudios.popularin.apis.tmdb.get.DiscoverFilmRequest
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class DiscoverFilmActivity : AppCompatActivity(), FilmGridAdapter.OnClickListener {
    // Variable untuk fitur load more
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false
    private val mStartPage: Int = 1
    private var mCurrentPage: Int = 1
    private var mTotalPage: Int = 0

    // Variable member
    private lateinit var mContext: Context
    private lateinit var mFilmList: ArrayList<Film>
    private lateinit var mDiscoverFilmRequest: DiscoverFilmRequest
    private lateinit var mFilmGridAdapter: FilmGridAdapter
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerFilm: RecyclerView
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_toolbar_recycler)

        // Context
        mContext = this

        // Binding
        mProgressBar = findViewById(R.id.pbr_rtr_layout)
        mRecyclerFilm = findViewById(R.id.recycler_rtr_layout)
        mAnchorLayout = findViewById(R.id.anchor_rtr_layout)
        mSwipeRefresh = findViewById(R.id.swipe_refresh_rtr_layout)
        mTextMessage = findViewById(R.id.text_aud_message)
        val toolbar: Toolbar = findViewById(R.id.toolbar_rtr_layout)

        // Extra
        val intent = intent
        val genreID = intent.getIntExtra(Popularin.GENRE_ID, 0)
        val genreTitle = intent.getStringExtra(Popularin.GENRE_TITLE)

        // Toolbar
        toolbar.title = genreTitle

        // Mendapatkan data awal
        mDiscoverFilmRequest = DiscoverFilmRequest(mContext, genreID)
        discoverFilm(mStartPage, false)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }

        mRecyclerFilm.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsLoading && mCurrentPage <= mTotalPage) {
                        mIsLoading = true
                        mSwipeRefresh.isRefreshing = true
                        discoverFilm(mCurrentPage, false)
                    }
                }
            }
        })

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            discoverFilm(mStartPage, true)
        }
    }

    override fun onFilmGridItemClick(position: Int) {
        val currentItem = mFilmList[position]
        gotoFilmDetail(currentItem.id)
    }

    override fun onFilmGridItemLongClick(position: Int) {
        val currentItem = mFilmList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.id, currentItem.originalTitle, year, currentItem.posterPath)
    }

    private fun discoverFilm(page: Int, refreshPage: Boolean) {
        mDiscoverFilmRequest.sendRequest(page, object : DiscoverFilmRequest.Callback {
            override fun onSuccess(totalPage: Int, filmList: ArrayList<Film>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mFilmList.clear()
                            mFilmGridAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmGridAdapter.notifyItemRangeInserted(insertIndex, filmList.size)
                        mRecyclerFilm.scrollToPosition(insertIndex)
                    }
                    false -> {
                        mFilmList = ArrayList()
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmGridAdapter = FilmGridAdapter(mContext, mFilmList, this@DiscoverFilmActivity)
                        mRecyclerFilm.adapter = mFilmGridAdapter
                        mRecyclerFilm.layoutManager = GridLayoutManager(mContext, 4)
                        mRecyclerFilm.visibility = View.VISIBLE
                        mProgressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mTextMessage.visibility = View.GONE
                mCurrentPage++
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