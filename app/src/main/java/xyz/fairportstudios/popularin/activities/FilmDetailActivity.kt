package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.CastAdapter
import xyz.fairportstudios.popularin.adapters.CrewAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.FilmMetadataRequest
import xyz.fairportstudios.popularin.apis.tmdb.get.FilmDetailRequest
import xyz.fairportstudios.popularin.databinding.ActivityFilmDetailBinding
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

    // View binding
    private lateinit var mViewBinding: ActivityFilmDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityFilmDetailBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        // Context
        mContext = this

        // Extra
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)

        // Mengatur jenis font untuk collapsing toolbar
        val typeface = ResourcesCompat.getFont(mContext, R.font.monument_extended_regular)
        mViewBinding.collapsingToolbar.setExpandedTitleTypeface(typeface)

        // Mendapatkan detail film
        getFilmDetail(filmID)

        // Mendapatkan metadata film
        getFilmMetadata(filmID)

        // Activity
        mViewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mViewBinding.playImage.setOnClickListener {
            when (mYoutubeKey.isNotEmpty()) {
                true -> playTrailer(mYoutubeKey)
                false -> searchTrailer("${mFilmTitle.toLowerCase(Locale.ROOT)} trailer")
            }
        }

        mViewBinding.genreChip.setOnClickListener {
            if (mGenreID != 0) gotoDiscoverFilm(mGenreID, mGenreTitle)
        }

        mViewBinding.reviewImage.setOnClickListener { gotoFilmReview(filmID) }

        mViewBinding.favoriteImage.setOnClickListener { gotoFavoritedBy(filmID) }

        mViewBinding.watchlistImage.setOnClickListener { gotoWatchlistedBy(filmID) }

        mViewBinding.fab.setOnClickListener { showFilmModal(filmID) }

        mViewBinding.swipeRefresh.setOnRefreshListener {
            mViewBinding.swipeRefresh.isRefreshing = true
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
                mViewBinding.toolbar.title = mFilmTitle
                mViewBinding.genreChip.text = mGenreTitle
                mViewBinding.runtimeChip.text = runtime
                if (overview.isNotEmpty()) {
                    mViewBinding.emptyOverviewImage.visibility = View.GONE
                    mViewBinding.overview.visibility = View.VISIBLE
                    mViewBinding.overview.text = overview
                }
                Glide.with(mContext).load(poster).into(mViewBinding.filmPoster)
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.anchorLayout.visibility = View.VISIBLE

                // Cast
                if (castList.isNotEmpty()) {
                    mCastList = ArrayList()
                    mCastList.addAll(castList)
                    setCastAdapter()
                    mViewBinding.emptyCastImage.visibility = View.GONE
                }

                // Crew
                if (crewList.isNotEmpty()) {
                    mCrewList = ArrayList()
                    mCrewList.addAll(crewList)
                    setCrewAdapter()
                    mViewBinding.emptyCrewImage.visibility = View.GONE
                }
            }

            override fun onError(message: String) {
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.errorMessage.visibility = View.VISIBLE
                mViewBinding.errorMessage.text = message
            }
        })
    }

    private fun getFilmMetadata(id: Int) {
        val filmMetadataRequest = FilmMetadataRequest(mContext, id)
        filmMetadataRequest.sendRequest(object : FilmMetadataRequest.Callback {
            override fun onSuccess(filmMetadata: FilmMetadata) {
                mViewBinding.ratingChip.text = String.format("%s/5", filmMetadata.averageRating)
                mViewBinding.totalReview.text = String.format("%d Ulasan", filmMetadata.totalReview)
                mViewBinding.totalFavorite.text = String.format("%d Favorit", filmMetadata.totalFavorite)
                mViewBinding.totalWatchlist.text = String.format("%d Watchlist", filmMetadata.totalWatchlist)
            }

            override fun onNotFound() {
                // Tidak digunakan
            }

            override fun onError(message: String) {
                Snackbar.make(mViewBinding.anchorLayout, R.string.failed_retrieve_metadata, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mViewBinding.swipeRefresh.isRefreshing = false
    }

    private fun setCastAdapter() {
        val castAdapter = CastAdapter(mContext, mCastList, this)
        mViewBinding.recyclerViewCast.adapter = castAdapter
        mViewBinding.recyclerViewCast.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mViewBinding.recyclerViewCast.hasFixedSize()
        mViewBinding.recyclerViewCast.visibility = View.VISIBLE
    }

    private fun setCrewAdapter() {
        val crewAdapter = CrewAdapter(mContext, mCrewList, this)
        mViewBinding.recyclerViewCrew.adapter = crewAdapter
        mViewBinding.recyclerViewCrew.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mViewBinding.recyclerViewCrew.hasFixedSize()
        mViewBinding.recyclerViewCrew.visibility = View.VISIBLE
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