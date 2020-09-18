package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.DeleteReviewRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarPagerBinding
import xyz.fairportstudios.popularin.fragments.ReviewCommentFragment
import xyz.fairportstudios.popularin.fragments.ReviewDetailFragment
import xyz.fairportstudios.popularin.interfaces.DeleteReviewRequestCallback
import xyz.fairportstudios.popularin.statics.Popularin

class ReviewActivity : AppCompatActivity() {
    // Primitive
    private var mIsLoading = false

    // Binding
    private lateinit var mBinding: ReusableToolbarPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ReusableToolbarPagerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Context
        val context = this

        // Extra
        val reviewID = intent.getIntExtra(Popularin.REVIEW_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)
        val isSelf = intent.getBooleanExtra(Popularin.IS_SELF, false)

        // Toolbar
        mBinding.toolbar.title = getString(R.string.review)
        if (isSelf) addToolbarMenu(context, reviewID)

        // Tab pager
        val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        pagerAdapter.addFragment(ReviewDetailFragment(reviewID), getString(R.string.detail))
        pagerAdapter.addFragment(ReviewCommentFragment(reviewID), getString(R.string.comment))
        mBinding.viewPager.adapter = pagerAdapter
        mBinding.viewPager.currentItem = viewPagerIndex
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager)

        // Activity
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun addToolbarMenu(context: Context, id: Int) {
        mBinding.toolbar.inflateMenu(R.menu.review_detail)
        mBinding.toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.menu_rd_edit -> {
                    editReview(context, id)
                    true
                }
                R.id.menu_rd_delete -> {
                    if (!mIsLoading) {
                        mIsLoading = true
                        deleteReview(context, id)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun editReview(context: Context, id: Int) {
        val intent = Intent(context, EditReviewActivity::class.java)
        intent.putExtra(Popularin.REVIEW_ID, id)
        startActivity(intent)
    }

    private fun deleteReview(context: Context, id: Int) {
        val deleteReviewRequest = DeleteReviewRequest(context, id)
        deleteReviewRequest.sendRequest(object : DeleteReviewRequestCallback {
            override fun onSuccess() {
                onBackPressed()
                Toast.makeText(context, R.string.review_deleted, Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}