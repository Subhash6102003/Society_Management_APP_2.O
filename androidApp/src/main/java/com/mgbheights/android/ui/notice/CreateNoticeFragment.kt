package com.mgbheights.android.ui.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.databinding.FragmentCreateNoticeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateNoticeFragment : Fragment() {
    private var _binding: FragmentCreateNoticeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoticeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnPost.setOnClickListener {
            // Validate and create notice
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

