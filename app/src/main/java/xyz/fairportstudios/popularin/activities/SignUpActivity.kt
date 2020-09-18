package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.post.SignUpRequest
import xyz.fairportstudios.popularin.databinding.ActivitySignUpBinding
import xyz.fairportstudios.popularin.interfaces.SignUpRequestCallback
import xyz.fairportstudios.popularin.preferences.Auth

class SignUpActivity : AppCompatActivity() {
    // Primitive
    private var mIsLoading = false

    // Member
    private lateinit var mFullName: String
    private lateinit var mUsername: String
    private lateinit var mEmail: String
    private lateinit var mPassword: String

    // Binding
    private lateinit var mBinding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Context
        val context = this

        // Pesan
        mBinding.welcomeMessage.text = getWelcomeMessage()

        // Text watcher
        mBinding.inputFullName.addTextChangedListener(mSignUpWatcher)
        mBinding.inputUsername.addTextChangedListener(mSignUpWatcher)
        mBinding.inputEmail.addTextChangedListener(mSignUpWatcher)
        mBinding.inputPassword.addTextChangedListener(mSignUpWatcher)

        // Activity
        mBinding.signUpButton.setOnClickListener {
            mIsLoading = true
            setSignUpButtonState(false)
            signUp(context)
        }
    }

    override fun onBackPressed() {
        if (!mIsLoading) super.onBackPressed()
    }

    private val mSignUpWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mFullName = mBinding.inputFullName.text.toString()
            mUsername = mBinding.inputUsername.text.toString()
            mEmail = mBinding.inputEmail.text.toString()
            mPassword = mBinding.inputPassword.text.toString()
            mBinding.signUpButton.isEnabled = mFullName.isNotEmpty() && mUsername.isNotEmpty() && mEmail.isNotEmpty() && mPassword.isNotEmpty()
        }
    }

    private fun getWelcomeMessage(): SpannableString {
        val welcomeMessage = getString(R.string.sign_up_welcome_message)
        val spannableString = SpannableString(welcomeMessage)
        val relativeSizeSpan = RelativeSizeSpan(2f)
        spannableString.setSpan(relativeSizeSpan, 0, 5, 0)
        return spannableString
    }

    private fun usernameValidated(): Boolean {
        return when {
            mUsername.length < 5 -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_username_length, Snackbar.LENGTH_LONG).show()
                false
            }
            mUsername.contains(" ") -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_alpha_dash, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun emailValidated(): Boolean {
        return when (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            true -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_email_format, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun passwordValidated(): Boolean {
        return when {
            mPassword.length < 8 -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_password_length, Snackbar.LENGTH_LONG).show()
                false
            }
            mPassword == mUsername -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_username_match_password, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun setSignUpButtonState(state: Boolean) {
        mBinding.signUpButton.isEnabled = state
        mBinding.signUpButton.text = when (state) {
            true -> getString(R.string.sign_up)
            false -> getString(R.string.loading)
        }
    }

    private fun signUp(context: Context) {
        when (usernameValidated() && emailValidated() && passwordValidated()) {
            true -> {
                val signUpRequest = SignUpRequest(context, mFullName, mUsername, mEmail, mPassword)
                signUpRequest.sendRequest(object : SignUpRequestCallback {
                    override fun onSuccess(authID: Int, authToken: String) {
                        val auth = Auth(context)
                        auth.setAuth(authID, authToken)

                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }

                    override fun onFailed(message: String) {
                        setSignUpButtonState(true)
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onError(message: String) {
                        setSignUpButtonState(true)
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                })
            }
            false -> setSignUpButtonState(true)
        }

        // Memberhentikan loading
        mIsLoading = false
    }
}