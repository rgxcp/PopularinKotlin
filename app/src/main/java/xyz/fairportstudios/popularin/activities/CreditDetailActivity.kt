package xyz.fairportstudios.popularin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.fragments.CreditBioFragment
import xyz.fairportstudios.popularin.fragments.CreditFilmAsCastFragment
import xyz.fairportstudios.popularin.fragments.CreditFilmAsCrewFragment
import xyz.fairportstudios.popularin.statics.Popularin

class CreditDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_toolbar_pager)

        // Binding
        val tabLayout: TabLayout = findViewById(R.id.tab_rtp_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar_rtp_layout)
        val viewPager: ViewPager = findViewById(R.id.pager_rtp_layout)

        // Extra
        val intent = intent
        val creditID = intent.getIntExtra(Popularin.CREDIT_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)

        // Toolbar
        toolbar.title = R.string.credit.toString()

        // Tab pager
        val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        pagerAdapter.addFragment(CreditBioFragment(creditID), R.string.bio.toString())
        pagerAdapter.addFragment(CreditFilmAsCastFragment(creditID), R.string.cast.toString())
        pagerAdapter.addFragment(CreditFilmAsCrewFragment(creditID), R.string.crew.toString())
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 3
        viewPager.currentItem = viewPagerIndex
        tabLayout.setupWithViewPager(viewPager)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}