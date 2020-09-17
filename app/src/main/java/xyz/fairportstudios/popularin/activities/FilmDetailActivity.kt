package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin
import java.util.Locale
import kotlin.collections.ArrayList

class FilmDetailActivity : AppCompatActivity(), CastAdapter.OnClickListener, CrewAdapter.OnClickListener {
    // Member
    private lateinit var mCastList: ArrayList<Cast>
    private lateinit var mCrewList: ArrayList<Crew>
    private lateinit var mContext: Context
    private lateinit var mFilmDetail: FilmDetail

    // Binding
    private lateinit var mBinding: ActivityFilmDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityFilmDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Context
        mContext = this

        // Extra
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)

        // Mengatur jenis font untuk collapsing toolbar
        val typeface = ResourcesCompat.getFont(mContext, R.font.monument_extended_regular)
        mBinding.collapsingToolbar.setExpandedTitleTypeface(typeface)

        // Mendapatkan detail film
        mBinding.isLoading = true
        getFilmDetail(filmID)

        // Mendapatkan metadata film
        getFilmMetadata(filmID)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mBinding.playImage.setOnClickListener {
            when (mFilmDetail.videoKey.isNotEmpty()) {
                true -> playTrailer(mFilmDetail.videoKey)
                false -> searchTrailer("${mFilmDetail.originalTitle.toLowerCase(Locale.ROOT)} trailer")
            }
        }

        mBinding.genreChip.setOnClickListener {
            if (mFilmDetail.genreID != 0) gotoDiscoverFilm(mFilmDetail.genreID, mBinding.genreChip.text.toString())
        }

        mBinding.reviewImage.setOnClickListener { gotoFilmReview(filmID) }

        mBinding.favoriteImage.setOnClickListener { gotoFavoritedBy(filmID) }

        mBinding.watchlistImage.setOnClickListener { gotoWatchlistedBy(filmID) }

        mBinding.fab.setOnClickListener { showFilmModal(filmID) }

        mBinding.swipeRefresh.setOnRefreshListener {
            mBinding.swipeRefresh.isRefreshing = true
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
                // Detail
                mFilmDetail = filmDetail
                mBinding.filmDetail = mFilmDetail
                mBinding.isLoading = false
                mBinding.loadSuccess = true

                // Cast
                if (mFilmDetail.hasCast) {
                    mCastList = ArrayList()
                    mCastList.addAll(castList)
                    setCastAdapter()
                }

                // Crew
                if (mFilmDetail.hasCrew) {
                    mCrewList = ArrayList()
                    mCrewList.addAll(crewList)
                    setCrewAdapter()
                }
            }

            override fun onError(message: String) {
                mBinding.isLoading = false
                mBinding.loadSuccess = false
                mBinding.message = message
            }
        })
    }

    private fun getFilmMetadata(id: Int) {
        val filmMetadataRequest = FilmMetadataRequest(mContext, id)
        filmMetadataRequest.sendRequest(object : FilmMetadataRequest.Callback {
            override fun onSuccess(filmMetadata: FilmMetadata) {
                mBinding.filmMetadata = filmMetadata
            }

            override fun onNotFound() {
                // Tidak digunakan
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, R.string.failed_retrieve_metadata, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mBinding.swipeRefresh.isRefreshing = false
    }

    private fun setCastAdapter() {
        val castAdapter = CastAdapter(mContext, mCastList, this)
        mBinding.recyclerViewCast.adapter = castAdapter
        mBinding.recyclerViewCast.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mBinding.recyclerViewCast.hasFixedSize()
    }

    private fun setCrewAdapter() {
        val crewAdapter = CrewAdapter(mContext, mCrewList, this)
        mBinding.recyclerViewCrew.adapter = crewAdapter
        mBinding.recyclerViewCrew.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mBinding.recyclerViewCrew.hasFixedSize()
    }

    private fun showFilmModal(id: Int) {
        val filmYear = ParseDate.getYear(mFilmDetail.releaseDate)
        val filmModal = FilmModal(id, mFilmDetail.originalTitle, filmYear, mFilmDetail.posterPath)
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