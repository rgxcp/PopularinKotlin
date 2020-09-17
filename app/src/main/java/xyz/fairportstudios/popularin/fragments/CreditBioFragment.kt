package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.apis.tmdb.get.CreditDetailRequest
import xyz.fairportstudios.popularin.databinding.FragmentCreditBioBinding
import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseBio

class CreditBioFragment(private val creditID: Int) : Fragment() {
    // Primitive
    private var mIsResumeFirstTime = true
    private var mIsLoadFirstTimeSuccess = false

    // Member
    private lateinit var mContext: Context

    // Binding
    private var _mBinding: FragmentCreditBioBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = FragmentCreditBioBinding.inflate(inflater, container, false)

        // Context
        mContext = requireActivity()

        // Activity
        mBinding.swipeRefresh.setOnRefreshListener {
            when (mIsLoadFirstTimeSuccess) {
                true -> mBinding.swipeRefresh.isRefreshing = false
                false -> {
                    mBinding.swipeRefresh.isRefreshing = true
                    getCreditBio()
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
            getCreditBio()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    private fun getCreditBio() {
        val creditDetailRequest = CreditDetailRequest(mContext, creditID)
        creditDetailRequest.sendRequest(object : CreditDetailRequest.Callback {
            override fun onSuccess(creditDetail: CreditDetail, filmAsCastList: ArrayList<Film>, filmAsCrewList: ArrayList<Film>) {
                mBinding.profilePath = creditDetail.profilePath
                mBinding.bioForHumans = ParseBio.getBioForHumans(creditDetail)
                mBinding.isLoading = false
                mBinding.loadSuccess = true
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
}