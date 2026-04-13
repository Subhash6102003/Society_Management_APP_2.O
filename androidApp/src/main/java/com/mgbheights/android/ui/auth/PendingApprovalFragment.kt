package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PendingApprovalFragment : Fragment(R.layout.fragment_pending_approval) {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Support both button IDs for backward compat
        val logoutBtn = view.findViewById<View>(R.id.btnBackToLogin)
            ?: view.findViewById<View>(R.id.btnLogout)
        logoutBtn?.setOnClickListener {
            authViewModel.signOut()
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph_main, true)
                .build()
            findNavController().navigate(R.id.landingFragment, null, options)
        }
    }
}
