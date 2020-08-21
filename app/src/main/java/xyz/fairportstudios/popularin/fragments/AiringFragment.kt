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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmAdapter
import xyz.fairportstudios.popularin.apis.tmdb.get.AiringFilmRequest
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class AiringFragment : Fragment(), FilmAdapter.OnClickListener {
    // Primitive
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mAiringFilmRequest: AiringFilmRequest
    private lateinit var mFilmList: ArrayList<Film>
    private lateinit var mContext: Context
    private lateinit var mFilmAdapter: FilmAdapter

    // View
    private lateinit var mAnchorLayout: CoordinatorLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerFilm: RecyclerView
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.reusable_recycler, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mAnchorLayout = view.findViewById(R.id.anchor_rr_layout)
        mProgressBar = view.findViewById(R.id.pbr_rr_layout)
        mRecyclerFilm = view.findViewById(R.id.recycler_rr_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_rr_layout)
        mTextMessage = view.findViewById(R.id.text_rr_message)

        // Mendapatkan data
        mAiringFilmRequest = AiringFilmRequest(mContext)
        getAiringFilm(false)

        // Activity
        mSwipeRefresh.setOnRefreshListener {
            mSwipeRefresh.isRefreshing = true
            getAiringFilm(true)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetState()
    }

    override fun onFilmItemClick(position: Int) {
        val currentItem = mFilmList[position]
        gotoFilmDetail(currentItem.id)
    }

    override fun onFilmPosterClick(position: Int) {
        val currentItem = mFilmList[position]
        gotoFilmDetail(currentItem.id)
    }

    override fun onFilmPosterLongClick(position: Int) {
        val currentItem = mFilmList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.id, currentItem.originalTitle, year, currentItem.posterPath)
    }

    private fun getAiringFilm(refreshPage: Boolean) {
        mAiringFilmRequest.sendRequest(object : AiringFilmRequest.Callback {
            override fun onSuccess(filmList: ArrayList<Film>) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> {
                        if (refreshPage) {
                            mFilmList.clear()
                            mFilmAdapter.notifyDataSetChanged()
                        }
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmAdapter.notifyItemRangeInserted(insertIndex, filmList.size)
                    }
                    false -> {
                        mFilmList = ArrayList()
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        setAdapter()
                        mProgressBar.visibility = View.GONE
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mTextMessage.visibility = View.GONE
            }

            override fun onNotFound() {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = getString(R.string.empty_airing_film)
            }

            override fun onError(message: String) {
                if (!mIsLoadFirstTimeSuccess) {
                    mProgressBar.visibility = View.GONE
                    mTextMessage.visibility = View.VISIBLE
                    mTextMessage.text = getString(R.string.empty_airing_film)
                }
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mSwipeRefresh.isRefreshing = false
    }

    private fun setAdapter() {
        mFilmAdapter = FilmAdapter(mContext, mFilmList, this)
        mRecyclerFilm.adapter = mFilmAdapter
        mRecyclerFilm.layoutManager = LinearLayoutManager(mContext)
        mRecyclerFilm.hasFixedSize()
        mRecyclerFilm.visibility = View.VISIBLE
    }

    private fun resetState() {
        mIsLoadFirstTimeSuccess = false
    }

    private fun showFilmModal(id: Int, title: String, year: String, poster: String) {
        val filmModal = FilmModal(id, title, year, poster)
        filmModal.show(requireFragmentManager(), Popularin.FILM_MODAL)
    }

    private fun gotoFilmDetail(id: Int) {
        val intent = Intent(mContext, FilmDetailActivity::class.java)
        intent.putExtra(Popularin.FILM_ID, id)
        startActivity(intent)
    }
}