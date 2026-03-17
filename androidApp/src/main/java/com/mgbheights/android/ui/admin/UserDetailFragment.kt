package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentUserDetailBinding
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.DateTimeUtil
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!
    private val args: UserDetailFragmentArgs by navArgs()
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        viewModel.allUsersState.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                val user = state.data.find { it.id == args.userId }
                if (user != null) {
                    displayUser(user)
                } else {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }

        // Fetch user data if not already loaded in the list
        viewModel.loadAllUsers()

        binding.btnDelete.setOnClickListener {
            viewModel.rejectUser(args.userId) // rejectUser deletes the user
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                Toast.makeText(requireContext(), "Action completed", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else if (state is Resource.Error) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUser(user: User) {
        binding.tvName.text = user.name.ifBlank { "Unnamed User" }
        binding.chipRole.text = user.role.name.replace("_", " ")
        
        PhotoCompressor.loadPhotoIntoView(binding.ivProfilePhoto, user.profilePhotoUrl, R.drawable.ic_profile)
        PhotoCompressor.loadPhotoIntoView(binding.ivIdProof, user.idProofUrl, R.drawable.ic_notices)

        binding.rowEmail.tvLabel.text = "Email"
        binding.rowEmail.tvValue.text = user.email.ifBlank { "N/A" }

        binding.rowPhone.tvLabel.text = "Phone"
        binding.rowPhone.tvValue.text = user.phoneNumber.ifBlank { "N/A" }

        binding.rowFlat.tvLabel.text = "Flat Number"
        binding.rowFlat.tvValue.text = user.flatNumber.ifBlank { "N/A" }

        binding.rowTower.tvLabel.text = "Tower/Block"
        binding.rowTower.tvValue.text = user.towerBlock.ifBlank { "N/A" }

        binding.rowJoined.tvLabel.text = "Joined On"
        binding.rowJoined.tvValue.text = DateTimeUtil.formatDate(user.createdAt)

        binding.rowStatus.tvLabel.text = "Status"
        binding.rowStatus.tvValue.text = if (user.isApproved) "Approved" else "Pending Approval"
        binding.rowStatus.tvValue.setTextColor(
            resources.getColor(if (user.isApproved) R.color.status_success else R.color.status_pending, null)
        )

        binding.btnApprove.isVisible = !user.isApproved
        binding.btnApprove.setOnClickListener {
            viewModel.approveUser(user.id)
        }
        
        // Additional info based on role
        if (user.role == UserRole.WORKER) {
            // Could show skills etc if added to model
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
