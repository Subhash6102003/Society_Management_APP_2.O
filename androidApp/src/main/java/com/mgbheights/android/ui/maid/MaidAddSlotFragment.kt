package com.mgbheights.android.ui.maid
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
class MaidAddSlotFragment : Fragment(R.layout.fragment_maid_add_slot) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnConfirmSlot).setOnClickListener {
            val flat = view.findViewById<EditText>(R.id.etFlatNumber).text.toString().trim()
            val building = view.findViewById<EditText>(R.id.etBuildingNumber).text.toString().trim()
            val workDays = view.findViewById<EditText>(R.id.etWorkDays).text.toString().trim()
            val workTimings = view.findViewById<EditText>(R.id.etWorkTimings).text.toString().trim()
            val workType = view.findViewById<EditText>(R.id.etWorkType).text.toString().trim()
            if (flat.isBlank() || building.isBlank() || workDays.isBlank() || workTimings.isBlank()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createSlotRequest(flat, building, workDays, workTimings, workType)
        }
    }
    private fun createSlotRequest(flat: String, building: String, workDays: String, workTimings: String, workType: String) {
        val maidUid = auth.currentUser?.uid ?: return
        db.collection("maids").document(maidUid).get().addOnSuccessListener { maidDoc ->
            val maidName = maidDoc.getString("name") ?: "Maid"
            findTarget(flat, building) { targetUid, targetType ->
                val request = hashMapOf(
                    "maidUid" to maidUid,
                    "maidName" to maidName,
                    "targetUid" to targetUid,
                    "targetUserType" to targetType,
                    "flatNumber" to flat,
                    "buildingNumber" to building,
                    "workDays" to workDays.split(",").map { it.trim() },
                    "workTimings" to workTimings,
                    "workType" to workType,
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("maidSlotRequests").add(request)
                    .addOnSuccessListener { Toast.makeText(requireContext(), "Request submitted", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(requireContext(), "Request failed", Toast.LENGTH_SHORT).show() }
            }
        }
    }
    private fun findTarget(flat: String, building: String, onFound: (String, String) -> Unit) {
        db.collection("residents")
            .whereEqualTo("flatNumber", flat)
            .whereEqualTo("buildingNumber", building)
            .limit(1)
            .get()
            .addOnSuccessListener { residentSnap ->
                val resident = residentSnap.documents.firstOrNull()
                if (resident != null) {
                    onFound(resident.id, "resident")
                    return@addOnSuccessListener
                }
                db.collection("tenants")
                    .whereEqualTo("flatNumber", flat)
                    .whereEqualTo("buildingNumber", building)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { tenantSnap ->
                        val tenant = tenantSnap.documents.firstOrNull()
                        if (tenant != null) {
                            onFound(tenant.id, "tenant")
                        } else {
                            Toast.makeText(requireContext(), "No resident/tenant found", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
    }
}
