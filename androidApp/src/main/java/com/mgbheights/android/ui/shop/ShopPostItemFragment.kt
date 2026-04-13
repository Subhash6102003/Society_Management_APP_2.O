package com.mgbheights.android.ui.shop
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
class ShopPostItemFragment : Fragment(R.layout.fragment_shop_post_item) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnPostItem).setOnClickListener {
            val title = view.findViewById<EditText>(R.id.etTitle).text.toString().trim()
            val description = view.findViewById<EditText>(R.id.etDescription).text.toString().trim()
            val category = view.findViewById<EditText>(R.id.etCategory).text.toString().trim()
            val condition = view.findViewById<EditText>(R.id.etCondition).text.toString().trim()
            val price = view.findViewById<EditText>(R.id.etPrice).text.toString().toDoubleOrNull() ?: 0.0
            val images = listOf(
                view.findViewById<EditText>(R.id.etImage1).text.toString().trim(),
                view.findViewById<EditText>(R.id.etImage2).text.toString().trim(),
                view.findViewById<EditText>(R.id.etImage3).text.toString().trim(),
                view.findViewById<EditText>(R.id.etImage4).text.toString().trim()
            ).filter { it.isNotBlank() }.take(4)
            if (title.isBlank() || description.isBlank()) {
                Toast.makeText(requireContext(), "Title and description required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val payload = hashMapOf<String, Any>(
                "postedByUid" to uid,
                "postedByName" to "",
                "postedByUserType" to "",
                "postedByFlat" to "",
                "postedByBuilding" to "",
                "title" to title,
                "description" to description,
                "category" to category,
                "price" to price,
                "isFree" to (price == 0.0),
                "condition" to condition,
                "images" to images,
                "status" to "available",
                "interestedBuyerUids" to emptyList<String>(),
                "soldToUid" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("shopListings").add(payload)
                .addOnSuccessListener { Toast.makeText(requireContext(), "Listing posted", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(requireContext(), "Failed to post", Toast.LENGTH_SHORT).show() }
        }
    }
}
