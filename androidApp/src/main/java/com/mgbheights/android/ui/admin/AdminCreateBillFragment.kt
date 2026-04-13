package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mgbheights.android.R

class AdminCreateBillFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Reuse fragment_bill_detail or a placeholder if specific layout missing
        return inflater.inflate(R.layout.fragment_bill_detail, container, false)
    }
}
