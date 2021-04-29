package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.EmptyAccountActivity
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.activities.LikedByActivity
import xyz.fairportstudios.popularin.activities.ReviewReportedByActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.ReviewDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.databinding.FragmentReviewDetailBinding
import xyz.fairportstudios.popularin.interfaces.LikeReviewRequestCallback
import xyz.fairportstudios.popularin.interfaces.ReviewDetailRequestCallback
import xyz.fairportstudios.popularin.interfaces.UnlikeReviewRequestCallback
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.ReviewDetail
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class ReviewDetailFragment(private val reviewID: Int) : Fragment() {
    // Primitive
    private var mIsResumeFirstTime = true
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mContext: Context
    private lateinit var mReviewDetail: ReviewDetail
    private lateinit var mFilmYear: String

    // Binding
    private var _mBinding: FragmentReviewDetailBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = FragmentReviewDetailBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Auth
        val isAuth = Auth(mContext).isAuth()

        // Activity
        mBinding.userProfile.setOnClickListener { gotoUserDetail() }

        mBinding.filmPoster.setOnClickListener { gotoFilmDetail() }

        mBinding.filmPoster.setOnLongClickListener {
            showFilmModal()
            return@setOnLongClickListener true
        }

        mBinding.reportImage.setOnClickListener {
            gotoReviewReportedBy()
        }

        mBinding.likeImage.setOnClickListener {
            when (isAuth && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    when (mReviewDetail.isLiked) {
                        true -> unlikeReview()
                        false -> likeReview()
                    }
                }
                false -> gotoEmptyAccount()
            }
        }

        mBinding.totalLike.setOnClickListener { gotoLikedBy() }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getReviewDetail()
        }

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data
            mIsResumeFirstTime = false
            mBinding.isLoading = true
            getReviewDetail()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    private fun getReviewDetail() {
        val reviewDetailRequest = ReviewDetailRequest(mContext, reviewID)
        reviewDetailRequest.sendRequest(object : ReviewDetailRequestCallback {
            override fun onSuccess(reviewDetail: ReviewDetail) {
                mReviewDetail = reviewDetail
                mFilmYear = ParseDate.getYear(mReviewDetail.releaseDate)
                mBinding.reviewDetail = mReviewDetail
                mBinding.filmYear = mFilmYear
                mBinding.isLoading = false
                mBinding.loadSuccess = true
                mIsLoadFirstTimeSuccess = true
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mBinding.isLoading = false
                        mBinding.loadSuccess = false
                        mBinding.message = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        mBinding.swipeRefresh.isRefreshing = false
    }

    private fun setLikeState(state: Boolean) {
        mReviewDetail.isLiked = state
        when (mReviewDetail.isLiked) {
            true -> {
                mReviewDetail.totalLike++
                mBinding.likeStatus.text = getString(R.string.liked)
                mBinding.likeImage.setImageResource(R.drawable.ic_fill_heart)
            }
            false -> {
                mReviewDetail.totalLike--
                mBinding.likeStatus.text = getString(R.string.like)
                mBinding.likeImage.setImageResource(R.drawable.ic_outline_heart)
            }
        }
        mBinding.totalLike.text = String.format("Total %s", mReviewDetail.totalLike)
    }

    private fun likeReview() {
        val likeReviewRequest = LikeReviewRequest(mContext, reviewID)
        likeReviewRequest.sendRequest(object : LikeReviewRequestCallback {
            override fun onSuccess() {
                setLikeState(true)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun unlikeReview() {
        val unlikeReviewRequest = UnlikeReviewRequest(mContext, reviewID)
        unlikeReviewRequest.sendRequest(object : UnlikeReviewRequestCallback {
            override fun onSuccess() {
                setLikeState(false)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun showFilmModal() {
        val filmModal = FilmModal(mReviewDetail.tmdbID, mReviewDetail.title, mFilmYear, mReviewDetail.poster)
        filmModal.show(requireFragmentManager(), Popularin.FILM_MODAL)
    }

    private fun gotoReviewReportedBy() {
        val intent = Intent(mContext, ReviewReportedByActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, reviewID)
        startActivity(intent)
    }

    private fun gotoUserDetail() {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, mReviewDetail.userID)
        startActivity(intent)
    }

    private fun gotoFilmDetail() {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, mReviewDetail.tmdbID)
        startActivity(intent)
    }

    private fun gotoLikedBy() {
        val intent = Intent(mContext, LikedByActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, reviewID)
        startActivity(intent)
    }

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}