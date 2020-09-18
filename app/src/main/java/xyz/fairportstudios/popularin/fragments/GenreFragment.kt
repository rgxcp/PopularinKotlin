package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import xyz.fairportstudios.popularin.activities.DiscoverFilmActivity
import xyz.fairportstudios.popularin.adapters.GenreGridAdapter
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
import xyz.fairportstudios.popularin.interfaces.GenreGridAdapterClickListener
import xyz.fairportstudios.popularin.models.Genre
import xyz.fairportstudios.popularin.services.LoadGenre
import xyz.fairportstudios.popularin.statics.Popularin

class GenreFragment : Fragment(), GenreGridAdapterClickListener {
    // Member
    private lateinit var mGenreList: ArrayList<Genre>
    private lateinit var mContext: Context

    // Binding
    private var _mBinding: ReusableRecyclerBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = ReusableRecyclerBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Menampilkan genre
        showGenre()

        // Activity
        mBinding.swipeRefresh.setOnRefreshListener { mBinding.swipeRefresh.isRefreshing = false }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    override fun onGenreItemClick(position: Int) {
        val currentItem = mGenreList[position]
        gotoDiscoverFilm(currentItem.id, currentItem.title)
    }

    private fun showGenre() {
        loadGenre()
        setAdapter()
        mBinding.progressBar.visibility = View.GONE
    }

    private fun loadGenre() {
        mGenreList = ArrayList()
        LoadGenre.getAllGenre(mContext, mGenreList)
    }

    private fun setAdapter() {
        val genreGridAdapter = GenreGridAdapter(mContext, mGenreList, this)
        mBinding.recyclerView.adapter = genreGridAdapter
        mBinding.recyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mBinding.recyclerView.hasFixedSize()
        mBinding.recyclerView.visibility = View.VISIBLE
    }

    private fun gotoDiscoverFilm(id: Int, title: String) {
        val intent = Intent(mContext, DiscoverFilmActivity::class.java)
        intent.putExtra(Popularin.GENRE_ID, id)
        intent.putExtra(Popularin.GENRE_TITLE, title)
        startActivity(intent)
    }
}