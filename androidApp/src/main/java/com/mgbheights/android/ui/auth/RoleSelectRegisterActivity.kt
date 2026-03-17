package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mgbheights.android.databinding.ActivityRoleSelectRegisterBinding

class RoleSelectRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roles = arrayOf("Admin", "Resident", "Tenant", "Security Guard", "Worker")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actRole.setAdapter(adapter)

        binding.btnContinue.setOnClickListener {
            val selectedRole = binding.actRole.text.toString()
            if (selectedRole.isEmpty()) {
                binding.tilRole.error = "Please select a role"
                return@setOnClickListener
            }

            when (selectedRole) {
                "Admin"          -> startActivity(Intent(this, AdminRegisterActivity::class.java))
                "Resident"       -> startActivity(Intent(this, ResidentRegisterActivity::class.java))
                "Tenant"         -> startActivity(Intent(this, TenantRegisterActivity::class.java))
                "Security Guard" -> startActivity(Intent(this, GuardRegisterActivity::class.java))
                "Worker"         -> startActivity(Intent(this, WorkerRegisterActivity::class.java))
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}
