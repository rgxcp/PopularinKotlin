package xyz.fairportstudios.popularin.modals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
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
import xyz.fairportstudios.popularin.models.FilmSelf
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class FilmModal(
    private val filmID: Int,
    private val filmTitle: String,
    private val filmYear: String,
    private val filmPoster: String
) : BottomSheetDialogFragment() {
    // Variable member
    private var mInReview: Boolean = false
    private var mInFavorite: Boolean = false
    private var mInWatchlist: Boolean = false
    private var mLastRate: Float = 0.0f
    private lateinit var mImageReview: ImageView
    private lateinit var mImageFavorite: ImageView
    private lateinit var mImageWatchlist: ImageView
    private lateinit var mRatingBar: RatingBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.modal_film, container, false)

        // Context
        val context = requireActivity()

        // Binding
        mImageReview = view.findViewById(R.id.image_mf_review)
        mImageFavorite = view.findViewById(R.id.image_mf_favorite)
        mImageWatchlist = view.findViewById(R.id.image_mf_watchlist)
        mRatingBar = view.findViewById(R.id.rbr_mf_layout)
        val textFilmTitle: TextView = view.findViewById(R.id.text_mf_title)
        val textFilmYear: TextView = view.findViewById(R.id.text_mf_year)

        // Auth
        val isAuth = Auth(context).isAuth()

        // Isi
        textFilmTitle.text = filmTitle
        textFilmYear.text = filmYear

        // Mendapatkan status film
        if (isAuth) {
            getFilmSelf(context)
        }

        // Activity
        mImageReview.setOnClickListener {
            when (isAuth) {
                true -> addReview(context)
                false -> gotoEmptyAccount(context)
            }
            dismiss()
        }

        mImageFavorite.setOnClickListener {
            when (isAuth) {
                true -> {
                    when (mInFavorite) {
                        true -> removeFromFavorite(context)
                        false -> addToFavorite(context)
                    }
                }
                false -> gotoEmptyAccount(context)
            }
            dismiss()
        }

        mImageWatchlist.setOnClickListener {
            when (isAuth) {
                true -> {
                    when (mInFavorite) {
                        true -> removeFromWatchlist(context)
                        false -> addToWatchlist(context)
                    }
                }
                false -> gotoEmptyAccount(context)
            }
            dismiss()
        }

        mRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            when (isAuth) {
                true -> {
                    if (mLastRate != rating) {
                        mLastRate = rating
                        addReview(context)
                        dismiss()
                    }
                }
                false -> {
                    gotoEmptyAccount(context)
                    dismiss()
                }
            }
        }

        return view
    }

    private fun getFilmSelf(context: Context) {
        val filmSelfRequest = FilmSelfRequest(context, filmID)
        filmSelfRequest.sendRequest(object : FilmSelfRequest.Callback {
            override fun onSuccess(filmSelf: FilmSelf) {
                mInReview = filmSelf.inReview
                mInFavorite = filmSelf.inFavorite
                mInWatchlist = filmSelf.inWatchlist
                mLastRate = filmSelf.lastRate.toFloat()

                if (mInReview) {
                    mImageReview.setImageResource(R.drawable.ic_fill_eye)
                }
                if (mInFavorite) {
                    mImageFavorite.setImageResource(R.drawable.ic_fill_heart)
                }
                if (mInWatchlist) {
                    mImageWatchlist.setImageResource(R.drawable.ic_fill_watchlist)
                }
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun gotoEmptyAccount(context: Context) {
        val intent = Intent(context, EmptyAccountActivity::class.java)
        startActivity(intent)
    }

    private fun addReview(context: Context) {
        val intent = Intent(context, AddReviewActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, filmID)
        intent.putExtra(Popularin.FILM_TITLE, filmTitle)
        intent.putExtra(Popularin.FILM_YEAR, filmYear)
        intent.putExtra(Popularin.FILM_POSTER, filmPoster)
        intent.putExtra(Popularin.RATING, mLastRate)
        startActivity(intent)
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
}