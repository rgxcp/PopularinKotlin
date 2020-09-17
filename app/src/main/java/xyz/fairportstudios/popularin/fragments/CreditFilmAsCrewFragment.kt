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

    // Binding
    private var _mBinding: ReusableRecyclerBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Activity
        mBinding.swipeRefresh.setOnRefreshListener {
            when (mIsLoadFirstTimeSuccess) {
                true -> mBinding.swipeRefresh.isRefreshing = false
                false -> {
                    mBinding.swipeRefresh.isRefreshing = true
                    getFilmAsCrew()
                }
            }
        }

        return mBinding.root
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
        _mBinding = null
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
                        mBinding.errorMessage.visibility = View.GONE
                    }
                    false -> {
                        mBinding.errorMessage.visibility = View.VISIBLE
                        mBinding.errorMessage.text = getString(R.string.empty_credit_film_as_crew)
                    }
                }
                mBinding.progressBar.visibility = View.GONE
                mIsLoadFirstTimeSuccess = true
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.errorMessage.visibility = View.VISIBLE
                        mBinding.errorMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mBinding.swipeRefresh.isRefreshing = false
    }

    private fun setAdapter() {
        val filmGridAdapter = FilmGridAdapter(mContext, mFilmAsCrewList, this)
        mBinding.recyclerView.adapter = filmGridAdapter
        mBinding.recyclerView.layoutManager = GridLayoutManager(mContext, 4)
        mBinding.recyclerView.hasFixedSize()
        mBinding.recyclerView.visibility = View.VISIBLE
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