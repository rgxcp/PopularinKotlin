package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.MainActivity
import xyz.fairportstudios.popularin.apis.popularin.get.SelfDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.put.UpdateProfileRequest
import xyz.fairportstudios.popularin.models.SelfDetail

class EditProfileFragment : Fragment() {
    // Member
    private lateinit var mFullName: String
    private lateinit var mUsername: String
    private lateinit var mEmail: String

    // View
    private lateinit var mButtonSaveProfile: Button
    private lateinit var mAnchorLayout: LinearLayout
    private lateinit var mInputFullName: TextInputEditText
    private lateinit var mInputUsername: TextInputEditText
    private lateinit var mInputEmail: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Context
        val context = requireActivity()

        // Binding
        mButtonSaveProfile = view.findViewById(R.id.button_fep_save_profile)
        mAnchorLayout = view.findViewById(R.id.anchor_fep_layout)
        mInputFullName = view.findViewById(R.id.input_fep_full_name)
        mInputUsername = view.findViewById(R.id.input_fep_username)
        mInputEmail = view.findViewById(R.id.input_fep_email)
        val buttonEditPassword = view.findViewById<Button>(R.id.button_fep_edit_password)
        val textWelcomeMessage = view.findViewById<TextView>(R.id.text_fep_welcome)

        // Pesan
        textWelcomeMessage.text = getWelcomeMessage()

        // Menampilkan data diri awal
        getSelfDetail(context)

        // Text watcher
        mInputFullName.addTextChangedListener(mEditProfileWatcher)
        mInputUsername.addTextChangedListener(mEditProfileWatcher)
        mInputEmail.addTextChangedListener(mEditProfileWatcher)

        // Activity
        mButtonSaveProfile.setOnClickListener {
            setSaveProfileButtonState(false)
            saveProfile(context)
        }

        buttonEditPassword.setOnClickListener { gotoEditPassword() }

        return view
    }

    private val mEditProfileWatcher = object : TextWatcher {
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
            mButtonSaveProfile.isEnabled = mFullName.isNotEmpty() && mUsername.isNotEmpty() && mEmail.isNotEmpty()
        }
    }

    private fun getWelcomeMessage(): SpannableString {
        val welcomeMessage = getString(R.string.edit_profile_welcome_message)
        val spannableString = SpannableString(welcomeMessage)
        val relativeSizeSpan = RelativeSizeSpan(2f)
        spannableString.setSpan(relativeSizeSpan, 0, 4, 0)
        return spannableString
    }

    private fun getSelfDetail(context: Context) {
        val selfDetailRequest = SelfDetailRequest(context)
        selfDetailRequest.sendRequest(object : SelfDetailRequest.Callback {
            override fun onSuccess(selfDetail: SelfDetail) {
                mInputFullName.setText(selfDetail.fullName)
                mInputUsername.setText(selfDetail.username)
                mInputEmail.setText(selfDetail.email)
            }

            override fun onError(message: String) {
                Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })
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
        return when (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            true -> {
                Snackbar.make(mAnchorLayout, R.string.validate_email_format, Snackbar.LENGTH_LONG).show()
                false
            }
            false -> true
        }
    }

    private fun setSaveProfileButtonState(state: Boolean) {
        mButtonSaveProfile.isEnabled = state
        mButtonSaveProfile.text = when (state) {
            true -> getString(R.string.save_profile)
            false -> getString(R.string.loading)
        }
    }

    private fun saveProfile(context: Context) {
        when (usernameValidated() && emailValidated()) {
            true -> {
                val updateProfileRequest = UpdateProfileRequest(context, mFullName, mUsername, mEmail)
                updateProfileRequest.sendRequest(object : UpdateProfileRequest.Callback {
                    override fun onSuccess() {
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finishAffinity()
                    }

                    override fun onFailed(message: String) {
                        setSaveProfileButtonState(true)
                        Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onError(message: String) {
                        setSaveProfileButtonState(true)
                        Snackbar.make(mAnchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }
                })
            }
            false -> setSaveProfileButtonState(true)
        }
    }

    private fun gotoEditPassword() {
        requireFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_aep_container, EditPasswordFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addToBackStack(null)
            .commit()
    }
}