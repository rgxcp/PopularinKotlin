package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.CastAdapter
import xyz.fairportstudios.popularin.adapters.CrewAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.FilmMetadataRequest
import xyz.fairportstudios.popularin.apis.tmdb.get.FilmDetailRequest
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Cast
import xyz.fairportstudios.popularin.models.Crew
import xyz.fairportstudios.popularin.models.FilmDetail
import xyz.fairportstudios.popularin.models.FilmMetadata
import xyz.fairportstudios.popularin.services.ConvertGenre
import xyz.fairportstudios.popularin.services.ConvertRuntime
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin
import xyz.fairportstudios.popularin.statics.TMDbAPI
import java.util.Locale
import kotlin.collections.ArrayList

class FilmDetailActivity : AppCompatActivity(), CastAdapter.OnClickListener, CrewAdapter.OnClickListener {
    // Primitive
    private var mGenreID = 0

    // Member
    private lateinit var mCastList: ArrayList<Cast>
    private lateinit var mCrewList: ArrayList<Crew>
    private lateinit var mContext: Context
    private lateinit var mGenreTitle: String
    private lateinit var mFilmTitle: String
    private lateinit var mFilmYear: String
    private lateinit var mFilmPoster: String
    private lateinit var mYoutubeKey: String

    // View
    private lateinit var mChipGenre: Chip
    private lateinit var mChipRuntime: Chip
    private lateinit var mChipRating: Chip
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mImagePoster: ImageView
    private lateinit var mImageEmptyOverview: ImageView
    private lateinit var mImageEmptyCast: ImageView
    private lateinit var mImageEmptyCrew: ImageView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerCast: RecyclerView
    private lateinit var mRecyclerCrew: RecyclerView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextTotalReview: TextView
    private lateinit var mTextTotalFavorite: TextView
    private lateinit var mTextTotalWatchlist: TextView
    private lateinit var mTextOverview: TextView
    private lateinit var mTextMessage: TextView
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_film_detail)

        // Context
        mContext = this

        // Binding
        mChipGenre = findViewById(R.id.chip_afd_genre)
        mChipRuntime = findViewById(R.id.chip_afd_runtime)
        mChipRating = findViewById(R.id.chip_afd_rating)
        mAnchorLayout = findViewById(R.id.anchor_afd_layout)
        mImagePoster = findViewById(R.id.image_afd_poster)
        mImageEmptyOverview = findViewById(R.id.image_afd_empty_overview)
        mImageEmptyCast = findViewById(R.id.image_afd_empty_cast)
        mImageEmptyCrew = findViewById(R.id.image_afd_empty_crew)
        mProgressBar = findViewById(R.id.pbr_afd_layout)
        mRecyclerCast = findViewById(R.id.recycler_afd_cast)
        mRecyclerCrew = findViewById(R.id.recycler_afd_crew)
        mSwipeRefresh = findViewById(R.id.swipe_refresh_afd_layout)
        mTextTotalReview = findViewById(R.id.text_afd_total_review)
        mTextTotalFavorite = findViewById(R.id.text_afd_total_favorite)
        mTextTotalWatchlist = findViewById(R.id.text_afd_total_watchlist)
        mTextOverview = findViewById(R.id.text_afd_overview)
        mTextMessage = findViewById(R.id.text_afd_message)
        mToolbar = findViewById(R.id.toolbar_afd_layout)
        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_afd_layout)
        val fab = findViewById<FloatingActionButton>(R.id.fab_afd_layout)
        val imagePlayTrailer = findViewById<ImageView>(R.id.image_afd_play)
        val imageReview = findViewById<ImageView>(R.id.image_afd_review)
        val imageFavorite = findViewById<ImageView>(R.id.image_afd_favorite)
        val imageWatchlist = findViewById<ImageView>(R.id.image_afd_watchlist)

        // Extra
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)

        // Mengatur jenis font untuk collapsing toolbar
        val typeface = ResourcesCompat.getFont(mContext, R.font.monument_extended_regular)
        collapsingToolbar.setExpandedTitleTypeface(typeface)

        // Mendapatkan detail film
        getFilmDetail(filmID)

        // Mendapatkan metadata film
        getFilmMetadata(filmID)

        // Activity
        mToolbar.setNavigationOnClickListener { onBackPressed() }

        imagePlayTrailer.setOnClickListener {
            when (mYoutubeKey.isNotEmpty()) {
                true -> playTrailer(mYoutubeKey)
                false -> searchTrailer("${mFilmTitle.toLowerCase(Locale.ROOT)} trailer")
            }
        }

        mChipGenre.setOnClickListener {
            if (mGenreID != 0) gotoDiscoverFilm(mGenreID, mGenreTitle)
        }

        imageReview.setOnClickListener { gotoFilmReview(filmID) }

        imageFavorite.setOnClickListener { gotoFavoritedBy(filmID) }

        imageWatchlist.setOnClickListener { gotoWatchlistedBy(filmID) }

        fab.setOnClickListener { showFilmModal(filmID) }

        mSwipeRefresh.setOnRefreshListener {
            mSwipeRefresh.isRefreshing = true
            getFilmMetadata(filmID)
        }
    }

    override fun onCastItemClick(position: Int) {
        val currentItem = mCastList[position]
        gotoCredit(currentItem.id, 1)
    }

    override fun onCrewItemClick(position: Int) {
        val currentItem = mCrewList[position]
        gotoCredit(currentItem.id, 2)
    }

    private fun getFilmDetail(id: Int) {
        val filmDetailRequest = FilmDetailRequest(mContext, id)
        filmDetailRequest.sendRequest(object : FilmDetailRequest.Callback {
            override fun onSuccess(filmDetail: FilmDetail, castList: ArrayList<Cast>, crewList: ArrayList<Crew>) {
                // Parsing
                mGenreID = filmDetail.genreID
                mFilmTitle = filmDetail.originalTitle
                mFilmPoster = filmDetail.posterPath
                mYoutubeKey = filmDetail.videoKey
                mGenreTitle = ConvertGenre.getGenreForHumans(mGenreID).toString()
                mFilmYear = ParseDate.getYear(filmDetail.releaseDate)
                val overview = filmDetail.overview
                val runtime = ConvertRuntime.getRuntimeForHumans(filmDetail.runtime)
                val poster = "${TMDbAPI.BASE_LARGE_IMAGE_URL}$mFilmPoster"

                // Detail
                mToolbar.title = mFilmTitle
                mChipGenre.text = mGenreTitle
                mChipRuntime.text = runtime
                if (overview.isNotEmpty()) {
                    mImageEmptyOverview.visibility = View.GONE
                    mTextOverview.visibility = View.VISIBLE
                    mTextOverview.text = overview
                }
                Glide.with(mContext).load(poster).into(mImagePoster)
                mProgressBar.visibility = View.GONE
                mAnchorLayout.visibility = View.VISIBLE

                // Cast
                if (castList.isNotEmpty()) {
                    mCastList = ArrayList()
                    mCastList.addAll(castList)
                    setCastAdapter()
                    mImageEmptyCast.visibility = View.GONE
                }

                // Crew
                if (crewList.isNotEmpty()) {
                    mCrewList = ArrayList()
                    mCrewList.addAll(crewList)
                    setCrewAdapter()
                    mImageEmptyCrew.visibility = View.GONE
                }
            }

            override fun onError(message: String) {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = message
            }
        })
    }

    private fun getFilmMetadata(id: Int) {
        val filmMetadataRequest = FilmMetadataRequest(mContext, id)
        filmMetadataRequest.sendRequest(object : FilmMetadataRequest.Callback {
            override fun onSuccess(filmMetadata: FilmMetadata) {
                mChipRating.text = String.format("%s/5", filmMetadata.averageRating)
                mTextTotalReview.text = String.format("%d Ulasan", filmMetadata.totalReview)
                mTextTotalFavorite.text = String.format("%d Favorit", filmMetadata.totalFavorite)
                mTextTotalWatchlist.text = String.format("%d Watchlist", filmMetadata.totalWatchlist)
            }

            override fun onNotFound() {
                // Tidak digunakan
            }

            override fun onError(message: String) {
                Snackbar.make(mAnchorLayout, R.string.failed_retrieve_metadata, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mSwipeRefresh.isRefreshing = false
    }

    private fun setCastAdapter() {
        val castAdapter = CastAdapter(mContext, mCastList, this)
        mRecyclerCast.adapter = castAdapter
        mRecyclerCast.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mRecyclerCast.hasFixedSize()
        mRecyclerCast.visibility = View.VISIBLE
    }

    private fun setCrewAdapter() {
        val crewAdapter = CrewAdapter(mContext, mCrewList, this)
        mRecyclerCrew.adapter = crewAdapter
        mRecyclerCrew.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mRecyclerCrew.hasFixedSize()
        mRecyclerCrew.visibility = View.VISIBLE
    }

    private fun showFilmModal(id: Int) {
        val filmModal = FilmModal(id, mFilmTitle, mFilmYear, mFilmPoster)
        filmModal.show(supportFragmentManager, Popularin.FILM_MODAL)
    }

    private fun playTrailer(key: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Popularin.YOUTUBE_VIDEO_URL}$key"))
        startActivity(intent)
    }

    private fun searchTrailer(query: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Popularin.YOUTUBE_SEARCH_URL}$query"))
        startActivity(intent)
    }

    private fun gotoDiscoverFilm(id: Int, genreTitle: String) {
        val intent = Intent(mContext, DiscoverFilmActivity::class.java)
        intent.putExtra(Popularin.GENRE_ID, id)
        intent.putExtra(Popularin.GENRE_TITLE, genreTitle)
        startActivity(intent)
    }

    private fun gotoFilmReview(id: Int) {
        val intent = Intent(mContext, FilmReviewActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoFavoritedBy(id: Int) {
        val intent = Intent(mContext, FavoritedByActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoWatchlistedBy(id: Int) {
        val intent = Intent(mContext, WatchlistedByActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun gotoCredit(id: Int, viewPagerIndex: Int) {
        val intent = Intent(mContext, CreditDetailActivity::class.java)
        intent.putExtra(Popularin.CREDIT_ID, id)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, viewPagerIndex)
        startActivity(intent)
    }
}