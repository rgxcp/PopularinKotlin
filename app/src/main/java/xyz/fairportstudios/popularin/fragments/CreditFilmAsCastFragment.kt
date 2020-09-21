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
import xyz.fairportstudios.popularin.interfaces.CreditDetailRequestCallback
import xyz.fairportstudios.popularin.interfaces.FilmGridAdapterClickListener
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class CreditFilmAsCastFragment(private val creditID: Int) : Fragment(), FilmGridAdapterClickListener {
    // Primitive
    private var mIsResumeFirstTime = true
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mFilmAsCastList: ArrayList<Film>
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
                    getFilmAsCast()
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
            mBinding.isLoading = true
            getFilmAsCast()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
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
        creditDetailRequest.sendRequest(object : CreditDetailRequestCallback {
            override fun onSuccess(creditDetail: CreditDetail, filmAsCastList: ArrayList<Film>, filmAsCrewList: ArrayList<Film>) {
                when (filmAsCastList.isNotEmpty()) {
                    true -> {
                        mFilmAsCastList = ArrayList()
                        mFilmAsCastList.addAll(filmAsCastList)
                        setAdapter()
                        mBinding.loadSuccess = true
                    }
                    false -> {
                        mBinding.loadSuccess = false
                        mBinding.message = getString(R.string.empty_credit_film_as_cast)
                    }
                }
                mBinding.isLoading = false
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
        mBinding.swipeRefresh.isRefreshing = false
    }

    private fun setAdapter() {
        val filmGridAdapter = FilmGridAdapter(mFilmAsCastList, this)
        mBinding.recyclerView.adapter = filmGridAdapter
        mBinding.recyclerView.layoutManager = GridLayoutManager(mContext, 4)
        mBinding.recyclerView.hasFixedSize()
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