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
        val tabLayout = findViewById<TabLayout>(R.id.tab_rtp_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_rtp_layout)
        val viewPager = findViewById<ViewPager>(R.id.pager_rtp_layout)

        // Extra
        val creditID = intent.getIntExtra(Popularin.CREDIT_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)

        // Toolbar
        toolbar.title = getString(R.string.credit)

        // Tab pager
        val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        pagerAdapter.addFragment(CreditBioFragment(creditID), getString(R.string.bio))
        pagerAdapter.addFragment(CreditFilmAsCastFragment(creditID), getString(R.string.cast))
        pagerAdapter.addFragment(CreditFilmAsCrewFragment(creditID), getString(R.string.crew))
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 3
        viewPager.currentItem = viewPagerIndex
        tabLayout.setupWithViewPager(viewPager)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}