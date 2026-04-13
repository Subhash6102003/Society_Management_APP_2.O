class MaidMyFlatSlotsFragment : Fragment(R.layout.fragment_maid_my_flat_slots)
package com.mgbheights.android.ui.maid
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
