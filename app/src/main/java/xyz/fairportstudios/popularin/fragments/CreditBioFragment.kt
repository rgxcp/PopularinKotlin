package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.tmdb.get.CreditDetailRequest
import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ParseBio
import xyz.fairportstudios.popularin.statics.TMDbAPI

class CreditBioFragment(private val creditID: Int) : Fragment() {
    // Variable untuk fitur onResume
    private var mIsResumeFirstTime: Boolean = true

    // Variable untuk fitur load
    private var mIsLoadFirstTimeSuccess: Boolean = false

    // Variable member
    private lateinit var mContext: Context
    private lateinit var mImageProfile: ImageView
    private lateinit var mBioLayout: LinearLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mAnchorLayout: RelativeLayout
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var mTextBio: TextView
    private lateinit var mTextMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_credit_bio, container, false)

        // Context
        mContext = requireActivity()

        // Binding
        mImageProfile = view.findViewById(R.id.image_fcb_profile)
        mBioLayout = view.findViewById(R.id.layout_fcb_bio)
        mProgressBar = view.findViewById(R.id.pbr_fcb_layout)
        mAnchorLayout = view.findViewById(R.id.anchor_fcb_layout)
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh_fcb_layout)
        mTextBio = view.findViewById(R.id.text_fcb_bio)
        mTextMessage = view.findViewById(R.id.text_fcb_message)

        // Activity
        mSwipeRefresh.setOnRefreshListener {
            when (mIsLoadFirstTimeSuccess) {
                true -> mSwipeRefresh.isRefreshing = false
                false -> {
                    mSwipeRefresh.isRefreshing = true
                    getCreditBio()
                }
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mIsResumeFirstTime) {
            // Mendapatkan data
            mIsResumeFirstTime = false
            getCreditBio()
        }
    }

    private fun getCreditBio() {
        val creditDetailRequest = CreditDetailRequest(mContext, creditID)
        creditDetailRequest.sendRequest(object : CreditDetailRequest.Callback {
            override fun onSuccess(creditDetail: CreditDetail, filmAsCastList: ArrayList<Film>, filmAsCrewList: ArrayList<Film>) {
                // Parsing
                val bio = ParseBio.getBioForHumans(creditDetail)
                val profile = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${creditDetail.profilePath}"

                // Isi
                mTextBio.text = bio
                Glide.with(mContext).load(profile).into(mImageProfile)
                mProgressBar.visibility = View.GONE
                mTextMessage.visibility = View.GONE
                mBioLayout.visibility = View.VISIBLE
                mIsLoadFirstTimeSuccess = true
            }

            override fun onError(message: String) {
                when (mIsLoadFirstTimeSuccess) {
                    true -> Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    false -> {
                        mProgressBar.visibility = View.GONE
                        mTextMessage.visibility = View.VISIBLE
                        mTextMessage.text = message
                    }
                }
            }
        })

        // Memberhentikan loading
        mSwipeRefresh.isRefreshing = false
    }
}