package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmGridAdapter
import xyz.fairportstudios.popularin.apis.tmdb.get.CreditDetailRequest
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class CreditFilmAsCastFragment(private val creditID: Int) : Fragment(), FilmGridAdapter.OnClickListener {
    // Variable untuk fitur onResume
    private var mIsResumeFirstTime: Boolean = true

    // Variable untuk fitur load
    private var mIsLoadFirstTimeSuccess: Boolean = false

    // Variable member
    private lateinit var mContext: Context
    private lateinit var mFilmAsCastList: ArrayList<Film>
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerFilm: RecyclerView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.reusable_recycler, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mAnchorLayout = view.findViewById(R.id.anchor_rr_layout)
        mProgressBar = view.findViewById(R.id.pbr_rr_layout)
        mRecyclerFilm = view.findViewById(R.id.recycler_rr_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_rr_layout)
        mTextMessage = view.findViewById(R.id.text_rr_message)

        // Activity
        mSwipeRefresh.setOnRefreshListener {
            when (mIsLoadFirstTimeSuccess) {
                true -> mSwipeRefresh.isRefreshing = false
                false -> {
                    mSwipeRefresh.isRefreshing = true
                    getFilmAsCast()
                }
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data
            mIsResumeFirstTime = false
            getFilmAsCast()
        }
    }

    override fun onFilmGridItemClick(position: Int) {
        val currentItem = mFilmAsCastList[position]
        gotoFilmDetail(currentItem.id)
    }

    override fun onFilmGridItemLongClick(position: Int) {
        val currentItem = mFilmAsCastList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.id, currentItem.originalTitle, year, currentItem.posterPath)
    }

    private fun getFilmAsCast() {
        val creditDetailRequest = CreditDetailRequest(mContext, creditID)
        creditDetailRequest.sendRequest(object : CreditDetailRequest.Callback {
            override fun onSuccess(creditDetail: CreditDetail, filmAsCastList: ArrayList<Film>, filmAsCrewList: ArrayList<Film>) {
                when (filmAsCastList.isNotEmpty()) {
                    true -> {
                        mFilmAsCastList = ArrayList()
                        mFilmAsCastList.addAll(filmAsCastList)
                        val filmGridAdapter = FilmGridAdapter(mContext, mFilmAsCastList, this@CreditFilmAsCastFragment)
                        mRecyclerFilm.adapter = filmGridAdapter
                        mRecyclerFilm.layoutManager = GridLayoutManager(mContext, 4)
                        mRecyclerFilm.hasFixedSize()
                        mRecyclerFilm.visibility = View.VISIBLE
                        mTextMessage.visibility = View.GONE
                    }
                    false -> {
                        mTextMessage.visibility = View.VISIBLE
                        mTextMessage.text = R.string.empty_credit_film_as_cast.toString()
                    }
                }
                mProgressBar.visibility = View.GONE
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
        mSwipeRefresh.isRefreshing = false
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val fragmentManager = (mContext as FragmentActivity).supportFragmentManager
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(fragmentManager, Popularin.FILM_MODAL)
    }
}