package xyz.fairportstudios.popularin.activities

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.post.AddReviewRequest
import xyz.fairportstudios.popularin.dialogs.WatchDatePickerDialog
import xyz.fairportstudios.popularin.statics.Popularin
import xyz.fairportstudios.popularin.statics.TMDbAPI
import java.text.DateFormat
import java.util.*

class AddReviewActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    // Variable untuk fitur load
    private var mIsLoading: Boolean = false

    // Variable member
    private var mRating: Float = 0.0f
    private lateinit var mCalendar: Calendar
    private lateinit var mInputReview: EditText
    private lateinit var mAnchorLayout: LinearLayout
    private lateinit var mWatchDate: String
    private lateinit var mReview: String
    private lateinit var mTextWatchDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        // Context
        val context = this

        // Calendar
        mCalendar = Calendar.getInstance()

        // Binding
        mInputReview = findViewById(R.id.input_adr_review)
        mAnchorLayout = findViewById(R.id.anchor_adr_layout)
        mTextWatchDate = findViewById(R.id.text_adr_watch_date)
        val imageFilmPoster: ImageView = findViewById(R.id.image_adr_poster)
        val ratingBar: RatingBar = findViewById(R.id.rbr_adr_layout)
        val textFilmTitle: TextView = findViewById(R.id.text_adr_title)
        val textFilmYear: TextView = findViewById(R.id.text_adr_year)
        val toolbar: Toolbar = findViewById(R.id.toolbar_adr_layout)

        // Extra
        val intent = intent
        mRating = intent.getFloatExtra(Popularin.RATING, 0.0f)
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)
        val filmTitle = intent.getStringExtra(Popularin.FILM_TITLE)
        val filmYear = intent.getStringExtra(Popularin.FILM_YEAR)
        val filmPoster = intent.getStringExtra("${TMDbAPI.BASE_SMALL_IMAGE_URL}${intent.getStringExtra(Popularin.FILM_POSTER)}")

        // Menampilkan tanggal sekarang
        getCurrentDate()

        // Menampilkan info film dan rating
        textFilmTitle.text = filmTitle
        textFilmYear.text = filmYear
        ratingBar.rating = mRating
        Glide.with(context).load(filmPoster).into(imageFilmPoster)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }

        toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (ratingValidated() && reviewValidated() && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    addReview(context, filmID)
                    true
                }
                false -> false
            }
        }

        mTextWatchDate.setOnClickListener { showDatePicker() }

        ratingBar.setOnRatingBarChangeListener { _, rating, _ -> mRating = rating }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mWatchDate = "$year-${month + 1}-$dayOfMonth"
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        mTextWatchDate.text = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.time)
    }

    private fun getCurrentDate() {
        val year = mCalendar.get(Calendar.YEAR)
        val month = mCalendar.get(Calendar.MONTH)
        val day = mCalendar.get(Calendar.DAY_OF_MONTH)
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

    private fun addReview(context: Context, filmID: Int) {
        val addReviewRequest = AddReviewRequest(context, filmID, mRating, mReview, mWatchDate)
        addReviewRequest.sendRequest(object : AddReviewRequest.Callback {
            override fun onSuccess() {
                onBackPressed()
                Toast.makeText(context, R.string.review_added, Toast.LENGTH_SHORT).show()
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