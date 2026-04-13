package com.mgbheights.android.utils

import com.mgbheights.shared.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseHelper @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun approveMaidSlotRequest(requestId: String, maidUid: String) {
        supabase.from("maid_slot_requests").update(
            mapOf("status" to "approved")
        ) { filter { eq("id", requestId) } }
    }

    suspend fun rejectMaidSlotRequest(requestId: String) {
        supabase.from("maid_slot_requests").update(
            mapOf("status" to "rejected")
        ) { filter { eq("id", requestId) } }
    }
}
