package xyz.fairportstudios.popularin.activities

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.post.AddReviewRequest
import xyz.fairportstudios.popularin.databinding.ActivityAddReviewBinding
import xyz.fairportstudios.popularin.dialogs.WatchDatePickerDialog
import xyz.fairportstudios.popularin.statics.Popularin
import xyz.fairportstudios.popularin.statics.TMDbAPI
import java.text.DateFormat
import java.util.*

class AddReviewActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    // Primitive
    private var mIsLoading = false
    private var mRating = 0.0f

    // Member
    private lateinit var mCalendar: Calendar
    private lateinit var mWatchDate: String
    private lateinit var mReview: String

    // View binding
    private lateinit var mViewBinding: ActivityAddReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityAddReviewBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        // Context
        val context = this

        // Extra
        mRating = intent.getFloatExtra(Popularin.RATING, 0.0f)
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)
        val filmTitle = intent.getStringExtra(Popularin.FILM_TITLE)
        val filmYear = intent.getStringExtra(Popularin.FILM_YEAR)
        val filmPoster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${intent.getStringExtra(Popularin.FILM_POSTER)}"

        // Calendar
        mCalendar = Calendar.getInstance()

        // Menampilkan tanggal sekarang
        getCurrentDate()

        // Menampilkan info film dan rating
        mViewBinding.filmTitle.text = filmTitle
        mViewBinding.filmYear.text = filmYear
        mViewBinding.ratingBar.rating = mRating
        Glide.with(context).load(filmPoster).into(mViewBinding.filmPoster)

        // Activity
        mViewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mViewBinding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (ratingValidated() && reviewValidated() && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    addReview(context, filmID)
                    true
                }
                false -> false
            }
        }

        mViewBinding.watchDate.setOnClickListener { showDatePicker() }

        mViewBinding.ratingBar.setOnRatingBarChangeListener { _, rating, _ -> mRating = rating }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        mWatchDate = "$year-${month + 1}-$day"
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, day)
        mViewBinding.watchDate.text = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.time)
    }

    private fun getCurrentDate() {
        val year = mCalendar.get(Calendar.YEAR)
        val month = mCalendar.get(Calendar.MONTH)
        val day = mCalendar.get(Calendar.DAY_OF_MONTH)
        mWatchDate = "$year-${month + 1}-$day"
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, day)
        mViewBinding.watchDate.text = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.time)
    }

    private fun showDatePicker() {
        val datePicker = WatchDatePickerDialog()
        datePicker.show(supportFragmentManager, Popularin.DATE_PICKER)
    }

    private fun ratingValidated(): Boolean {
        return when (mRating == 0.0f) {
            true -> {
                Snackbar.make(mViewBinding.anchorLayout, R.string.validate_rating, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun reviewValidated(): Boolean {
        mReview = mViewBinding.inputReview.text.toString()
        return when (mReview.isEmpty()) {
            true -> {
                Snackbar.make(mViewBinding.anchorLayout, R.string.validate_review, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun addReview(context: Context, filmID: Int) {
        val addReviewRequest = AddReviewRequest(context, filmID, mRating, mReview, mWatchDate)
        addReviewRequest.sendRequest(object : AddReviewRequest.Callback {
            override fun onSuccess() {
                onBackPressed()
                Toast.makeText(context, R.string.review_added, Toast.LENGTH_SHORT).show()
            }

            override fun onFailed(message: String) {
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }

            override fun onError(message: String) {
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}