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
import xyz.fairportstudios.popularin.interfaces.UpdatePasswordRequestCallback

class EditPasswordFragment : Fragment() {
    // Member
    private lateinit var mCurrentPassword: String
    private lateinit var mNewPassword: String
    private lateinit var mConfirmPassword: String

    // Binding
    private var _mBinding: FragmentEditPasswordBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = FragmentEditPasswordBinding.inflate(inflater, container, false)

        // Context
        val context = requireActivity()

        // Pesan
        mBinding.welcomeMessage.text = getWelcomeMessage()

        // Text watcher
        mBinding.inputCurrentPassword.addTextChangedListener(mEditPasswordWatcher)
        mBinding.inputNewPassword.addTextChangedListener(mEditPasswordWatcher)
        mBinding.inputConfirmPassword.addTextChangedListener(mEditPasswordWatcher)

        // Activity
        mBinding.savePasswordButton.setOnClickListener {
            setSavePasswordButtonState(false)
            savePassword(context)
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    private val mEditPasswordWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mCurrentPassword = mBinding.inputCurrentPassword.text.toString()
            mNewPassword = mBinding.inputNewPassword.text.toString()
            mConfirmPassword = mBinding.inputConfirmPassword.text.toString()
            mBinding.savePasswordButton.isEnabled = mCurrentPassword.isNotEmpty() && mNewPassword.isNotEmpty() && mConfirmPassword.isNotEmpty()
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
                Snackbar.make(mBinding.anchorLayout, R.string.validate_new_password_length, Snackbar.LENGTH_LONG).show()
                false
            }
            mConfirmPassword != mNewPassword -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_confirm_password_un_match_new_password, Snackbar.LENGTH_LONG).show()
                false
            }
            mNewPassword == mCurrentPassword -> {
                Snackbar.make(mBinding.anchorLayout, R.string.validate_new_password_match_current_password, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun setSavePasswordButtonState(state: Boolean) {
        mBinding.savePasswordButton.isEnabled = state
        when (state) {
            true -> mBinding.savePasswordButton.text = getString(R.string.save_password)
            false -> mBinding.savePasswordButton.text = getString(R.string.loading)
        }
    }

    private fun savePassword(context: Context) {
        when (passwordValidated()) {
            true -> {
                val updatePasswordRequest = UpdatePasswordRequest(context, mCurrentPassword, mNewPassword, mConfirmPassword)
                updatePasswordRequest.sendRequest(object : UpdatePasswordRequestCallback {
                    override fun onSuccess() {
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finishAffinity()
                    }

                    override fun onInvalidCurrentPassword() {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mBinding.anchorLayout, R.string.invalid_current_password, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onFailed(message: String) {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onError(message: String) {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                })
            }
            false -> setSavePasswordButtonState(true)
        }
    }
}