package xyz.fairportstudios.popularin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.adapters.PagerAdapter
import xyz.fairportstudios.popularin.databinding.ReusableToolbarPagerBinding
import xyz.fairportstudios.popularin.fragments.FollowerFragment
import xyz.fairportstudios.popularin.fragments.FollowingFragment
import xyz.fairportstudios.popularin.fragments.MutualFragment
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.statics.Popularin

class SocialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ReusableToolbarPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Context
        val context = this

        // Extra
        val userID = intent.getIntExtra(Popularin.USER_ID, 0)
        val viewPagerIndex = intent.getIntExtra(Popularin.VIEW_PAGER_INDEX, 0)

        // Auth
        val auth = Auth(context)
        val isAuth = auth.isAuth()
        val isSelf = auth.isSelf(userID, auth.getAuthID())

        // Toolbar
        binding.toolbar.title = getString(R.string.social)

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
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = screenPageLimit
        binding.viewPager.currentItem = viewPagerIndex
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // Activity
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}