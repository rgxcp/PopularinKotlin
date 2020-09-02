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
    // View binding
    private var _mViewBinding: ReusableEmptyAccountBinding? = null
    private val mViewBinding get() = _mViewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mViewBinding = ReusableEmptyAccountBinding.inflate(inflater, container, false)

        // Context
        val context = requireActivity()

        // Activity
        mViewBinding.signInButton.setOnClickListener { gotoSignIn(context) }

        mViewBinding.signUpButton.setOnClickListener { gotoSignUp(context) }

        return mViewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mViewBinding = null
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