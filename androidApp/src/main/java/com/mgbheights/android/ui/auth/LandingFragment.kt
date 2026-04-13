package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LandingFragment : Fragment(R.layout.fragment_landing) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnLogin).setOnClickListener {
            findNavController().navigate(R.id.action_landing_to_enter_email)
        }
        view.findViewById<View>(R.id.btnSignUp).setOnClickListener {
            findNavController().navigate(R.id.action_landing_to_select_role)
        }
    }
}
