package xyz.fairportstudios.popularin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.databinding.ReusableToolbarPagerBinding
import xyz.fairportstudios.popularin.fragments.CreditBioFragment
import xyz.fairportstudios.popularin.fragments.CreditFilmAsCastFragment
import xyz.fairportstudios.popularin.fragments.CreditFilmAsCrewFragment
import xyz.fairportstudios.popularin.statics.Popularin

class CreditDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ReusableToolbarPagerBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Extra
        val creditID = intent.getIntExtra(Popularin.CREDIT_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)

        // Toolbar
        viewBinding.toolbar.title = getString(R.string.credit)

        // Tab pager
        val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        pagerAdapter.addFragment(CreditBioFragment(creditID), getString(R.string.bio))
        pagerAdapter.addFragment(CreditFilmAsCastFragment(creditID), getString(R.string.cast))
        pagerAdapter.addFragment(CreditFilmAsCrewFragment(creditID), getString(R.string.crew))
        viewBinding.viewPager.adapter = pagerAdapter
        viewBinding.viewPager.offscreenPageLimit = 3
        viewBinding.viewPager.currentItem = viewPagerIndex
        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)

        // Activity
        viewBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}