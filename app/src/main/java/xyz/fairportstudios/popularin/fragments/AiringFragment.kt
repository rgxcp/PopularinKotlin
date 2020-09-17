package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmAdapter
import xyz.fairportstudios.popularin.apis.tmdb.get.AiringFilmRequest
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
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

    // Binding
    private var _mBinding: ReusableRecyclerBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Mendapatkan data
        mAiringFilmRequest = AiringFilmRequest(mContext)
        getAiringFilm(false)

        // Activity
        mBinding.swipeRefresh.setOnRefreshListener {
            mBinding.swipeRefresh.isRefreshing = true
            getAiringFilm(true)
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
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
                        mBinding.progressBar.visibility = View.GONE
                        mIsLoadFirstTimeSuccess = true
                    }
                }
                mBinding.errorMessage.visibility = View.GONE
            }

            override fun onNotFound() {
                mBinding.progressBar.visibility = View.GONE
                mBinding.errorMessage.visibility = View.VISIBLE
                mBinding.errorMessage.text = getString(R.string.empty_airing_film)
            }

            override fun onError(message: String) {
                if (!mIsLoadFirstTimeSuccess) {
                    mBinding.progressBar.visibility = View.GONE
                    mBinding.errorMessage.visibility = View.VISIBLE
                    mBinding.errorMessage.text = getString(R.string.empty_airing_film)
                }
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mBinding.swipeRefresh.isRefreshing = false
    }

    private fun setAdapter() {
        mFilmAdapter = FilmAdapter(mContext, mFilmList, this)
        mBinding.recyclerView.adapter = mFilmAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
        mBinding.recyclerView.hasFixedSize()
        mBinding.recyclerView.visibility = View.VISIBLE
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