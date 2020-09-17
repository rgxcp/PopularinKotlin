package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xyz.fairportstudios.popularin.databinding.ReusableEmptyAccountBinding

class EmptyAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ReusableEmptyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Context
        val context = this

        // Activity
        binding.signInButton.setOnClickListener { gotoSignIn(context) }

        binding.signUpButton.setOnClickListener { gotoSignUp(context) }
    }

    private fun gotoSignIn(context: Context) {
        val intent = Intent(context, SignInActivity::class.java)
        startActivity(intent)
    }

    private fun gotoSignUp(context: Context) {
        val intent = Intent(context, SignUpActivity::class.java)
        startActivity(intent)
    }
}