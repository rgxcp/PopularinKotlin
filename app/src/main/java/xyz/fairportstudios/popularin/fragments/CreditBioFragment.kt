package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.apis.tmdb.get.CreditDetailRequest
import xyz.fairportstudios.popularin.databinding.FragmentCreditBioBinding
import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseBio
import xyz.fairportstudios.popularin.statics.TMDbAPI

class CreditBioFragment(private val creditID: Int) : Fragment() {
    // Primitive
    private var mIsResumeFirstTime = true
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mContext: Context

    // View binding
    private var _mViewBinding: FragmentCreditBioBinding? = null
    private val mViewBinding get() = _mViewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mViewBinding = FragmentCreditBioBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Activity
        mViewBinding.swipeRefresh.setOnRefreshListener {
            when (mIsLoadFirstTimeSuccess) {
                true -> mViewBinding.swipeRefresh.isRefreshing = false
                false -> {
                    mViewBinding.swipeRefresh.isRefreshing = true
                    getCreditBio()
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
            getCreditBio()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mViewBinding = null
    }

    private fun getCreditBio() {
        val creditDetailRequest = CreditDetailRequest(mContext, creditID)
        creditDetailRequest.sendRequest(object : CreditDetailRequest.Callback {
            override fun onSuccess(creditDetail: CreditDetail, filmAsCastList: ArrayList<Film>, filmAsCrewList: ArrayList<Film>) {
                // Parsing
                val bio = ParseBio.getBioForHumans(creditDetail)
                val profile = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${creditDetail.profilePath}"

                // Isi
                mViewBinding.bio.text = bio
                Glide.with(mContext).load(profile).into(mViewBinding.creditProfile)
                mViewBinding.progressBar.visibility = View.GONE
                mViewBinding.errorMessage.visibility = View.GONE
                mViewBinding.bioLayout.visibility = View.VISIBLE
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
}