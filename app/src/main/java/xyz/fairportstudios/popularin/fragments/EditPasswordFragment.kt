package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.MainActivity
import xyz.fairportstudios.popularin.apis.popularin.put.UpdatePasswordRequest
import xyz.fairportstudios.popularin.databinding.FragmentEditPasswordBinding

class EditPasswordFragment : Fragment() {
    // Member
    private lateinit var mCurrentPassword: String
    private lateinit var mNewPassword: String
    private lateinit var mConfirmPassword: String

    // View binding
    private var _mViewBinding: FragmentEditPasswordBinding? = null
    private val mViewBinding get() = _mViewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mViewBinding = FragmentEditPasswordBinding.inflate(inflater, container, false)

        // Context
        val context = requireActivity()

        // Pesan
        mViewBinding.welcomeMessage.text = getWelcomeMessage()

        // Text watcher
        mViewBinding.inputCurrentPassword.addTextChangedListener(mEditPasswordWatcher)
        mViewBinding.inputNewPassword.addTextChangedListener(mEditPasswordWatcher)
        mViewBinding.inputConfirmPassword.addTextChangedListener(mEditPasswordWatcher)

        // Activity
        mViewBinding.savePasswordButton.setOnClickListener {
            setSavePasswordButtonState(false)
            savePassword(context)
        }

        return mViewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mViewBinding = null
    }

    private val mEditPasswordWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mCurrentPassword = mViewBinding.inputCurrentPassword.text.toString()
            mNewPassword = mViewBinding.inputNewPassword.text.toString()
            mConfirmPassword = mViewBinding.inputConfirmPassword.text.toString()
            mViewBinding.savePasswordButton.isEnabled = mCurrentPassword.isNotEmpty() && mNewPassword.isNotEmpty() && mConfirmPassword.isNotEmpty()
        }
    }

    private fun getWelcomeMessage(): SpannableString {
        val welcomeMessage = getString(R.string.edit_password_welcome_message)
        val spannableString = SpannableString(welcomeMessage)
        val relativeSizeSpan = RelativeSizeSpan(2f)
        spannableString.setSpan(relativeSizeSpan, 0, 4, 0)
        return spannableString
    }

    private fun passwordValidated(): Boolean {
        return when {
            mNewPassword.length < 8 -> {
                Snackbar.make(mViewBinding.anchorLayout, R.string.validate_new_password_length, Snackbar.LENGTH_LONG).show()
                false
            }
            mConfirmPassword != mNewPassword -> {
                Snackbar.make(mViewBinding.anchorLayout, R.string.validate_confirm_password_un_match_new_password, Snackbar.LENGTH_LONG).show()
                false
            }
            mNewPassword == mCurrentPassword -> {
                Snackbar.make(mViewBinding.anchorLayout, R.string.validate_new_password_match_current_password, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun setSavePasswordButtonState(state: Boolean) {
        mViewBinding.savePasswordButton.isEnabled = state
        when (state) {
            true -> mViewBinding.savePasswordButton.text = getString(R.string.save_password)
            false -> mViewBinding.savePasswordButton.text = getString(R.string.loading)
        }
    }

    private fun savePassword(context: Context) {
        when (passwordValidated()) {
            true -> {
                val updatePasswordRequest = UpdatePasswordRequest(context, mCurrentPassword, mNewPassword, mConfirmPassword)
                updatePasswordRequest.sendRequest(object : UpdatePasswordRequest.Callback {
                    override fun onSuccess() {
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finishAffinity()
                    }

                    override fun onInvalidCurrentPassword() {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mViewBinding.anchorLayout, R.string.invalid_current_password, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onFailed(message: String) {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onError(message: String) {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mViewBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                })
            }
            false -> setSavePasswordButtonState(true)
        }
    }
}