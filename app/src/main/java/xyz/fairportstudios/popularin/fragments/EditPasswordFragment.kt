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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.MainActivity
import xyz.fairportstudios.popularin.apis.popularin.put.UpdatePasswordRequest

class EditPasswordFragment : Fragment() {
    // Variable member
    private lateinit var mButtonSavePassword: Button
    private lateinit var mAnchorLayout: LinearLayout
    private lateinit var mCurrentPassword: String
    private lateinit var mNewPassword: String
    private lateinit var mConfirmPassword: String
    private lateinit var mInputCurrentPassword: TextInputEditText
    private lateinit var mInputNewPassword: TextInputEditText
    private lateinit var mInputConfirmPassword: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_edit_password, container, false)

        // Context
        val context = requireActivity()

        // Binding
        mButtonSavePassword = view.findViewById(R.id.button_fepw_save_password)
        mAnchorLayout = view.findViewById(R.id.anchor_fepw_layout)
        mInputCurrentPassword = view.findViewById(R.id.input_fepw_current_password)
        mInputNewPassword = view.findViewById(R.id.input_fepw_new_password)
        mInputConfirmPassword = view.findViewById(R.id.input_fepw_confirm_password)
        val textWelcome: TextView = view.findViewById(R.id.text_fepw_welcome)

        // Pesan
        textWelcome.text = getWelcomeMessage()

        // Text watcher
        mInputCurrentPassword.addTextChangedListener(editPasswordWatcher)
        mInputNewPassword.addTextChangedListener(editPasswordWatcher)
        mInputConfirmPassword.addTextChangedListener(editPasswordWatcher)

        // Activity
        mButtonSavePassword.setOnClickListener {
            setSavePasswordButtonState(false)
            savePassword(context)
        }

        return view
    }

    private val editPasswordWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Tidak digunakan
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Tidak digunakan
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mCurrentPassword = mInputCurrentPassword.text.toString()
            mNewPassword = mInputNewPassword.text.toString()
            mConfirmPassword = mInputConfirmPassword.text.toString()
            mButtonSavePassword.isEnabled = mCurrentPassword.isNotEmpty() && mNewPassword.isNotEmpty() && mConfirmPassword.isNotEmpty()
        }
    }

    private fun getWelcomeMessage(): SpannableString {
        val welcomeMessage = R.string.edit_password_welcome_message.toString()
        val spannableString = SpannableString(welcomeMessage)
        val relativeSizeSpan = RelativeSizeSpan(2f)
        spannableString.setSpan(relativeSizeSpan, 0, 4, 0)
        return spannableString
    }

    private fun passwordValidated(): Boolean {
        return when {
            mNewPassword.length < 8 -> {
                Snackbar.make(mAnchorLayout, R.string.validate_new_password_length, Snackbar.LENGTH_LONG).show()
                false
            }
            mConfirmPassword != mNewPassword -> {
                Snackbar.make(mAnchorLayout, R.string.validate_confirm_password_un_match_new_password, Snackbar.LENGTH_LONG).show()
                false
            }
            mNewPassword == mCurrentPassword -> {
                Snackbar.make(mAnchorLayout, R.string.validate_new_password_match_current_password, Snackbar.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun setSavePasswordButtonState(state: Boolean) {
        mButtonSavePassword.isEnabled = state
        when (state) {
            true -> mButtonSavePassword.text = R.string.save_password.toString()
            false -> mButtonSavePassword.text = R.string.loading.toString()
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
                        Snackbar.make(mAnchorLayout, R.string.invalid_current_password, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onFailed(message: String) {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onError(message: String) {
                        setSavePasswordButtonState(true)
                        Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                })
            }
            false -> setSavePasswordButtonState(true)
        }
    }
}