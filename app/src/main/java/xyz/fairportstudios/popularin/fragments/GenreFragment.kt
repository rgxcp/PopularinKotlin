package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.DiscoverFilmActivity
import xyz.fairportstudios.popularin.adapters.GenreGridAdapter
import xyz.fairportstudios.popularin.databinding.ReusableRecyclerBinding
import xyz.fairportstudios.popularin.models.Genre
import xyz.fairportstudios.popularin.statics.Popularin

class GenreFragment : Fragment(), GenreGridAdapter.OnClickListener {
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