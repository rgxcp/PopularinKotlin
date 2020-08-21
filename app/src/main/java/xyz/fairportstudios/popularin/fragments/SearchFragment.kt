package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.FilmDetailActivity
import xyz.fairportstudios.popularin.activities.UserDetailActivity
import xyz.fairportstudios.popularin.adapters.FilmAdapter
import xyz.fairportstudios.popularin.adapters.UserAdapter
import xyz.fairportstudios.popularin.apis.popularin.get.SearchUserRequest
import xyz.fairportstudios.popularin.apis.tmdb.get.SearchFilmRequest
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
    private lateinit var mSearchQuery: String

    // View
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerSearch: RecyclerView
    private lateinit var mSearchFilmRequest: SearchFilmRequest
    private lateinit var mSearchUserRequest: SearchUserRequest
    private lateinit var mTextMessage: TextView
    private lateinit var mUserAdapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mProgressBar = view.findViewById(R.id.pbr_fs_layout)
        mRecyclerSearch = view.findViewById(R.id.recycler_fs_layout)
        mTextMessage = view.findViewById(R.id.text_fs_message)
        val chipSearchInFilm = view.findViewById<Chip>(R.id.chip_fs_in_film)
        val chipSearchInUser = view.findViewById<Chip>(R.id.chip_fs_in_user)
        val searchInLayout = view.findViewById<LinearLayout>(R.id.layout_fs_search_in)
        val searchView = view.findViewById<SearchView>(R.id.search_fs_layout)

        // Activity
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Tidak digunakan
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                when (newText.isNullOrEmpty()) {
                    true -> {
                        searchInLayout.visibility = View.GONE
                        mRecyclerSearch.visibility = View.VISIBLE
                    }
                    false -> {
                        mSearchQuery = newText
                        searchInLayout.visibility = View.VISIBLE
                        mRecyclerSearch.visibility = View.GONE
                        chipSearchInFilm.text = String.format("Cari \"%s\" dalam film", mSearchQuery)
                        chipSearchInUser.text = String.format("Cari \"%s\" dalam pengguna", mSearchQuery)
                    }
                }
                mTextMessage.visibility = View.GONE
                return true
            }
        })

        chipSearchInFilm.setOnClickListener {
            if (mIsSearchFilmFirstTime) {
                mSearchFilmRequest = SearchFilmRequest(mContext)
            }
            searchInLayout.visibility = View.GONE
            mProgressBar.visibility = View.VISIBLE
            searchFilm()
        }

        chipSearchInUser.setOnClickListener {
            if (mIsSearchUserFirstTime) {
                mSearchUserRequest = SearchUserRequest(mContext)
            }
            searchInLayout.visibility = View.GONE
            mProgressBar.visibility = View.VISIBLE
            searchUser()
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
                        mRecyclerSearch.layoutManager = LinearLayoutManager(mContext)
                        mIsLoadFilmFirstTimeSuccess = true
                    }
                }
                mRecyclerSearch.adapter = mFilmAdapter
                mRecyclerSearch.visibility = View.VISIBLE
                mProgressBar.visibility = View.GONE
            }

            override fun onNotFound() {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = getString(R.string.empty_search_result)
            }

            override fun onError(message: String) {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = message
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
                        mRecyclerSearch.layoutManager = LinearLayoutManager(mContext)
                        mIsLoadUserFirstTimeSuccess = true
                    }
                }
                mRecyclerSearch.adapter = mUserAdapter
                mRecyclerSearch.visibility = View.VISIBLE
                mProgressBar.visibility = View.GONE
            }

            override fun onNotFound() {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = getString(R.string.empty_search_result)
            }

            override fun onError(message: String) {
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.VISIBLE
                mTextMessage.text = message
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