package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.post.SignInRequest
import xyz.fairportstudios.popularin.databinding.ActivitySignInBinding
import xyz.fairportstudios.popularin.preferences.Auth

class SignInActivity : AppCompatActivity() {
    // Primitive
    private var mIsLoading = false

    // Member
    private lateinit var mUsername: String
    private lateinit var mPassword: String

    // View binding
    private lateinit var mViewBinding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        // Context
        val context = this

        // Pesan
        mViewBinding.welcomeMessage.text = getWelcomeMessage()

        // Text watcher
        mViewBinding.inputUsername.addTextChangedListener(mSignInWatcher)
        mViewBinding.inputPassword.addTextChangedListener(mSignInWatcher)

        // Activity
        mViewBinding.signInButton.setOnClickListener {
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
            mUsername = mViewBinding.inputUsername.text.toString()
            mPassword = mViewBinding.inputPassword.text.toString()
            mViewBinding.signInButton.isEnabled = mUsername.isNotEmpty() && mPassword.isNotEmpty()
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
        mViewBinding.signInButton.isEnabled = state
        mViewBinding.signInButton.text = when (state) {
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
                Snackbar.make(mViewBinding.anchorLayout, R.string.invalid_username, Snackbar.LENGTH_LONG).show()
            }

            override fun onInvalidPassword() {
                setSignInButtonState(true)
                Snackbar.make(mViewBinding.anchorLayout, R.string.invalid_password, Snackbar.LENGTH_LONG).show()
            }

            override fun onFailed(message: String) {
                setSignInButtonState(true)
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }

            override fun onError(message: String) {
                setSignInButtonState(true)
                Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })

        // Memberhentikan loading
        mIsLoading = false
    }
}