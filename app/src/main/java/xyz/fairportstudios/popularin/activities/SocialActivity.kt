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
        val tabLayout: TabLayout = findViewById(R.id.tab_rtp_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar_rtp_layout)
        val viewPager: ViewPager = findViewById(R.id.pager_rtp_layout)

        // Extra
        val intent = intent
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)

        // Auth
        val auth = Auth(context)
        val isAuth = auth.isAuth()
        val isSelf = userID == auth.getAuthID()

        // Toolbar
        toolbar.title = R.string.social.toString()

        // Limit page yang akan ditampilkan
        val screenPageLimit = when (isAuth && !isSelf) {
            true -> 3
            false -> 2
        }

        // Tab pager
        val pagerAdapter = PagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        pagerAdapter.addFragment(FollowerFragment(userID), R.string.follower.toString())
        pagerAdapter.addFragment(FollowingFragment(userID), R.string.following.toString())
        if (isAuth && !isSelf) {
            pagerAdapter.addFragment(MutualFragment(userID), R.string.mutual.toString())
        }
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = screenPageLimit
        viewPager.currentItem = viewPagerIndex
        tabLayout.setupWithViewPager(viewPager)

        // Activity
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}