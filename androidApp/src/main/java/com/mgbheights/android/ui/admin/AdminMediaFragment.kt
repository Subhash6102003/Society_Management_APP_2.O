package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentAdminMediaBinding

class AdminMediaFragment : Fragment() {

    private var _binding: FragmentAdminMediaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup TabLayout and RecyclerView for media gallery
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
