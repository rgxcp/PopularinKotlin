package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Genre
import xyz.fairportstudios.popularin.models.Review
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class TimelineFragment : Fragment(), GenreHorizontalAdapter.OnClickListener, ReviewAdapter.OnClickListener {
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

    // View
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLoadMoreBar: ProgressBar
    private lateinit var mRecyclerGenre: RecyclerView
    private lateinit var mRecyclerTimeline: RecyclerView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_timeline, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mAnchorLayout = view.findViewById(R.id.anchor_ft_layout)
        mProgressBar = view.findViewById(R.id.pbr_ft_layout)
        mLoadMoreBar = view.findViewById(R.id.lbr_ft_layout)
        mRecyclerGenre = view.findViewById(R.id.recycler_ft_genre)
        mRecyclerTimeline = view.findViewById(R.id.recycler_ft_timeline)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_ft_layout)
        mTextMessage = view.findViewById(R.id.text_ft_message)
        val nestedScrollView = view.findViewById<NestedScrollView>(R.id.nested_scroll_ft_layout)

        // Handler
        val handler = Handler()

        // Menampilkan genre
        showGenre()

        // Mendapatkan data awal timeline
        mTimelineRequest = TimelineRequest(mContext)
        getTimeline(mStartPage, false)

        // Activity
        nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                if (!mIsLoading && mCurrentPage <= mTotalPage) {
                    mIsLoading = true
                    mLoadMoreBar.visibility = View.VISIBLE
                    handler.postDelayed({
                        getTimeline(mCurrentPage, false)
                    }, 1000)
                }
            }
        }

        mSwipeRefresh.setOnRefreshListener {
            mIsLoading = true
            mSwipeRefresh.isRefreshing = true
            getTimeline(mStartPage, true)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        showFilmModal(currentItem.id, currentItem.title, year, currentItem.poster)
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
        mTimelineRequest.sendRequest(page, object : TimelineRequest.Callback {
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
                        mProgressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mTextMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mReviewList.clear()
                        mReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mProgressBar.visibility = View.GONE
                }
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = getString(R.string.empty_timeline)
            }

            override fun onError(message: String) {
                if (!mIsLoadFirstTimeSuccess) {
                    mProgressBar.visibility = View.GONE
                    mTextMessage.visibility = View.VISIBLE
                    mTextMessage.text = getString(R.string.empty_timeline)
                }
                mLoadMoreBar.visibility = View.GONE
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
        if (refreshPage) mSwipeRefresh.isRefreshing = false
        mLoadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun loadGenre() {
        mGenreList = ArrayList()
        mGenreList.add(Genre(28, R.drawable.img_action, getString(R.string.genre_action)))
        mGenreList.add(Genre(16, R.drawable.img_animation, getString(R.string.genre_animation)))
        mGenreList.add(Genre(99, R.drawable.img_documentary, getString(R.string.genre_documentary)))
        mGenreList.add(Genre(18, R.drawable.img_drama, getString(R.string.genre_drama)))
        mGenreList.add(Genre(14, R.drawable.img_fantasy, getString(R.string.genre_fantasy)))
        mGenreList.add(Genre(878, R.drawable.img_fiction, getString(R.string.genre_fiction)))
        mGenreList.add(Genre(27, R.drawable.img_horror, getString(R.string.genre_horror)))
        mGenreList.add(Genre(80, R.drawable.img_crime, getString(R.string.genre_crime)))
        mGenreList.add(Genre(10751, R.drawable.img_family, getString(R.string.genre_family)))
        mGenreList.add(Genre(35, R.drawable.img_comedy, getString(R.string.genre_comedy)))
        mGenreList.add(Genre(9648, R.drawable.img_mystery, getString(R.string.genre_mystery)))
        mGenreList.add(Genre(10752, R.drawable.img_war, getString(R.string.genre_war)))
        mGenreList.add(Genre(12, R.drawable.img_adventure, getString(R.string.genre_adventure)))
        mGenreList.add(Genre(10749, R.drawable.img_romance, getString(R.string.genre_romance)))
        mGenreList.add(Genre(36, R.drawable.img_history, getString(R.string.genre_history)))
        mGenreList.add(Genre(53, R.drawable.img_thriller, getString(R.string.genre_thriller)))
    }

    private fun setGenreAdapter() {
        val genreHorizontalAdapter = GenreHorizontalAdapter(mContext, mGenreList, this)
        mRecyclerGenre.adapter = genreHorizontalAdapter
        mRecyclerGenre.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mRecyclerGenre.hasFixedSize()
        mRecyclerGenre.visibility = View.VISIBLE
    }

    private fun setTimelineAdapter() {
        mReviewAdapter = ReviewAdapter(mContext, mReviewList, this)
        mRecyclerTimeline.adapter = mReviewAdapter
        mRecyclerTimeline.layoutManager = LinearLayoutManager(mContext)
        mRecyclerTimeline.visibility = View.VISIBLE
    }

    private fun resetState() {
        mIsLoading = true
        mIsLoadFirstTimeSuccess = false
        mCurrentPage = 1
    }

    private fun likeReview(id: Int, position: Int) {
        val likeReviewRequest = LikeReviewRequest(mContext, id)
        likeReviewRequest.sendRequest(object : LikeReviewRequest.Callback {
            override fun onSuccess() {
                mTotalLike++
                val currentItem = mReviewList[position]
                currentItem.isLiked = true
                currentItem.totalLike = mTotalLike
                mReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun unlikeReview(id: Int, position: Int) {
        val unlikeReviewRequest = UnlikeReviewRequest(mContext, id)
        unlikeReviewRequest.sendRequest(object : UnlikeReviewRequest.Callback {
            override fun onSuccess() {
                mTotalLike--
                val currentItem = mReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
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