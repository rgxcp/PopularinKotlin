package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmAdapter
import xyz.fairportstudios.popularin.adapters.UserAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.SearchUserRequest
import xyz.fairportstudios.popularin.apis.tmdb.get.SearchFilmRequest
import xyz.fairportstudios.popularin.databinding.FragmentSearchBinding
import xyz.fairportstudios.popularin.modals.FilmModal
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.Popularin

class SearchFragment : Fragment(), FilmAdapter.OnClickListener, UserAdapter.OnClickListener {
    // Primitive
    private var mIsSearchFilmFirstTime = true
    private var mIsSearchUserFirstTime = true
    private var mIsLoadFilmFirstTimeSuccess = false
    private var mIsLoadUserFirstTimeSuccess = false

    // Member
    private lateinit var mFilmList: ArrayList<Film>
    private lateinit var mUserList: ArrayList<User>
    private lateinit var mContext: Context
    private lateinit var mFilmAdapter: FilmAdapter
    private lateinit var mSearchFilmRequest: SearchFilmRequest
    private lateinit var mSearchUserRequest: SearchUserRequest
    private lateinit var mSearchQuery: String
    private lateinit var mUserAdapter: UserAdapter

    // View binding
    private var _mViewBinding: FragmentSearchBinding? = null
    private val mViewBinding get() = _mViewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mViewBinding = FragmentSearchBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Activity
        mViewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Tidak digunakan
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                when (newText.isNullOrEmpty()) {
                    true -> {
                        mViewBinding.searchInLayout.visibility = View.GONE
                        mViewBinding.recyclerView.visibility = View.VISIBLE
                    }
                    false -> {
                        mSearchQuery = newText
                        mViewBinding.searchInLayout.visibility = View.VISIBLE
                        mViewBinding.recyclerView.visibility = View.GONE
                        mViewBinding.inFilmChip.text = String.format("Cari \"%s\" dalam film", mSearchQuery)
                        mViewBinding.inUserChip.text = String.format("Cari \"%s\" dalam pengguna", mSearchQuery)
                    }
                }
                mViewBinding.errorMessage.visibility = View.GONE
                return true
            }
        })

        mViewBinding.inFilmChip.setOnClickListener {
            if (mIsSearchFilmFirstTime) {
                mSearchFilmRequest = SearchFilmRequest(mContext)
            }
            mViewBinding.searchInLayout.visibility = View.GONE
            mViewBinding.progressBar.visibility = View.VISIBLE
            searchFilm()
        }

        mViewBinding.inUserChip.setOnClickListener {
            if (mIsSearchUserFirstTime) {
                mSearchUserRequest = SearchUserRequest(mContext)
            }
            mViewBinding.searchInLayout.visibility = View.GONE
            mViewBinding.progressBar.visibility = View.VISIBLE
            searchUser()
        }

        return mViewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mViewBinding = null
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

    override fun onUserItemClick(position: Int) {
        val currentItem = mUserList[position]
        gotoUserDetail(currentItem.id)
    }

    private fun searchFilm() {
        mSearchFilmRequest.sendRequest(mSearchQuery, object : SearchFilmRequest.Callback {
            override fun onSuccess(filmList: ArrayList<Film>) {
                when (mIsSearchFilmFirstTime || mIsLoadFilmFirstTimeSuccess) {
                    true -> {
                        mFilmList.clear()
                        mFilmAdapter.notifyDataSetChanged()
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmAdapter.notifyItemRangeInserted(insertIndex, filmList.size)
                    }
                    false -> {
                        mFilmList = ArrayList()
                        val insertIndex = mFilmList.size
                        mFilmList.addAll(insertIndex, filmList)
                        mFilmAdapter = FilmAdapter(mContext, mFilmList, this@SearchFragment)
                        mViewBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
                        mIsLoadFilmFirstTimeSuccess = true
                    }
                }
                mViewBinding.recyclerView.adapter = mFilmAdapter
                mViewBinding.recyclerView.visibility = View.VISIBLE
                mViewBinding.progressBar.visibility = View.GONE
            }

            override fun onNotFound() {
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.errorMessage.visibility = View.VISIBLE
                mViewBinding.errorMessage.text = getString(R.string.empty_search_result)
            }

            override fun onError(message: String) {
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.errorMessage.visibility = View.VISIBLE
                mViewBinding.errorMessage.text = message
            }
        })

        mIsSearchFilmFirstTime = false
    }

    private fun searchUser() {
        mSearchUserRequest.sendRequest(mSearchQuery, object : SearchUserRequest.Callback {
            override fun onSuccess(userList: ArrayList<User>) {
                when (mIsSearchUserFirstTime || mIsLoadUserFirstTimeSuccess) {
                    true -> {
                        mUserList.clear()
                        mUserAdapter.notifyDataSetChanged()
                        val insertIndex = mUserList.size
                        mUserList.addAll(insertIndex, userList)
                        mUserAdapter.notifyItemRangeInserted(insertIndex, userList.size)
                    }
                    false -> {
                        mUserList = ArrayList()
                        val insertIndex = mUserList.size
                        mUserList.addAll(insertIndex, userList)
                        mUserAdapter = UserAdapter(mContext, mUserList, this@SearchFragment)
                        mViewBinding.recyclerView.layoutManager = LinearLayoutManager(mContext)
                        mIsLoadUserFirstTimeSuccess = true
                    }
                }
                mViewBinding.recyclerView.adapter = mUserAdapter
                mViewBinding.recyclerView.visibility = View.VISIBLE
                mViewBinding.progressBar.visibility = View.GONE
            }

            override fun onNotFound() {
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.errorMessage.visibility = View.VISIBLE
                mViewBinding.errorMessage.text = getString(R.string.empty_search_result)
            }

            override fun onError(message: String) {
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.errorMessage.visibility = View.VISIBLE
                mViewBinding.errorMessage.text = message
            }
        })

        mIsSearchUserFirstTime = false
    }

    private fun resetState() {
        mIsSearchFilmFirstTime = true
        mIsSearchUserFirstTime = true
        mIsLoadFilmFirstTimeSuccess = false
        mIsLoadUserFirstTimeSuccess = false
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

    private fun gotoUserDetail(id: Int) {
        val intent = Intent(mContext, UserDetailActivity::class.java)
        intent.putExtra(Popularin.USER_ID, id)
        startActivity(intent)
    }
}