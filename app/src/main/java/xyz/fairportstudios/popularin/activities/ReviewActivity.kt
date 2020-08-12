package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.apis.popularin.delete.DeleteReviewRequest
import xyz.fairportstudios.popularin.fragments.ReviewCommentFragment
import xyz.fairportstudios.popularin.fragments.ReviewDetailFragment
import xyz.fairportstudios.popularin.statics.Popularin

class ReviewActivity : AppCompatActivity() {
    // Variable untuk fitur load
    private var mIsLoading: Boolean = false

    // Variable member
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_toolbar_pager)

        // Context
        val context = this

        // Binding
        mToolbar = findViewById(R.id.toolbar_rtp_layout)
        val tabLayout: TabLayout = findViewById(R.id.tab_rtp_layout)
        val viewPager: ViewPager = findViewById(R.id.pager_rtp_layout)

        // Extra
        val intent = intent
        val reviewID = intent.getIntExtra(Popularin.REVIEW_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)
        val isSelf = intent.getBooleanExtra(Popularin.IS_SELF, false)

        // Toolbar
        mToolbar.title = R.string.review.toString()
        if (isSelf) {
            addToolbarMenu(context, reviewID)
        }

        // Tab pager
        val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        pagerAdapter.addFragment(ReviewDetailFragment(reviewID), R.string.detail.toString())
        pagerAdapter.addFragment(ReviewCommentFragment(reviewID), R.string.comment.toString())
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = viewPagerIndex
        tabLayout.setupWithViewPager(viewPager)

        // Activity
        mToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun addToolbarMenu(context: Context, id: Int) {
        mToolbar.inflateMenu(R.menu.review_detail)
        mToolbar.setOnMenuItemClickListener { item ->
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
        deleteReviewRequest.sendRequest(object : DeleteReviewRequest.Callback {
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