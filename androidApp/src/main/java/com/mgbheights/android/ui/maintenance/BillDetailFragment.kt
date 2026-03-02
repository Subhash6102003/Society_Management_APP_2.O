package com.mgbheights.android.ui.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentBillDetailBinding
import com.mgbheights.shared.domain.model.BillStatus
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.domain.usecase.maintenance.GetBillsUseCase
import com.mgbheights.shared.util.DateTimeUtil
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class BillDetailFragment : Fragment() {

    private var _binding: FragmentBillDetailBinding? = null
    private val binding get() = _binding!!
    private val args: BillDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBillDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        // Bill detail would be loaded via ViewModel in production
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

