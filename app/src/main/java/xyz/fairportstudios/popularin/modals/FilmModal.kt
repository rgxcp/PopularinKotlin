package xyz.fairportstudios.popularin.modals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.AddReviewActivity
import xyz.fairportstudios.popularin.activities.EmptyAccountActivity
import xyz.fairportstudios.popularin.apis.popularin.delete.DeleteFavoriteRequest
import xyz.fairportstudios.popularin.apis.popularin.delete.DeleteWatchlistRequest
import xyz.fairportstudios.popularin.apis.popularin.get.FilmSelfRequest
import xyz.fairportstudios.popularin.apis.popularin.post.AddFavoriteRequest
import xyz.fairportstudios.popularin.apis.popularin.post.AddWatchlistRequest
import xyz.fairportstudios.popularin.databinding.ModalFilmBinding
import xyz.fairportstudios.popularin.models.FilmSelf
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class FilmModal(
    private val filmID: Int,
    private val filmTitle: String,
    private val filmYear: String,
    private val filmPoster: String
) : BottomSheetDialogFragment() {
    // Primitive
    private var mLastRate = 0.0f

    // Member
    private lateinit var mFilmSelf: FilmSelf

    // Binding
    private var _mBinding: ModalFilmBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = ModalFilmBinding.inflate(inflater, container, false)

        // Context
        val context = requireActivity()

        // Auth
        val isAuth = Auth(context).isAuth()

        // Isi
        mBinding.title = filmTitle
        mBinding.year = filmYear

        // Mendapatkan status film
        if (isAuth) {
            getFilmSelf(context)
        }

        // Activity
        mBinding.reviewImage.setOnClickListener {
            when (isAuth) {
                true -> gotoAddReview(context)
                false -> gotoEmptyAccount(context)
            }
            dismiss()
        }

        mBinding.favoriteImage.setOnClickListener {
            when (isAuth) {
                true -> {
                    when (mFilmSelf.inFavorite) {
                        true -> removeFromFavorite(context)
                        false -> addToFavorite(context)
                    }
                }
                false -> gotoEmptyAccount(context)
            }
            dismiss()
        }

        mBinding.watchlistImage.setOnClickListener {
            when (isAuth) {
                true -> {
                    when (mFilmSelf.inWatchlist) {
                        true -> removeFromWatchlist(context)
                        false -> addToWatchlist(context)
                    }
                }
                false -> gotoEmptyAccount(context)
            }
            dismiss()
        }

        mBinding.ratingBar.setOnRatingBarChangeListener { _, newRate, _ ->
            when (isAuth) {
                true -> {
                    if (mLastRate != newRate) {
                        mLastRate = newRate
                        gotoAddReview(context)
                        dismiss()
                    }
                }
                false -> {
                    gotoEmptyAccount(context)
                    dismiss()
                }
            }
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    private fun getFilmSelf(context: Context) {
        val filmSelfRequest = FilmSelfRequest(context, filmID)
        filmSelfRequest.sendRequest(object : FilmSelfRequest.Callback {
            override fun onSuccess(filmSelf: FilmSelf) {
                mFilmSelf = filmSelf
                mBinding.filmSelf = mFilmSelf
                mLastRate = mFilmSelf.lastRate.toFloat()
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addToFavorite(context: Context) {
        val addFavoriteRequest = AddFavoriteRequest(context, filmID)
        addFavoriteRequest.sendRequest(object : AddFavoriteRequest.Callback {
            override fun onSuccess() {
                Toast.makeText(context, R.string.film_added_to_favorite, Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeFromFavorite(context: Context) {
        val deleteFavoriteRequest = DeleteFavoriteRequest(context, filmID)
        deleteFavoriteRequest.sendRequest(object : DeleteFavoriteRequest.Callback {
            override fun onSuccess() {
                Toast.makeText(context, R.string.film_removed_from_favorite, Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addToWatchlist(context: Context) {
        val addWatchlistRequest = AddWatchlistRequest(context, filmID)
        addWatchlistRequest.sendRequest(object : AddWatchlistRequest.Callback {
            override fun onSuccess() {
                Toast.makeText(context, R.string.film_added_to_watchlist, Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeFromWatchlist(context: Context) {
        val deleteWatchlistRequest = DeleteWatchlistRequest(context, filmID)
        deleteWatchlistRequest.sendRequest(object : DeleteWatchlistRequest.Callback {
            override fun onSuccess() {
                Toast.makeText(context, R.string.film_removed_from_watchlist, Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun gotoAddReview(context: Context) {
        val intent = Intent(context, AddReviewActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, filmID)
        intent.putExtra(Popularin.FILM_TITLE, filmTitle)
        intent.putExtra(Popularin.FILM_YEAR, filmYear)
        intent.putExtra(Popularin.FILM_POSTER, filmPoster)
        intent.putExtra(Popularin.RATING, mLastRate)
        startActivity(intent)
    }

    private fun gotoEmptyAccount(context: Context) {
        val intent = Intent(context, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}