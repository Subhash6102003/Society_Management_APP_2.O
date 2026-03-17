package com.mgbheights.android.ui.visitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.databinding.FragmentAddVisitorBinding
import com.mgbheights.android.util.CameraHelper
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import android.net.Uri

@AndroidEntryPoint
class AddVisitorFragment : Fragment() {

    private var _binding: FragmentAddVisitorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VisitorViewModel by viewModels()
    private lateinit var cameraHelper: CameraHelper
    private var visitorPhotoBase64: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddVisitorBinding.inflate(inflater, container, false)
        cameraHelper = CameraHelper(this) { uri ->
            handlePhotoResult(uri)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.cardPhoto.setOnClickListener {
            cameraHelper.showPhotoPicker()
        }

        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }

        observeViewModel()
    }

    private fun handlePhotoResult(uri: Uri) {
        val base64 = PhotoCompressor.compressGeneralPhoto(requireContext(), uri)
        if (base64 != null) {
            visitorPhotoBase64 = base64
            PhotoCompressor.loadPhotoIntoView(binding.ivVisitorPhoto, base64, com.mgbheights.android.R.drawable.ic_profile)
            binding.layoutCapturePhoto.visibility = View.GONE
        } else {
            Toast.makeText(requireContext(), "Failed to process photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSubmit() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val purpose = binding.etPurpose.text.toString().trim()
        val flat = binding.etFlat.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || purpose.isEmpty() || flat.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val visitor = Visitor(
            name = name,
            phoneNumber = phone,
            purpose = purpose,
            flatNumber = flat,
            vehicleNumber = binding.etVehicle.text.toString().trim(),
            photoUrl = visitorPhotoBase64
        )

        viewModel.addVisitor(visitor)
    }

    private fun observeViewModel() {
        viewModel.addVisitorState.observe(viewLifecycleOwner) { state ->
            binding.progressLoading.visibility = if (state is Resource.Loading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = state !is Resource.Loading

            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Visitor registered successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
