package com.mgbheights.android.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.ActivityAdminBinding
import com.mgbheights.android.ui.auth.LoginActivity
import com.mgbheights.android.util.GlideApp // Assuming GlideApp is generated or just use Glide

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.navHostFragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.adminDashboardFragment, R.id.adminResidentsFragment, R.id.adminTenantsFragment,
                R.id.adminGuardsFragment, R.id.adminWorkersFragment, R.id.adminComplaintsFragment,
                R.id.adminNoticesFragment, R.id.adminVisitorsLogFragment, R.id.adminMediaFragment,
                R.id.adminProfileFragment
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                logout()
                true
            } else {
                val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                handled
            }
        }

        updateNavHeader()
    }

    private fun updateNavHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tvAdminName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvAdminEmail)
        val ivProfile = headerView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.ivAdminProfile)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            tvEmail.text = user.email
            FirebaseFirestore.getInstance().collection("admins").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        tvName.text = doc.getString("name")
                        // Load image with Glide if needed
                    }
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
