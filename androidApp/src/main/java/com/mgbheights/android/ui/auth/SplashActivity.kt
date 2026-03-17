package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Wait 1.5 seconds then check auth
        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                goToAuth()
            } else {
                user.reload().addOnCompleteListener {
                    if (!user.isEmailVerified) {
                        goToAuth()
                    } else {
                        checkRoleAndRoute(user.uid)
                    }
                }
            }
        }, 1500)
    }

    private fun checkRoleAndRoute(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val cols = listOf("admins", "residents", "tenants", "guards", "workers")
        var i = 0
        fun next() {
            if (i >= cols.size) {
                FirebaseAuth.getInstance().signOut()
                goToAuth()
                return
            }
            db.collection(cols[i]).document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val intent = when (doc.getString("role")) {
                            "admin"    -> Intent(this, AdminActivity::class.java)
                            "resident" -> Intent(this, ResidentActivity::class.java)
                            "tenant"   -> Intent(this, TenantActivity::class.java)
                            "guard"    -> Intent(this, GuardActivity::class.java)
                            "worker"   -> Intent(this, WorkerActivity::class.java)
                            else       -> null
                        }
                        if (intent != null) {
                            startActivity(intent)
                            finish()
                        } else {
                            i++
                            next()
                        }
                    } else {
                        i++
                        next()
                    }
                }
                .addOnFailureListener {
                    i++
                    next()
                }
        }
        next()
    }

    private fun goToAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}
