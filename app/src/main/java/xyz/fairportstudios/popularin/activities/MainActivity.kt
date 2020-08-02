package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.fragments.*
import xyz.fairportstudios.popularin.preferences.Auth

class MainActivity : AppCompatActivity() {
    // Variable untuk fitur double tap to exit
    private val mTimeInterval: Int = 2000
    private var mTimeBackPressed: Long = 0

    // Variable member
    private var mIsAuth: Boolean = false
    private val mAccountFragment: Fragment = AccountFragment()
    private val mAiringFragment: Fragment = AiringFragment()
    private val mEmptyAccountFragment: Fragment = EmptyAccountFragment()
    private val mGenreFragment: Fragment = GenreFragment()
    private val mReviewFragment: Fragment = ReviewFragment()
    private val mSearchFragment: Fragment = SearchFragment()
    private val mTimelineFragment: Fragment = TimelineFragment()
    private lateinit var mContext: Context
    private lateinit var mSelectedFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Context
        mContext = this

        // Auth
        mIsAuth = Auth(mContext).isAuth()

        // Bottom navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation_am_layout)
        bottomNavigation.setOnNavigationItemSelectedListener(listener)

        // Menampilkan fragment otomatis sesuai kondisi
        mSelectedFragment = when (mIsAuth) {
            true -> mTimelineFragment
            false -> mGenreFragment
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_am_container, mSelectedFragment)
            .commit()
    }

    override fun onBackPressed() {
        if (mTimeInterval + mTimeBackPressed > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(mContext, R.string.press_once_more_to_exit, Toast.LENGTH_SHORT).show()
        }
        mTimeBackPressed = System.currentTimeMillis()
    }

    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.menu_bn_home -> {
                mSelectedFragment = when (mIsAuth) {
                    true -> mTimelineFragment
                    false -> mGenreFragment
                }
            }
            R.id.menu_bn_airing -> mSelectedFragment = mAiringFragment
            R.id.menu_bn_review -> mSelectedFragment = mReviewFragment
            R.id.menu_bn_search -> mSelectedFragment = mSearchFragment
            R.id.menu_bn_account -> {
                mSelectedFragment = when (mIsAuth) {
                    true -> mAccountFragment
                    false -> mEmptyAccountFragment
                }
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_am_container, mSelectedFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        return@OnNavigationItemSelectedListener true
    }
}