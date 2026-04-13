package com.mgbheights.android.ui.complaint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentCreateComplaintBinding
import com.mgbheights.android.util.CameraHelper
import com.mgbheights.android.util.PhotoCompressor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateComplaintFragment : Fragment() {
    private var _binding: FragmentCreateComplaintBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ComplaintViewModel by viewModels()
    private lateinit var cameraHelper: CameraHelper
    private var photoBase64: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateComplaintBinding.inflate(inflater, container, false)
        
        cameraHelper = CameraHelper(this) { uri ->
            val base64 = PhotoCompressor.compressIdProof(requireContext(), uri)
            if (base64 != null) {
                photoBase64 = base64
                binding.btnAddPhotos.text = "Photo Added ✅"
                Toast.makeText(requireContext(), "Photo attached", Toast.LENGTH_SHORT).show()
            }
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        // UI Clean up: Hide category and priority dropdowns as requested
        binding.tilCategory.visibility = View.GONE
        binding.tilPriority.visibility = View.GONE

        binding.btnAddPhotos.setOnClickListener {
            cameraHelper.showPhotoPicker()
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val category = "General" // Simplified

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Title and description are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "You must be logged in to submit a complaint.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get user details to add to complaint
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get().addOnSuccessListener { userDoc ->
                val complaintData = hashMapOf(
                    "title"           to title,
                    "description"     to description,
                    "category"        to category,
                    "submittedByUid"  to user.uid,
                    "submittedByName" to userDoc.getString("name"),
                    "submitterUserType" to userDoc.getString("userType"),
                    "flatNumber"      to userDoc.getString("flatNumber"),
                    "buildingNumber"  to userDoc.getString("buildingNumber"),
                    "status"          to "pending",
                    "createdAt"       to FieldValue.serverTimestamp()
                )
                FirebaseFirestore.getInstance()
                    .collection("complaints")
                    .add(complaintData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Complaint submitted!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to submit complaint.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
