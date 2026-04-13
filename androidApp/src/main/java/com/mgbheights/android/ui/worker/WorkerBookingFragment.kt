package com.mgbheights.android.ui.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.FragmentWorkerBookingBinding

class WorkerBookingFragment : Fragment() {

    private var _binding: FragmentWorkerBookingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkerBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val workerTypes = listOf("Plumber", "Electrician", "Carpenter", "Painter", "Cleaner", "AC Technician")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, workerTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWorkerType.adapter = adapter

        binding.btnBook.setOnClickListener {
            attemptBooking()
        }
    }

    private fun attemptBooking() {
        val workerType = binding.spinnerWorkerType.selectedItem.toString()
        val description = binding.etDescription.text.toString().trim()
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please describe the issue.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnBook.isEnabled = false

        // Fetch user info (flat number, role) before booking
        val db = FirebaseFirestore.getInstance()
        val collections = listOf("residents", "tenants")
        var found = false
        
        fun createBooking(userData: Map<String, Any>, userType: String) {
            val booking = hashMapOf(
                "bookedBy" to user.uid,
                "userName" to (userData["name"] ?: ""),
                "userType" to userType,
                "workerType" to workerType,
                "description" to description,
                "flatNumber" to (userData["flatNumber"] ?: ""),
                "towerBlock" to (userData["towerBlock"] ?: ""),
                "status" to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("workerBookings").add(booking)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Booking request sent successfully!", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnBook.isEnabled = true
                    Toast.makeText(requireContext(), "Booking failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun checkCollections(index: Int) {
            if (index >= collections.size) {
                if (!found) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnBook.isEnabled = true
                    Toast.makeText(requireContext(), "User profile not found. Please complete your profile.", Toast.LENGTH_SHORT).show()
                }
                return
            }
            db.collection(collections[index]).document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        found = true
                        createBooking(doc.data ?: emptyMap(), collections[index].removeSuffix("s"))
                    } else {
                        checkCollections(index + 1)
                    }
                }
                .addOnFailureListener {
                    checkCollections(index + 1)
                }
        }

        checkCollections(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
