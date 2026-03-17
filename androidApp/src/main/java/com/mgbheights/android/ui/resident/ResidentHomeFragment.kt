package com.mgbheights.android.ui.resident

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentResidentHomeBinding

class ResidentHomeFragment : Fragment() {

    private var _binding: FragmentResidentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResidentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("residents").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        binding.tvWelcome.text = "Welcome, ${doc.getString("name")}"
                        binding.tvFlatNumber.text = "Flat: ${doc.getString("flatNumber")}"
                    }
                }
        }

        binding.btnRaiseComplaint.setOnClickListener {
            findNavController().navigate(R.id.residentRaiseComplaintFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
