package xyz.fairportstudios.popularin.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.activities.SignInActivity
import xyz.fairportstudios.popularin.activities.SignUpActivity

class EmptyAccountFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.reusable_empty_account, container, false)

        // Context
        val context = requireActivity()

        // Binding
        val buttonSignIn = view.findViewById<Button>(R.id.button_rea_sign_in)
        val buttonSignUp = view.findViewById<Button>(R.id.button_rea_sign_up)

        // Activity
        buttonSignIn.setOnClickListener { gotoSignIn(context) }

        buttonSignUp.setOnClickListener { gotoSignUp(context) }

        return view
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