package com.mgbheights.android.ui.visitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.databinding.FragmentAddVisitorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddVisitorFragment : Fragment() {

    private var _binding: FragmentAddVisitorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VisitorViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddVisitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.cardPhoto.setOnClickListener {
            // Launch CameraX for photo capture
        }

        binding.btnSubmit.setOnClickListener {
            // Validate and submit visitor
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

