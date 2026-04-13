package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Shown when a user's account has been rejected by admin.
 * Provides a sign-out button so the user can try with another account.
 */
@AndroidEntryPoint
class RejectedFragment : Fragment(R.layout.fragment_rejected) {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBackToLogin)?.setOnClickListener {
            authViewModel.signOut()
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph_main, true)
                .build()
            findNavController().navigate(R.id.landingFragment, null, options)
        }
    }
}

