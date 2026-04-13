package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentAdminResidentDetailBinding
import com.mgbheights.shared.domain.model.User

class AdminResidentDetailFragment : Fragment() {

    private var _binding: FragmentAdminResidentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: AdminResidentDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminResidentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadResidentDetails()
    }

    private fun loadResidentDetails() {
        FirebaseFirestore.getInstance().collection("residents").document(args.userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(com.mgbheights.shared.domain.model.User::class.java)
                    if (user != null) {
                        displayDetails(user)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayDetails(user: User) {
        binding.tvName.text = user.name
        binding.tvFlatNumber.text = "Flat: ${user.flatNumber}"
        binding.tvPhone.text = "Phone: ${user.phoneNumber}"
        binding.tvEmail.text = "Email: ${user.email}"

        Glide.with(this)
            .load(user.profilePhotoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(binding.ivProfile)
            
        // Load complaints history if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
