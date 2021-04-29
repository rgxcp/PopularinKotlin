package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.DeleteReviewRequest
import xyz.fairportstudios.popularin.apis.popularin.post.ReportReviewRequest
import xyz.fairportstudios.popularin.databinding.ReusableToolbarPagerBinding
import xyz.fairportstudios.popularin.fragments.ReviewCommentFragment
import xyz.fairportstudios.popularin.fragments.ReviewDetailFragment
import xyz.fairportstudios.popularin.interfaces.DeleteReviewRequestCallback
import xyz.fairportstudios.popularin.interfaces.ReportReviewRequestCallback
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.services.LoadReportCategory.getAllReportCategory
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

        // Auth
        val isAuth = Auth(context).isAuth()

        // Toolbar
        mBinding.toolbarTitle = getString(R.string.review)
        when (isSelf) {
            true -> addAuthToolbarMenu(context, reviewID)
            false -> addGuestToolbarMenu(context, isAuth, reviewID)
        }

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

    private fun addAuthToolbarMenu(context: Context, id: Int) {
        mBinding.toolbar.inflateMenu(R.menu.review_auth)
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

    private fun addGuestToolbarMenu(context: Context, isAuth: Boolean, reviewId: Int) {
        mBinding.toolbar.inflateMenu(R.menu.review_guest)
        mBinding.toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.menu_rg_report -> {
                    when (isAuth) {
                        true -> showPickReportCategoryDialog(context, reviewId)
                        false -> gotoEmptyAccount(context)
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

    private fun showPickReportCategoryDialog(context: Context, reviewId: Int) {
        val reportCategories = getAllReportCategory(context)
        var pickedReportCategory = 0

        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.pick_report_category_dialog_title))
            .setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(getString(R.string.pick)) { _, _ ->
                reportReview(context, reviewId, pickedReportCategory + 1)
            }
            .setSingleChoiceItems(reportCategories, pickedReportCategory) { _, which ->
                pickedReportCategory = which
            }
            .show()
    }

    private fun reportReview(context: Context, reviewId: Int, reportCategoryId: Int) {
        val reportReviewRequest = ReportReviewRequest(context, reviewId, reportCategoryId)
        reportReviewRequest.sendRequest(object : ReportReviewRequestCallback {
            override fun onSuccess() {
                Toast.makeText(context, getString(R.string.review_reported), Toast.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun gotoEmptyAccount(context: Context) {
        val intent = Intent(context, EmptyAccountActivity::class.java)
        startActivity(intent)
    }
}