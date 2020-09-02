package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.adapters.FilmGridAdapter
import xyz.fairportstudios.popularin.apis.tmdb.get.DiscoverFilmRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarRecyclerBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class DiscoverFilmActivity : AppCompatActivity(), FilmGridAdapter.OnClickListener {
    // Primitive
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0

    // Member
    private lateinit var mFilmList: ArrayList<Film>
    private lateinit var mContext: Context
    private lateinit var mDiscoverFilmRequest: DiscoverFilmRequest
    private lateinit var mFilmGridAdapter: FilmGridAdapter

    // View binding
    private lateinit var mViewBinding: ReusableToolbarRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ReusableToolbarRecyclerBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        // Context
        mContext = this

        // Extra
        val genreID = intent.getIntExtra(Popularin.GENRE_ID, 0)
        val genreTitle = intent.getStringExtra(Popularin.GENRE_TITLE)

        // Handler
        val handler = Handler()

        // Toolbar
        mViewBinding.toolbar.title = genreTitle

        // Mendapatkan data awal
        mDiscoverFilmRequest = DiscoverFilmRequest(mContext, genreID)
        discoverFilm(mStartPage, false)

        // Activity
        mViewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mViewBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mViewBinding.loadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        discoverFilm(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mViewBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mViewBinding.swipeRefresh.isRefreshing = true
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
                            mTotalPage = totalPage
                            mFilmList.clear()
                            mFilmGridAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmGridAdapter.notifyItemRangeInserted(insertIndex, filmList.size)
                    }
                    false -> {
                        mFilmList = ArrayList()
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        setAdapter()
                        mViewBinding.progressBar.visibility = View.GONE
                        mViewBinding.errorMessage.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mCurrentPage++
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mViewBinding.loadMoreBar.visibility = View.GONE
                        Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                    false -> {
                        mViewBinding.progressBar.visibility = View.GONE
                        mViewBinding.errorMessage.visibility = View.VISIBLE
                        mViewBinding.errorMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mViewBinding.swipeRefresh.isRefreshing = false
        mViewBinding.loadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun setAdapter() {
        mFilmGridAdapter = FilmGridAdapter(mContext, mFilmList, this)
        mViewBinding.recyclerView.adapter = mFilmGridAdapter
        mViewBinding.recyclerView.layoutManager = GridLayoutManager(mContext, 4)
        mViewBinding.recyclerView.visibility = View.VISIBLE
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