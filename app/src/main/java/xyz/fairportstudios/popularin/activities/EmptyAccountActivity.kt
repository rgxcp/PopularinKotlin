package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import xyz.fairportstudios.popularin.R

class EmptyAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reusable_empty_account)

        // Context
        val context = this

        // Binding
        val buttonSignIn: Button = findViewById(R.id.button_rea_sign_in)
        val buttonSignUp: Button = findViewById(R.id.button_rea_sign_up)

        // Activity
        buttonSignIn.setOnClickListener { gotoSignIn(context) }

        buttonSignUp.setOnClickListener { gotoSignUp(context) }
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