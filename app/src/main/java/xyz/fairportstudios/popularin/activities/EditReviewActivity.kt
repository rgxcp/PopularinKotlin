package xyz.fairportstudios.popularin.activities

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.get.ReviewDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.put.UpdateReviewRequest
import xyz.fairportstudios.popularin.databinding.ActivityEditReviewBinding
import xyz.fairportstudios.popularin.dialogs.WatchDatePickerDialog
import xyz.fairportstudios.popularin.models.ReviewDetail
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin
import java.text.DateFormat
import java.util.Calendar

class EditReviewActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    // Primitive
    private var mIsLoading = true
    private var mRating = 0.0f

    // Member
    private lateinit var mCalendar: Calendar
    private lateinit var mReviewDetail: ReviewDetail

    // Binding
    private lateinit var mBinding: ActivityEditReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityEditReviewBinding.inflate(layoutInflater)
        setContentView(mBinding.anchorLayout)

        // Context
        val context = this

        // Extra
        val reviewID = intent.getIntExtra(Popularin.REVIEW_ID, 0)

        // Calendar
        mCalendar = Calendar.getInstance()

        // Menampilkan ulasan awal
        mBinding.isLoading = true
        getCurrentReview(context, reviewID)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

        mBinding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (ratingValidated() && reviewValidated() && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    editReview(context, reviewID)
                    true
                }
                false -> false
            }
        }

        mBinding.watchDate.setOnClickListener { showDatePicker() }

        mBinding.ratingBar.setOnRatingBarChangeListener { _, rating, _ -> mRating = rating }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        mReviewDetail.watchDate = "$year-${month + 1}-$day"
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, day)
        mBinding.watchDate.text = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.time)
    }

    private fun getCurrentReview(context: Context, id: Int) {
        val reviewDetailRequest = ReviewDetailRequest(context, id)
        reviewDetailRequest.sendRequest(object : ReviewDetailRequest.Callback {
            override fun onSuccess(reviewDetail: ReviewDetail) {
                mReviewDetail = reviewDetail
                mRating = mReviewDetail.rating.toFloat()
                mBinding.reviewDetail = mReviewDetail
                mBinding.year = ParseDate.getYear(mReviewDetail.releaseDate)
                mBinding.isLoading = false
                mBinding.loadSuccess = true
                getCurrentWatchDate(mReviewDetail.watchDate)
            }

            override fun onError(message: String) {
                mBinding.isLoading = false
                mBinding.loadSuccess = false
                mBinding.message = message
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun getCurrentWatchDate(watchDate: String) {
        val year = ParseDate.getYear(watchDate).toInt()
        val month = ParseDate.getMonth(watchDate).toInt() - 1
        val day = ParseDate.getDay(watchDate).toInt()
        mReviewDetail.watchDate = "$year-${month + 1}-$day"
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, day)
        mBinding.watchDate.text = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.time)
    }

    private fun showDatePicker() {
        val datePicker = WatchDatePickerDialog()
        datePicker.show(supportFragmentManager, Popularin.DATE_PICKER)
    }

    private fun ratingValidated(): Boolean {
        return when (mRating == 0.0f) {
            true -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_rating, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun reviewValidated(): Boolean {
        mReviewDetail.reviewDetail = mBinding.inputReview.text.toString()
        return when (mReviewDetail.reviewDetail.isEmpty()) {
            true -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_review, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun editReview(context: Context, id: Int) {
        val updateReviewRequest = UpdateReviewRequest(context, id, mRating, mReviewDetail.reviewDetail, mReviewDetail.watchDate)
        updateReviewRequest.sendRequest(object : UpdateReviewRequest.Callback {
            override fun onSuccess() {
                onBackPressed()
                Toast.makeText(context, R.string.review_updated, Toast.LENGTH_SHORT).show()
            }

            override fun onFailed(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}