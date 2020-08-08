package xyz.fairportstudios.popularin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.util.Patterns
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.apis.popularin.post.SignUpRequest
import xyz.fairportstudios.popularin.preferences.Auth

class SignUpActivity : AppCompatActivity() {
    // Variable untuk fitur load
    private var mIsLoading: Boolean = false

    // Variable member
    private lateinit var mButtonSignUp: Button
    private lateinit var mAnchorLayout: LinearLayout
    private lateinit var mFullName: String
    private lateinit var mUsername: String
    private lateinit var mEmail: String
    private lateinit var mPassword: String
    private lateinit var mInputFullName: TextInputEditText
    private lateinit var mInputUsername: TextInputEditText
    private lateinit var mInputEmail: TextInputEditText
    private lateinit var mInputPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Context
        val context = this

        // Binding
        mButtonSignUp = findViewById(R.id.button_asu_sign_up)
        mAnchorLayout = findViewById(R.id.anchor_asu_layout)
        mInputFullName = findViewById(R.id.input_asu_full_name)
        mInputUsername = findViewById(R.id.input_asu_username)
        mInputEmail = findViewById(R.id.input_asu_email)
        mInputPassword = findViewById(R.id.input_asu_password)
        val textWelcome: TextView = findViewById(R.id.text_asu_welcome)

        // Pesan
        textWelcome.text = getWelcomeMessage()

        // Text watcher
        mInputFullName.addTextChangedListener(signUpWatcher)
        mInputUsername.addTextChangedListener(signUpWatcher)
        mInputEmail.addTextChangedListener(signUpWatcher)
        mInputPassword.addTextChangedListener(signUpWatcher)

        // Activity
        mButtonSignUp.setOnClickListener {
            mIsLoading = true
            setSignUpButtonState(false)
            signUp(context)
        }
    }

    override fun onBackPressed() {
        if (!mIsLoading) {
            super.onBackPressed()
        }
    }

    private val signUpWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mFullName = mInputFullName.text.toString()
            mUsername = mInputUsername.text.toString()
            mEmail = mInputEmail.text.toString()
            mPassword = mInputPassword.text.toString()
            mButtonSignUp.isEnabled = mFullName.isNotEmpty() && mUsername.isNotEmpty() && mEmail.isNotEmpty() && mPassword.isNotEmpty()
        }
    }

    private fun getWelcomeMessage(): SpannableString {
        val welcomeMessage = R.string.sign_up_welcome_message.toString()
        val spannableString = SpannableString(welcomeMessage)
        val relativeSizeSpan = RelativeSizeSpan(2f)
        spannableString.setSpan(relativeSizeSpan, 0, 5, 0)
        return spannableString
    }

    private fun usernameValidated(): Boolean {
        return when {
            mUsername.length < 5 -> {
                Snackbar.make(mAnchorLayout, R.string.validate_username_length, Snackbar.LENGTH_LONG).show()
                false
            }
            mUsername.contains(" ") -> {
                Snackbar.make(mAnchorLayout, R.string.validate_alpha_dash, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun emailValidated(): Boolean {
        return when {
            !Patterns.EMAIL_ADDRESS.matcher(mEmail).matches() -> {
                Snackbar.make(mAnchorLayout, R.string.validate_email_format, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun passwordValidated(): Boolean {
        return when {
            mPassword.length < 8 -> {
                Snackbar.make(mAnchorLayout, R.string.validate_password_length, Snackbar.LENGTH_LONG).show()
                false
            }
            mPassword == mUsername -> {
                Snackbar.make(mAnchorLayout, R.string.validate_username_match_password, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun setSignUpButtonState(state: Boolean) {
        mButtonSignUp.isEnabled = state
        when (state) {
            true -> mButtonSignUp.text = R.string.sign_up.toString()
            false -> mButtonSignUp.text = R.string.loading.toString()
        }
    }

    private fun signUp(context: Context) {
        when (usernameValidated() && emailValidated() && passwordValidated()) {
            true -> {
                val signUpRequest = SignUpRequest(context, mFullName, mUsername, mEmail, mPassword)
                signUpRequest.sendRequest(object : SignUpRequest.Callback {
                    override fun onSuccess(authID: Int, authToken: String) {
                        val auth = Auth(context)
                        auth.setAuth(authID, authToken)

                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }

                    override fun onFailed(message: String) {
                        setSignUpButtonState(true)
                        Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onError(message: String) {
                        setSignUpButtonState(true)
                        Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                })
            }
            false -> setSignUpButtonState(true)
        }

        // Memberhentikan loading
        mIsLoading = false
    }
}