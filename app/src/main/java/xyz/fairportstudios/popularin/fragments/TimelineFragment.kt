package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.DiscoverFilmActivity
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.activities.ReviewActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.GenreHorizontalAdapter
import xyz.fairportstudios.popularin.adapters.ReviewAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.TimelineRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.databinding.FragmentTimelineBinding
import xyz.fairportstudios.popularin.interfaces.GenreHorizontalAdapterClickListener
import xyz.fairportstudios.popularin.interfaces.LikeReviewRequestCallback
import xyz.fairportstudios.popularin.interfaces.ReviewAdapterClickListener
import xyz.fairportstudios.popularin.interfaces.TimelineRequestCallback
import xyz.fairportstudios.popularin.interfaces.UnlikeReviewRequestCallback
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Genre
import xyz.fairportstudios.popularin.models.Review
import xyz.fairportstudios.popularin.services.LoadGenre
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class TimelineFragment : Fragment(), GenreHorizontalAdapterClickListener, ReviewAdapterClickListener {
    // Primitive
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0
    private var mTotalLike = 0

    // Member
    private lateinit var mGenreList: ArrayList<Genre>
    private lateinit var mReviewList: ArrayList<Review>
    private lateinit var mContext: Context
    private lateinit var mReviewAdapter: ReviewAdapter
    private lateinit var mTimelineRequest: TimelineRequest

    // Binding
    private var _mBinding: FragmentTimelineBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = FragmentTimelineBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Handler
        val handler = Handler()

        // Menampilkan genre
        showGenre()

        // Mendapatkan data awal timeline
        mTimelineRequest = TimelineRequest(mContext)
        getTimeline(mStartPage, false)

        // Activity
        mBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mBinding.loadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getTimeline(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mBinding.swipeRefresh.isRefreshing = true
            getTimeline(mStartPage, true)
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
        resetState()
    }

    override fun onGenreItemClick(position: Int) {
        val currentItem = mGenreList[position]
        gotoDiscoverFilm(currentItem.id, currentItem.title)
    }

    override fun onReviewItemClick(position: Int) {
        val currentItem = mReviewList[position]
        gotoReviewDetail(currentItem.id)
    }

    override fun onReviewUserProfileClick(position: Int) {
        val currentItem = mReviewList[position]
        gotoUserDetail(currentItem.userID)
    }

    override fun onReviewFilmPosterClick(position: Int) {
        val currentItem = mReviewList[position]
        gotoFilmDetail(currentItem.tmdbID)
    }

    override fun onReviewFilmPosterLongClick(position: Int) {
        val currentItem = mReviewList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.tmdbID, currentItem.title, year, currentItem.poster)
    }

    override fun onReviewLikeClick(position: Int) {
        val currentItem = mReviewList[position]
        mTotalLike = currentItem.totalLike
        if (!mIsLoading) {
            mIsLoading = true
            when (currentItem.isLiked) {
                true -> unlikeReview(currentItem.id, position)
                false -> likeReview(currentItem.id, position)
            }
        }
    }

    override fun onReviewCommentClick(position: Int) {
        val currentItem = mReviewList[position]
        gotoReviewComment(currentItem.id)
    }

    private fun showGenre() {
        loadGenre()
        setGenreAdapter()
    }

    private fun getTimeline(page: Int, refreshPage: Boolean) {
        mTimelineRequest.sendRequest(page, object : TimelineRequestCallback {
            override fun onSuccess(totalPage: Int, reviewList: ArrayList<Review>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mReviewList.clear()
                            mReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mReviewList.size
                        mReviewList.addAll(insertIndex, reviewList)
                        mReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mReviewAdapter.notifyItemRangeInserted(insertIndex, reviewList.size)
                    }
                    false -> {
                        mReviewList = ArrayList()
                        val insertIndex = mReviewList.size
                        mReviewList.addAll(insertIndex, reviewList)
                        setTimelineAdapter()
                        mBinding.progressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mBinding.errorMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mReviewList.clear()
                        mReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mBinding.progressBar.visibility = View.GONE
                }
                mBinding.errorMessage.visibility = View.VISIBLE
                mBinding.errorMessage.text = getString(R.string.empty_timeline)
            }

            override fun onError(message: String) {
                if (!mIsLoadFirstTimeSuccess) {
                    mBinding.progressBar.visibility = View.GONE
                    mBinding.errorMessage.visibility = View.VISIBLE
                    mBinding.errorMessage.text = getString(R.string.empty_timeline)
                }
                mBinding.loadMoreBar.visibility = View.GONE
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mBinding.swipeRefresh.isRefreshing = false
        mBinding.loadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun loadGenre() {
        mGenreList = ArrayList()
        LoadGenre.getAllGenre(mContext, mGenreList)
    }

    private fun setGenreAdapter() {
        val genreHorizontalAdapter = GenreHorizontalAdapter(mContext, mGenreList, this)
        mBinding.recyclerViewGenre.adapter = genreHorizontalAdapter
        mBinding.recyclerViewGenre.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mBinding.recyclerViewGenre.hasFixedSize()
        mBinding.recyclerViewGenre.visibility = View.VISIBLE
    }

    private fun setTimelineAdapter() {
        mReviewAdapter = ReviewAdapter(mContext, mReviewList, this)
        mBinding.recyclerViewTimeline.adapter = mReviewAdapter
        mBinding.recyclerViewTimeline.layoutManager = LinearLayoutManager(mContext)
        mBinding.recyclerViewTimeline.visibility = View.VISIBLE
    }

    private fun resetState() {
        mIsLoading = true
        mIsLoadFirstTimeSuccess = false
        mCurrentPage = 1
    }

    private fun likeReview(id: Int, position: Int) {
        val likeReviewRequest = LikeReviewRequest(mContext, id)
        likeReviewRequest.sendRequest(object : LikeReviewRequestCallback {
            override fun onSuccess() {
                mTotalLike++
                val currentItem = mReviewList[position]
                currentItem.isLiked = true
                currentItem.totalLike = mTotalLike
                mReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun unlikeReview(id: Int, position: Int) {
        val unlikeReviewRequest = UnlikeReviewRequest(mContext, id)
        unlikeReviewRequest.sendRequest(object : UnlikeReviewRequestCallback {
            override fun onSuccess() {
                mTotalLike--
                val currentItem = mReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(requireFragmentManager(), Popularin.FILM_MODAL)
    }

    private fun gotoDiscoverFilm(id: Int, title: String) {
        val intent = Intent(mContext, DiscoverFilmActivity::class.java)
        intent.putExtra(Popularin.GENRE_ID, id)
        intent.putExtra(Popularin.GENRE_TITLE, title)
        startActivity(intent)
    }

    private fun gotoReviewDetail(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        startActivity(intent)
    }

    private fun gotoReviewComment(id: Int) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, 1)
        startActivity(intent)
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }
}