package com.mgbheights.android.utils

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun approveMaidSlotRequest(
        requestId: String,
        maidUid: String,
        targetUid: String,
        targetUserType: String,
        flatObject: Map<String, Any>
    ) {
        db.collection("maidSlotRequests").document(requestId).update("status", "approved")
        db.collection("maids").document(maidUid)
            .update("assignedFlats", FieldValue.arrayUnion(flatObject))

        val targetCollection = if (targetUserType == "resident") "residents" else "tenants"
        db.collection(targetCollection).document(targetUid)
            .update("assignedMaidUids", FieldValue.arrayUnion(maidUid))
    }

    fun rejectMaidSlotRequest(requestId: String) {
        db.collection("maidSlotRequests").document(requestId).update("status", "rejected")
    }
}

