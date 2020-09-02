package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.FilmReviewAdapter
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.UnlikeReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.get.FilmReviewFromAllRequest
import xyz.fairportstudios.popularin.apis.popularin.post.LikeReviewRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarPagerBinding
import xyz.fairportstudios.popularin.databinding.ReusableToolbarRecyclerBinding
import xyz.fairportstudios.popularin.fragments.LikedReviewFragment
import xyz.fairportstudios.popularin.fragments.ReviewFromAllFragment
import xyz.fairportstudios.popularin.fragments.ReviewFromFollowingFragment
import xyz.fairportstudios.popularin.fragments.SelfReviewFragment
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class FilmReviewActivity : AppCompatActivity(), FilmReviewAdapter.OnClickListener {
    // Primitive
    private var mIsAuth = false
    private var mIsLoading = true
    private var mIsLoadFirstTimeSuccess = false
    private val mStartPage = 1
    private var mCurrentPage = 1
    private var mTotalPage = 0
    private var mTotalLike = 0

    // Member
    private lateinit var mFilmReviewList: ArrayList<FilmReview>
    private lateinit var mAuth: Auth
    private lateinit var mContext: Context
    private lateinit var mFilmReviewAdapter: FilmReviewAdapter
    private lateinit var mFilmReviewFromAllRequest: FilmReviewFromAllRequest

    // View binding
    private lateinit var mViewBinding: ReusableToolbarRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Context
        mContext = this

        // Extra
        val filmID = intent.getIntExtra(Popularin.FILM_ID, 0)

        // Auth
        mAuth = Auth(mContext)
        mIsAuth = mAuth.isAuth()

        // Menampilkan layout berdasarkan kondisi
        when (mIsAuth) {
            true -> {
                val viewBinding = ReusableToolbarPagerBinding.inflate(layoutInflater)
                setContentView(viewBinding.root)

                // Toolbar
                viewBinding.toolbar.title = getString(R.string.review)

                // Tab pager
                val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
                pagerAdapter.addFragment(ReviewFromAllFragment(filmID), getString(R.string.all))
                pagerAdapter.addFragment(ReviewFromFollowingFragment(filmID), getString(R.string.following))
                pagerAdapter.addFragment(LikedReviewFragment(filmID), getString(R.string.liked))
                pagerAdapter.addFragment(SelfReviewFragment(filmID), getString(R.string.self))
                viewBinding.viewPager.adapter = pagerAdapter
                viewBinding.viewPager.offscreenPageLimit = 4
                viewBinding.tabLayout.tabMode = TabLayout.MODE_AUTO
                viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)

                // Activity
                viewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
            }
            false -> {
                mViewBinding = ReusableToolbarRecyclerBinding.inflate(layoutInflater)
                setContentView(mViewBinding.root)

                // Handler
                val handler = Handler()

                // Toolbar
                mViewBinding.toolbar.title = getString(R.string.review)

                // Mendapatkan data awal
                mFilmReviewFromAllRequest = FilmReviewFromAllRequest(mContext, filmID)
                getFilmReviewFromAll(mStartPage, false)

                // Activity
                mViewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }

                mViewBinding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                    if (scrollY > oldScrollY) {
                        if (!mIsLoading && mCurrentPage <= mTotalPage) {
                            mIsLoading = true
                            mViewBinding.loadMoreBar.visibility = View.VISIBLE
                            handler.postDelayed({
                                getFilmReviewFromAll(mCurrentPage, false)
                            }, 1000)
                        }
                    }
                }

                mViewBinding.swipeRefresh.setOnRefreshListener {
                    mIsLoading = true
                    mViewBinding.swipeRefresh.isRefreshing = true
                    getFilmReviewFromAll(mStartPage, true)
                }
            }
        }
    }

    override fun onFilmReviewItemClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewDetail(currentItem.id, isSelf)
    }

    override fun onFilmReviewUserProfileClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        gotoUserDetail(currentItem.userID)
    }

    override fun onFilmReviewLikeClick(position: Int) {
        when (mIsAuth) {
            true -> {
                val currentItem = mFilmReviewList[position]
                mTotalLike = currentItem.totalLike
                if (!mIsLoading) {
                    mIsLoading = true
                    when (currentItem.isLiked) {
                        true -> unlikeReview(currentItem.id, position)
                        false -> likeReview(currentItem.id, position)
                    }
                }
            }
            false -> gotoEmptyAccount()
        }
    }

    override fun onFilmReviewCommentClick(position: Int) {
        val currentItem = mFilmReviewList[position]
        val isSelf = mAuth.isSelf(currentItem.userID, mAuth.getAuthID())
        gotoReviewComment(currentItem.id, isSelf)
    }

    private fun getFilmReviewFromAll(page: Int, refreshPage: Boolean) {
        mFilmReviewFromAllRequest.sendRequest(page, object : FilmReviewFromAllRequest.Callback {
            override fun onSuccess(totalPage: Int, filmReviewList: ArrayList<FilmReview>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mCurrentPage = 1
                            mTotalPage = totalPage
                            mFilmReviewList.clear()
                            mFilmReviewAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmReviewList.size
                        mFilmReviewList.addAll(insertIndex, filmReviewList)
                        mFilmReviewAdapter.notifyItemChanged(insertIndex - 1)
                        mFilmReviewAdapter.notifyItemRangeInserted(insertIndex, filmReviewList.size)
                    }
                    false -> {
                        mFilmReviewList = ArrayList()
                        val insertIndex = mFilmReviewList.size
                        mFilmReviewList.addAll(insertIndex, filmReviewList)
                        setAdapter()
                        mViewBinding.progressBar.visibility = View.GONE
                        mTotalPage = totalPage
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mViewBinding.errorMessage.visibility = View.GONE
                mCurrentPage++
            }

            override fun onNotFound() {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mCurrentPage = 1
                        mFilmReviewList.clear()
                        mFilmReviewAdapter.notifyDataSetChanged()
                    }
                    false -> mViewBinding.progressBar.visibility = View.GONE
                }
                mViewBinding.errorMessage.visibility = View.VISIBLE
                mViewBinding.errorMessage.text = getString(R.string.empty_film_review)
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        mViewBinding.loadMoreBar.visibility = View.GONE
                        Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
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
        if (refreshPage) mViewBinding.swipeRefresh.isRefreshing = false
        mViewBinding.loadMoreBar.visibility = when (page == mTotalPage) {
            true -> View.GONE
            false -> View.INVISIBLE
        }
    }

    private fun setAdapter() {
        mFilmReviewAdapter = FilmReviewAdapter(mContext, mFilmReviewList, this)
        mViewBinding.recyclerView.adapter = mFilmReviewAdapter
        mViewBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
        mViewBinding.recyclerView.visibility = View.VISIBLE
    }

    private fun likeReview(id: Int, position: Int) {
        val likeReviewRequest = LikeReviewRequest(mContext, id)
        likeReviewRequest.sendRequest(object : LikeReviewRequest.Callback {
            override fun onSuccess() {
                mTotalLike++
                val currentItem = mFilmReviewList[position]
                currentItem.isLiked = true
                currentItem.totalLike = mTotalLike
                mFilmReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
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
                val currentItem = mFilmReviewList[position]
                currentItem.isLiked = false
                currentItem.totalLike = mTotalLike
                mFilmReviewAdapter.notifyItemChanged(position)
            }

            override fun onError(message: String) {
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }

    private fun gotoReviewDetail(id: Int, isSelf: Boolean) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, isSelf)
        startActivity(intent)
    }

    private fun gotoReviewComment(id: Int, isSelf: Boolean) {
        val intent = Intent(mContext, ReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        intent.putExtra(Popularin.IS_SELF, isSelf)
        intent.putExtra(Popularin.VIEW_PAGER_INDEX, 1)
        startActivity(intent)
    }

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }

    private fun gotoEmptyAccount() {
        val intent = Intent(mContext, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}