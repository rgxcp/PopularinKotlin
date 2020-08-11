package xyz.fairportstudios.popularin.activities

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.get.ReviewDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.put.UpdateReviewRequest
import xyz.fairportstudios.popularin.dialogs.WatchDatePickerDialog
import xyz.fairportstudios.popularin.models.ReviewDetail
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin
import xyz.fairportstudios.popularin.statics.TMDbAPI
import java.text.DateFormat
import java.util.*

class EditReviewActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    // Variable untuk fitur load
    private var mIsLoading: Boolean = true

    // Variable member
    private var mRating: Float = 0.0f
    private lateinit var mCalendar: Calendar
    private lateinit var mInputReview: EditText
    private lateinit var mImageFilmPoster: ImageView
    private lateinit var mEditReviewLayout: LinearLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRatingBar: RatingBar
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mWatchDate: String
    private lateinit var mReview: String
    private lateinit var mTextFilmTitle: TextView
    private lateinit var mTextFilmYear: TextView
    private lateinit var mTextWatchDate: TextView
    private lateinit var mTextMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_review)

        // Context
        val context = this

        // Calendar
        mCalendar = Calendar.getInstance()

        // Binding
        mInputReview = findViewById(R.id.input_aer_review)
        mImageFilmPoster = findViewById(R.id.image_aer_poster)
        mEditReviewLayout = findViewById(R.id.layout_aer_edit_review)
        mProgressBar = findViewById(R.id.pbr_aer_layout)
        mRatingBar = findViewById(R.id.rbr_aer_layout)
        mAnchorLayout = findViewById(R.id.anchor_aer_layout)
        mTextFilmTitle = findViewById(R.id.text_aer_title)
        mTextFilmYear = findViewById(R.id.text_aer_year)
        mTextWatchDate = findViewById(R.id.text_aer_watch_date)
        mTextMessage = findViewById(R.id.text_aer_message)
        val toolbar: Toolbar = findViewById(R.id.toolbar_aer_layout)

        // Extra
        val intent = intent
        val reviewID = intent.getIntExtra(Popularin.REVIEW_ID, 0)

        // Menampilkan ulasan awal
        getCurrentReview(context, reviewID)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }

        toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (ratingValidated() && reviewValidated() && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    editReview(context, reviewID)
                    true
                }
                false -> false
            }
        }

        mTextWatchDate.setOnClickListener { showDatePicker() }

        mRatingBar.setOnRatingBarChangeListener { _, rating, _ -> mRating = rating }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mWatchDate = "$year-${month + 1}-$dayOfMonth"
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        mTextWatchDate.text = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.time)
    }

    private fun getCurrentReview(context: Context, id: Int) {
        val reviewDetailRequest = ReviewDetailRequest(context, id)
        reviewDetailRequest.sendRequest(object : ReviewDetailRequest.Callback {
            override fun onSuccess(reviewDetail: ReviewDetail) {
                // Menampilkan tanggal tonton awal
                getCurrentWatchDate(reviewDetail.watchDate)

                // Parsing
                mRating = reviewDetail.rating.toFloat()
                mReview = reviewDetail.reviewDetail
                val filmYear = ParseDate.getYear(reviewDetail.releaseDate)
                val filmPoster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${reviewDetail.poster}"

                // Isi
                mTextFilmTitle.text = reviewDetail.title
                mTextFilmYear.text = filmYear
                mRatingBar.rating = mRating
                mInputReview.setText(mReview)
                Glide.with(context).load(filmPoster).into(mImageFilmPoster)
                mProgressBar.visibility = View.GONE
                mEditReviewLayout.visibility = View.VISIBLE
            }

            override fun onError(message: String) {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = message
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun getCurrentWatchDate(watchDate: String) {
        val parseDate = ParseDate
        val year = parseDate.getYear(watchDate).toInt()
        val month = parseDate.getMonth(watchDate).toInt() - 1
        val day = parseDate.getDay(watchDate).toInt()
        mWatchDate = "$year-${month + 1}-$day"
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, day)
        mTextWatchDate.text = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.time)
    }

    private fun showDatePicker() {
        val datePicker = WatchDatePickerDialog()
        datePicker.show(supportFragmentManager, Popularin.DATE_PICKER)
    }

    private fun ratingValidated(): Boolean {
        return when (mRating == 0.0f) {
            true -> {
                Snackbar.make(mAnchorLayout, R.string.validate_rating, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun reviewValidated(): Boolean {
        mReview = mInputReview.text.toString()
        return when (mReview.isEmpty()) {
            true -> {
                Snackbar.make(mAnchorLayout, R.string.validate_review, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun editReview(context: Context, id: Int) {
        val updateReviewRequest = UpdateReviewRequest(context, id, mRating, mReview, mWatchDate)
        updateReviewRequest.sendRequest(object : UpdateReviewRequest.Callback {
            override fun onSuccess() {
                onBackPressed()
                Toast.makeText(context, R.string.review_updated, Toast.LENGTH_SHORT).show()
            }

            override fun onFailed(message: String) {
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }

            override fun onError(message: String) {
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}