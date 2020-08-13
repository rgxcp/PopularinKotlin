package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.ReviewDetail
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin
import xyz.fairportstudios.popularin.statics.TMDbAPI

class ReviewDetailFragment(private val reviewID: Int) : Fragment() {
    // Variable untuk fitur onResume
    private var mIsResumeFirstTime: Boolean = true

    // Variable untuk fitur load
    private var mIsLoading: Boolean = true
    private var mIsLoadFirstTimeSuccess: Boolean = false

    // Variable member
    private var mIsLiked: Boolean = false
    private var mUserID: Int = 0
    private var mFilmID: Int = 0
    private var mTotalLike: Int = 0
    private lateinit var mContext: Context
    private lateinit var mImageUserProfile: ImageView
    private lateinit var mImageFilmPoster: ImageView
    private lateinit var mImageReviewStar: ImageView
    private lateinit var mImageLike: ImageView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mScrollView: ScrollView
    private lateinit var mFilmTitle: String
    private lateinit var mFilmYear: String
    private lateinit var mFilmPoster: String
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextUsername: TextView
    private lateinit var mTextFilmTitleYear: TextView
    private lateinit var mTextReviewDate: TextView
    private lateinit var mTextReviewDetail: TextView
    private lateinit var mTextLikeStatus: TextView
    private lateinit var mTextTotalLike: TextView
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_review_detail, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mImageUserProfile = view.findViewById(R.id.image_frd_profile)
        mImageFilmPoster = view.findViewById(R.id.image_frd_poster)
        mImageReviewStar = view.findViewById(R.id.image_frd_star)
        mImageLike = view.findViewById(R.id.image_frd_like)
        mProgressBar = view.findViewById(R.id.pbr_frd_layout)
        mAnchorLayout = view.findViewById(R.id.anchor_frd_layout)
        mScrollView = view.findViewById(R.id.scroll_frd_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_frd_layout)
        mTextUsername = view.findViewById(R.id.text_frd_username)
        mTextFilmTitleYear = view.findViewById(R.id.text_frd_title_year)
        mTextReviewDate = view.findViewById(R.id.text_frd_date)
        mTextReviewDetail = view.findViewById(R.id.text_frd_review)
        mTextLikeStatus = view.findViewById(R.id.text_frd_like_status)
        mTextTotalLike = view.findViewById(R.id.text_frd_total_like)
        mTextMessage = view.findViewById(R.id.text_frd_message)

        // Auth
        val isAuth = Auth(mContext).isAuth()

        // Activity
        mImageUserProfile.setOnClickListener { gotoUserDetail() }

        mImageFilmPoster.setOnClickListener { gotoFilmDetail() }

        mImageFilmPoster.setOnLongClickListener {
            showFilmModal()
            return@setOnLongClickListener true
        }

        mImageLike.setOnClickListener {
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

        mTextTotalLike.setOnClickListener { gotoLikedBy() }

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getReviewDetail()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data
            mIsResumeFirstTime = false
            getReviewDetail()
        }
    }

    private fun getReviewDetail() {
        val reviewDetailRequest = ReviewDetailRequest(mContext, reviewID)
        reviewDetailRequest.sendRequest(object : ReviewDetailRequest.Callback {
            override fun onSuccess(reviewDetail: ReviewDetail) {
                // Like status
                mIsLiked = reviewDetail.isLiked
                if (mIsLiked) {
                    mTextLikeStatus.text = R.string.liked.toString()
                    mImageLike.setImageResource(R.drawable.ic_fill_heart)
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

                // Isi
                mTextUsername.text = reviewDetail.username
                mTextFilmTitleYear.text = String.format("%s (%s)", mFilmTitle, mFilmYear)
                mTextReviewDate.text = reviewDate
                mTextReviewDetail.text = reviewDetail.reviewDetail
                mTextTotalLike.text = String.format("Total %s", mTotalLike)
                mImageReviewStar.setImageResource(reviewStar!!)
                Glide.with(mContext).load(reviewDetail.profilePicture).into(mImageUserProfile)
                Glide.with(mContext).load("${TMDbAPI.BASE_SMALL_IMAGE_URL}$mFilmPoster").into(mImageFilmPoster)
                mTextMessage.visibility = View.GONE
                mProgressBar.visibility = View.GONE
                mScrollView.visibility = View.VISIBLE
                mIsLoadFirstTimeSuccess = true
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mProgressBar.visibility = View.GONE
                        mTextMessage.visibility = View.VISIBLE
                        mTextMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        mSwipeRefresh.isRefreshing = false
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

    private fun showFilmModal() {
        val fragmentManager = (mContext as FragmentActivity).supportFragmentManager
        val filmModal = FilmModal(mFilmID, mFilmTitle, mFilmYear, mFilmPoster)
        filmModal.show(fragmentManager, Popularin.FILM_MODAL)
    }

    private fun setLikeState(state: Boolean) {
        mIsLiked = state
        when (mIsLiked) {
            true -> {
                mTotalLike++
                mTextLikeStatus.text = R.string.liked.toString()
                mImageLike.setImageResource(R.drawable.ic_fill_heart)
            }
            false -> {
                mTotalLike--
                mTextLikeStatus.text = R.string.like.toString()
                mImageLike.setImageResource(R.drawable.ic_outline_heart)
            }
        }
        mTextTotalLike.text = String.format("Total %s", mTotalLike)
    }

    private fun likeReview() {
        val likeReviewRequest = LikeReviewRequest(mContext, reviewID)
        likeReviewRequest.sendRequest(object : LikeReviewRequest.Callback {
            override fun onSuccess() {
                setLikeState(true)
            }

            override fun onError(message: String) {
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
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
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}