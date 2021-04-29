package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import xyz.fairportstudios.popularin.activities.SignInActivity
import xyz.fairportstudios.popularin.activities.SignUpActivity
import xyz.fairportstudios.popularin.databinding.ReusableEmptyAccountBinding

class EmptyAccountFragment : Fragment() {
    // Binding
    private var _mBinding: ReusableEmptyAccountBinding? = null
    private val mBinding get() = _mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = ReusableEmptyAccountBinding.inflate(inflater, container, false)

        // Context
        val context = requireActivity()

        // Activity
        mBinding.signInButton.setOnClickListener { gotoSignIn(context) }

        mBinding.signUpButton.setOnClickListener { gotoSignUp(context) }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
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