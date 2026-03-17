package com.mgbheights.android.ui.complaint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
            val title = binding.etTitle.text?.toString()?.trim() ?: ""
            val desc = binding.etDescription.text?.toString()?.trim() ?: ""
            
            if (title.isBlank() || desc.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Submit logic...
            Toast.makeText(requireContext(), "Complaint submitted", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
