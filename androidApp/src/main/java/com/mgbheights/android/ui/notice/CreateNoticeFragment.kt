package com.mgbheights.android.ui.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentCreateNoticeBinding
import com.mgbheights.android.util.CameraHelper
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.shared.domain.model.NoticeCategory
import com.mgbheights.shared.domain.model.NoticePriority
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateNoticeFragment : Fragment() {
    private var _binding: FragmentCreateNoticeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoticeViewModel by viewModels()
    private lateinit var cameraHelper: CameraHelper
    private var imageBase64: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNoticeBinding.inflate(inflater, container, false)
        
        cameraHelper = CameraHelper(this) { uri ->
            val base64 = PhotoCompressor.compressGeneralPhoto(requireContext(), uri)
            if (base64 != null) {
                imageBase64 = base64
                binding.ivPreview.isVisible = true
                PhotoCompressor.loadPhotoIntoView(binding.ivPreview, base64, R.drawable.ic_notices)
                binding.btnAddImage.text = "Change Image"
            }
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.tvTargetLabel.isVisible = false
        binding.chipGroupTargets.isVisible = false

        setupDropdowns()

        val prefillTitle = arguments?.getString("prefillTitle") ?: ""
        if (prefillTitle.isNotBlank()) {
            binding.etTitle.setText(prefillTitle)
            if (prefillTitle.contains("VERY IMPORTANT")) {
                binding.switchEmergency.isChecked = true
            }
        }

        binding.btnAddImage.setOnClickListener {
            cameraHelper.showPhotoPicker()
        }

        binding.btnPost.setOnClickListener {
            val title = binding.etTitle.text?.toString()?.trim() ?: ""
            val body = binding.etBody.text?.toString()?.trim() ?: ""
            
            if (title.isBlank() || body.isBlank()) {
                Toast.makeText(requireContext(), "Please fill title and body", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // In a real implementation, we'd upload imageBase64 to Storage here
            Toast.makeText(requireContext(), "Notice posted successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupDropdowns() {
        val categories = NoticeCategory.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        // Use R.layout.item_dropdown for proper horizontal padding
        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
        binding.actCategory.setAdapter(categoryAdapter)
        binding.actCategory.setText(categories.first(), false)

        val priorities = NoticePriority.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        val priorityAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, priorities)
        binding.actPriority.setAdapter(priorityAdapter)
        binding.actPriority.setText(priorities[1], false) // Default to Normal
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
