package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmGridAdapter
import xyz.fairportstudios.popularin.apis.tmdb.get.CreditDetailRequest
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class CreditFilmAsCrewFragment(private val creditID: Int) : Fragment(), FilmGridAdapter.OnClickListener {
    // Primitive
    private var mIsResumeFirstTime = true
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mFilmAsCrewList: ArrayList<Film>
    private lateinit var mContext: Context

    // View binding
    private var _mViewBinding: ReusableRecyclerBinding? = null
    private val mViewBinding get() = _mViewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mViewBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Activity
        mViewBinding.swipeRefresh.setOnRefreshListener {
            when (mIsLoadFirstTimeSuccess) {
                true -> mViewBinding.swipeRefresh.isRefreshing = false
                false -> {
                    mViewBinding.swipeRefresh.isRefreshing = true
                    getFilmAsCrew()
                }
            }
        }

        return mViewBinding.root
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data
            mIsResumeFirstTime = false
            getFilmAsCrew()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mViewBinding = null
    }

    override fun onFilmGridItemClick(position: Int) {
        val currentItem = mFilmAsCrewList[position]
        gotoFilmDetail(currentItem.id)
    }

    override fun onFilmGridItemLongClick(position: Int) {
        val currentItem = mFilmAsCrewList[position]
        val year = ParseDate.getYear(currentItem.releaseDate)
        showFilmModal(currentItem.id, currentItem.originalTitle, year, currentItem.posterPath)
    }

    private fun getFilmAsCrew() {
        val creditDetailRequest = CreditDetailRequest(mContext, creditID)
        creditDetailRequest.sendRequest(object : CreditDetailRequest.Callback {
            override fun onSuccess(creditDetail: CreditDetail, filmAsCastList: ArrayList<Film>, filmAsCrewList: ArrayList<Film>) {
                when (filmAsCrewList.isNotEmpty()) {
                    true -> {
                        mFilmAsCrewList = ArrayList()
                        mFilmAsCrewList.addAll(filmAsCrewList)
                        setAdapter()
                        mViewBinding.errorMessage.visibility = View.GONE
                    }
                    false -> {
                        mViewBinding.errorMessage.visibility = View.VISIBLE
                        mViewBinding.errorMessage.text = getString(R.string.empty_credit_film_as_crew)
                    }
                }
                mViewBinding.progressBar.visibility = View.GONE
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
        mViewBinding.swipeRefresh.isRefreshing = false
    }

    private fun setAdapter() {
        val filmGridAdapter = FilmGridAdapter(mContext, mFilmAsCrewList, this)
        mViewBinding.recyclerView.adapter = filmGridAdapter
        mViewBinding.recyclerView.layoutManager = GridLayoutManager(mContext, 4)
        mViewBinding.recyclerView.hasFixedSize()
        mViewBinding.recyclerView.visibility = View.VISIBLE
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