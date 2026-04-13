import com.mgbheights.app.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import android.widget.ArrayAdapter
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.mgbheights.android.util.ImageUtils
import com.mgbheights.app.utils.ImageUtils
import com.google.firebase.firestore.FieldValue

    private var workerId: String? = null

    private var workerId: String? = null // Assume this is passed as an argument
    private var _binding: FragmentWorkerBookingBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
    private val binding get() = _binding!!

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        workerId?.let {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(it)
                .get()
        val description = binding.etDescription.text.toString().trim()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) return@addOnSuccessListener
                    binding.tvWorkerName.text = doc.getString("name") ?: ""

                    binding.tvWorkerRate.text = "₹${doc.getString("hourlyRate")}/hr"
        val description = binding.etDescription.text.toString().trim()
                            binding.ivWorkerPhoto.setImageBitmap(it)
                        }
                    }
            return
        }
                    binding.tvWorkerRate.text    = "₹${doc.getString("hourlyRate")}/hr"
                }
        }

        binding.btnBook.setOnClickListener {
            bookWorker()
        }
    }

    private fun bookWorker() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "You must be logged in to book.", Toast.LENGTH_SHORT).show()

        // Fetch user info (flat number, role) before booking

        val description = binding.etDescription.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a description.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val collections = listOf("residents", "tenants")
        
                "description" to description,
                "flatNumber" to (userData["flatNumber"] ?: ""),

                "status" to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("workerBookings").add(booking)
                "workerId" to workerId,
                    binding.btnBook.isEnabled = true
                    Toast.makeText(requireContext(), "Booking failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun checkCollections(index: Int) {
            if (index >= collections.size) {
                if (!found) {
                    binding.progressBar.visibility = View.GONE

                    Toast.makeText(requireContext(), "User profile not found. Please complete your profile.", Toast.LENGTH_SHORT).show()
                }
                return
            }
            db.collection(collections[index]).document(user.uid).get()
                .addOnSuccessListener { doc ->
                "workerType" to workerType,
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

        checkCollections(0)

        checkCollections(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
