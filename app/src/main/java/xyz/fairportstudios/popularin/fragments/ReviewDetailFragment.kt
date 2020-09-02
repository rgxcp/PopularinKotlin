package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.EmptyAccountActivity
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.activities.LikedByActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.ReviewDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.databinding.FragmentReviewDetailBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.ReviewDetail
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin
import xyz.fairportstudios.popularin.statics.TMDbAPI

class ReviewDetailFragment(private val reviewID: Int) : Fragment() {
    // Primitive
    private var mIsResumeFirstTime = true
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private var mIsLiked = false
    private var mUserID = 0
    private var mFilmID = 0
    private var mTotalLike = 0

    // Member
    private lateinit var mContext: Context
    private lateinit var mFilmTitle: String
    private lateinit var mFilmYear: String
    private lateinit var mFilmPoster: String

    // View binding
    private var _mViewBinding: FragmentReviewDetailBinding? = null
    private val mViewBinding get() = _mViewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mViewBinding = FragmentReviewDetailBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Auth
        val isAuth = Auth(mContext).isAuth()

        // Activity
        mViewBinding.userProfile.setOnClickListener { gotoUserDetail() }

        mViewBinding.filmPoster.setOnClickListener { gotoFilmDetail() }

        mViewBinding.filmPoster.setOnLongClickListener {
            showFilmModal()
            return@setOnLongClickListener true
        }

        mViewBinding.likeImage.setOnClickListener {
            when (isAuth && !mIsLoading) {
                true -> {
                    mIsLoading = true
                    when (mIsLiked) {
                        true -> unlikeReview()
                        false -> likeReview()
                    }
                }
                false -> gotoEmptyAccount()
            }
        }

        mViewBinding.totalLike.setOnClickListener { gotoLikedBy() }

        mViewBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mViewBinding.swipeRefresh.isRefreshing = true
            getReviewDetail()
        }

        return mViewBinding.root
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data
            mIsResumeFirstTime = false
            getReviewDetail()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mViewBinding = null
    }

    private fun getReviewDetail() {
        val reviewDetailRequest = ReviewDetailRequest(mContext, reviewID)
        reviewDetailRequest.sendRequest(object : ReviewDetailRequest.Callback {
            override fun onSuccess(reviewDetail: ReviewDetail) {
                // Like status
                mIsLiked = reviewDetail.isLiked
                if (mIsLiked) {
                    mViewBinding.likeStatus.text = getString(R.string.liked)
                    mViewBinding.likeImage.setImageResource(R.drawable.ic_fill_heart)
                }

                // Parsing
                mUserID = reviewDetail.userID
                mFilmID = reviewDetail.tmdbID
                mTotalLike = reviewDetail.totalLike
                mFilmTitle = reviewDetail.title
                mFilmPoster = reviewDetail.poster
                mFilmYear = ParseDate.getYear(reviewDetail.releaseDate)
                val reviewStar = ConvertRating.getStar(reviewDetail.rating)
                val reviewDate = ParseDate.getDateForHumans(reviewDetail.reviewDate)
                val poster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}$mFilmPoster"

                // Isi
                mViewBinding.username.text = reviewDetail.username
                mViewBinding.filmTitleYear.text = String.format("%s (%s)", mFilmTitle, mFilmYear)
                mViewBinding.reviewDate.text = reviewDate
                mViewBinding.reviewDetail.text = reviewDetail.reviewDetail
                mViewBinding.totalLike.text = String.format("Total %s", mTotalLike)
                reviewStar?.let { mViewBinding.starImage.setImageResource(it) }
                Glide.with(mContext).load(reviewDetail.profilePicture).into(mViewBinding.userProfile)
                Glide.with(mContext).load(poster).into(mViewBinding.filmPoster)
                mViewBinding.errorMessage.visibility = View.GONE
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.scrollView.visibility = View.VISIBLE
                mIsLoadFirstTimeSuccess = true
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mViewBinding.progressBar.visibility = View.GONE
                        mViewBinding.errorMessage.visibility = View.VISIBLE
                        mViewBinding.errorMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        mViewBinding.swipeRefresh.isRefreshing = false
    }

    private fun setLikeState(state: Boolean) {
        mIsLiked = state
        when (mIsLiked) {
            true -> {
                mTotalLike++
                mViewBinding.likeStatus.text = getString(R.string.liked)
                mViewBinding.likeImage.setImageResource(R.drawable.ic_fill_heart)
            }
            false -> {
                mTotalLike--
                mViewBinding.likeStatus.text = getString(R.string.like)
                mViewBinding.likeImage.setImageResource(R.drawable.ic_outline_heart)
            }
        }
        mViewBinding.totalLike.text = String.format("Total %s", mTotalLike)
    }

    private fun likeReview() {
        val likeReviewRequest = LikeReviewRequest(mContext, reviewID)
        likeReviewRequest.sendRequest(object : LikeReviewRequest.Callback {
            override fun onSuccess() {
                setLikeState(true)
            }

            override fun onError(message: String) {
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun unlikeReview() {
        val unlikeReviewRequest = UnlikeReviewRequest(mContext, reviewID)
        unlikeReviewRequest.sendRequest(object : UnlikeReviewRequest.Callback {
            override fun onSuccess() {
                setLikeState(false)
            }

            override fun onError(message: String) {
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun showFilmModal() {
        val filmModal = FilmModal(mFilmID, mFilmTitle, mFilmYear, mFilmPoster)
        filmModal.show(requireFragmentManager(), Popularin.FILM_MODAL)
    }

    private fun gotoUserDetail() {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, mUserID)
        startActivity(intent)
    }

    private fun gotoFilmDetail() {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, mFilmID)
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