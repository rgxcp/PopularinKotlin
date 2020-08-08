package xyz.fairportstudios.popularin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.fragments.EditProfileFragment

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_aep_container, EditProfileFragment())
            .commit()
    }
}