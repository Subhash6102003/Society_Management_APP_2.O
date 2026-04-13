package com.mgbheights.android.ui.auth
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
class SelectRoleFragment : Fragment(R.layout.fragment_select_role) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val email = arguments?.getString("email").orEmpty()
        val roleMap = mapOf(
            R.id.btnResident to "resident",
            R.id.btnTenant to "tenant",
            R.id.btnGuard to "guard",
            R.id.btnWorker to "worker",
            R.id.btnMaid to "maid"
        )
        roleMap.forEach { (id, role) ->
            view.findViewById<View>(id).setOnClickListener {
                findNavController().navigate(R.id.createProfileFragment, bundleOf("email" to email, "role" to role))
            }
        }
    }
}
