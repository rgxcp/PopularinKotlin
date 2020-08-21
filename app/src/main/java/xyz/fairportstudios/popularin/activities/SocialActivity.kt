package xyz.fairportstudios.popularin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.fragments.FollowerFragment
import xyz.fairportstudios.popularin.fragments.FollowingFragment
import xyz.fairportstudios.popularin.fragments.MutualFragment
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class SocialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_toolbar_pager)

        // Context
        val context = this

        // Binding
        val tabLayout = findViewById<TabLayout>(R.id.tab_rtp_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_rtp_layout)
        val viewPager = findViewById<ViewPager>(R.id.pager_rtp_layout)

        // Extra
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)

        // Auth
        val auth = Auth(context)
        val isAuth = auth.isAuth()
        val isSelf = auth.isSelf(userID, auth.getAuthID())

        // Toolbar
        toolbar.title = getString(R.string.social)

        // Limit page yang akan ditampilkan
        val screenPageLimit = when (isAuth && !isSelf) {
            true -> 3
            false -> 2
        }

        // Tab pager
        val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        pagerAdapter.addFragment(FollowerFragment(userID, isSelf), getString(R.string.follower))
        pagerAdapter.addFragment(FollowingFragment(userID, isSelf), getString(R.string.following))
        if (isAuth && !isSelf) pagerAdapter.addFragment(MutualFragment(userID), getString(R.string.mutual))
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = screenPageLimit
        viewPager.currentItem = viewPagerIndex
        tabLayout.setupWithViewPager(viewPager)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}