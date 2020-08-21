package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.post.SignInRequest
import xyz.fairportstudios.popularin.preferences.Auth

class SignInActivity : AppCompatActivity() {
    // Primitive
    private var mIsLoading = false

    // Member
    private lateinit var mUsername: String
    private lateinit var mPassword: String

    // View
    private lateinit var mButtonSignIn: Button
    private lateinit var mAnchorLayout: LinearLayout
    private lateinit var mInputUsername: TextInputEditText
    private lateinit var mInputPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Context
        val context = this

        // Binding
        mButtonSignIn = findViewById(R.id.button_asi_sign_in)
        mAnchorLayout = findViewById(R.id.anchor_asi_layout)
        mInputUsername = findViewById(R.id.input_asi_username)
        mInputPassword = findViewById(R.id.input_asi_password)
        val textWelcomeMessage = findViewById<TextView>(R.id.text_asi_welcome)

        // Pesan
        textWelcomeMessage.text = getWelcomeMessage()

        // Text watcher
        mInputUsername.addTextChangedListener(mSignInWatcher)
        mInputPassword.addTextChangedListener(mSignInWatcher)

        // Activity
        mButtonSignIn.setOnClickListener {
            mIsLoading = true
            setSignInButtonState(false)
            signIn(context)
        }
    }

    override fun onBackPressed() {
        if (!mIsLoading) super.onBackPressed()
    }

    private val mSignInWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mUsername = mInputUsername.text.toString()
            mPassword = mInputPassword.text.toString()
            mButtonSignIn.isEnabled = mUsername.isNotEmpty() && mPassword.isNotEmpty()
        }
    }

    private fun getWelcomeMessage(): SpannableString {
        val welcomeMessage = getString(R.string.sign_in_welcome_message)
        val spannableString = SpannableString(welcomeMessage)
        val relativeSizeSpan = RelativeSizeSpan(2f)
        spannableString.setSpan(relativeSizeSpan, 0, 5, 0)
        return spannableString
    }

    private fun setSignInButtonState(state: Boolean) {
        mButtonSignIn.isEnabled = state
        mButtonSignIn.text = when (state) {
            true -> getString(R.string.sign_in)
            false -> getString(R.string.loading)
        }
    }

    private fun signIn(context: Context) {
        val signInRequest = SignInRequest(context, mUsername, mPassword)
        signInRequest.sendRequest(object : SignInRequest.Callback {
            override fun onSuccess(authID: Int, authToken: String) {
                val auth = Auth(context)
                auth.setAuth(authID, authToken)

                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

            override fun onInvalidUsername() {
                setSignInButtonState(true)
                Snackbar.make(mAnchorLayout, R.string.invalid_username, Snackbar.LENGTH_LONG).show()
            }

            override fun onInvalidPassword() {
                setSignInButtonState(true)
                Snackbar.make(mAnchorLayout, R.string.invalid_password, Snackbar.LENGTH_LONG).show()
            }

            override fun onFailed(message: String) {
                setSignInButtonState(true)
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }

            override fun onError(message: String) {
                setSignInButtonState(true)
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}