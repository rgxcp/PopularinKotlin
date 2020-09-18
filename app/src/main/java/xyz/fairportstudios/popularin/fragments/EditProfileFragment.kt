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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.MainActivity
import xyz.fairportstudios.popularin.apis.popularin.get.SelfDetailRequest
import xyz.fairportstudios.popularin.apis.popularin.put.UpdateProfileRequest
import xyz.fairportstudios.popularin.databinding.FragmentEditProfileBinding
import xyz.fairportstudios.popularin.interfaces.SelfDetailRequestCallback
import xyz.fairportstudios.popularin.interfaces.UpdateProfileRequestCallback
import xyz.fairportstudios.popularin.models.SelfDetail

class EditProfileFragment : Fragment() {
    // Member
    private lateinit var mFullName: String
    private lateinit var mUsername: String
    private lateinit var mEmail: String

    // Binding
    private var _mBinding: FragmentEditProfileBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = FragmentEditProfileBinding.inflate(inflater, container, false)

        // Context
        val context = requireActivity()

        // Pesan
        mBinding.welcomeMessage.text = getWelcomeMessage()

        // Menampilkan data diri awal
        getSelfDetail(context)

        // Text watcher
        mBinding.inputFullName.addTextChangedListener(mEditProfileWatcher)
        mBinding.inputUsername.addTextChangedListener(mEditProfileWatcher)
        mBinding.inputEmail.addTextChangedListener(mEditProfileWatcher)

        // Activity
        mBinding.saveProfileButton.setOnClickListener {
            setSaveProfileButtonState(false)
            saveProfile(context)
        }

        mBinding.editPasswordButton.setOnClickListener { gotoEditPassword() }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    private val mEditProfileWatcher = object : TextWatcher {
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
            mBinding.saveProfileButton.isEnabled = mFullName.isNotEmpty() && mUsername.isNotEmpty() && mEmail.isNotEmpty()
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
        selfDetailRequest.sendRequest(object : SelfDetailRequestCallback {
            override fun onSuccess(selfDetail: SelfDetail) {
                mBinding.selfDetail = selfDetail
            }

            override fun onError(message: String) {
                Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        })
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

    private fun setSaveProfileButtonState(state: Boolean) {
        mBinding.saveProfileButton.isEnabled = state
        mBinding.saveProfileButton.text = when (state) {
            true -> getString(R.string.save_profile)
            false -> getString(R.string.loading)
        }
    }

    private fun saveProfile(context: Context) {
        when (usernameValidated() && emailValidated()) {
            true -> {
                val updateProfileRequest = UpdateProfileRequest(context, mFullName, mUsername, mEmail)
                updateProfileRequest.sendRequest(object : UpdateProfileRequestCallback {
                    override fun onSuccess() {
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finishAffinity()
                    }

                    override fun onFailed(message: String) {
                        setSaveProfileButtonState(true)
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onError(message: String) {
                        setSaveProfileButtonState(true)
                        Snackbar.make(mBinding.anchorLayout, message, Snackbar.LENGTH_LONG).show()
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