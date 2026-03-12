package com.mgbheights.android

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.mgbheights.android.databinding.ActivityMainBinding
import com.mgbheights.android.ui.auth.AuthViewModel
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var connectivityManager: ConnectivityManager? = null

    // Fix #8: Track the current user's role so we can route the Dashboard tab correctly
    private val authViewModel: AuthViewModel by viewModels()
    private var currentUserRole: UserRole? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread { binding.offlineBanner.visibility = View.GONE }
        }

        override fun onLost(network: Network) {
            runOnUiThread { binding.offlineBanner.visibility = View.VISIBLE }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge insets — handle both system bars and keyboard (IME)
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinatorLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, maxOf(systemBars.bottom, ime.bottom))
            insets
        }

        setupNavigation()
        setupNetworkMonitoring()
        observeUserRole()
    }

    /** Fix #8: Keep currentUserRole in sync so the bottom-nav handler knows where to route. */
    private fun observeUserRole() {
        authViewModel.currentUser.observe(this) { state ->
            if (state is Resource.Success) {
                currentUserRole = state.data.role
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setOnItemReselectedListener { /* do nothing on reselect */ }
        binding.bottomNav.setupWithNavController(navController)

        // Fix #8: Override item-selected to intercept the Dashboard tab for Guard/Worker roles.
        // setupWithNavController() registers its own setOnItemSelectedListener; we replace it here
        // so we control Dashboard-tab routing without losing the rest of the default behaviour.
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.dashboardFragment) {
                val role = currentUserRole
                when (role) {
                    UserRole.SECURITY_GUARD, UserRole.SECURITY_GUARD_WORKER -> {
                        // Navigate directly to the guard dashboard, collapsing any intermediate
                        // screens on the back stack and avoiding duplicate instances.
                        val opts = NavOptions.Builder()
                            .setPopUpTo(R.id.guardDashboardFragment, inclusive = false)
                            .setLaunchSingleTop(true)
                            .build()
                        try { navController.navigate(R.id.guardDashboardFragment, null, opts) }
                        catch (_: Exception) {
                            // Destination not on stack yet — plain navigate
                            navController.navigate(R.id.guardDashboardFragment)
                        }
                        return@setOnItemSelectedListener true
                    }
                    UserRole.WORKER -> {
                        val opts = NavOptions.Builder()
                            .setPopUpTo(R.id.workerDashboardFragment, inclusive = false)
                            .setLaunchSingleTop(true)
                            .build()
                        try { navController.navigate(R.id.workerDashboardFragment, null, opts) }
                        catch (_: Exception) {
                            navController.navigate(R.id.workerDashboardFragment)
                        }
                        return@setOnItemSelectedListener true
                    }
                    else -> { /* ADMIN, RESIDENT, TENANT — fall through to default */ }
                }
            }
            // Default behaviour for all other tabs (and non-role-specific Dashboard taps)
            NavigationUI.onNavDestinationSelected(item, navController)
        }

        // Show/hide bottom nav based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNav = when (destination.id) {
                R.id.dashboardFragment,
                R.id.guardDashboardFragment,
                R.id.workerDashboardFragment,
                R.id.maintenanceListFragment,
                R.id.visitorListFragment,
                R.id.noticeListFragment,
                R.id.profileFragment -> true
                else -> false
            }
            binding.bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE

            // Keep Dashboard tab selected for role-specific dashboards
            val roleDashboards = setOf(
                R.id.guardDashboardFragment,
                R.id.workerDashboardFragment
            )
            if (destination.id in roleDashboards) {
                binding.bottomNav.menu.findItem(R.id.dashboardFragment)?.isChecked = true
            }

            // Adjust fragment container margin
            val params = binding.navHostFragment.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
            params.bottomMargin = if (showBottomNav) resources.getDimensionPixelSize(R.dimen.bottom_nav_height) else 0
            binding.navHostFragment.layoutParams = params
        }
    }

    private fun setupNetworkMonitoring() {
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        binding.offlineBanner.visibility = if (isConnected) View.GONE else View.VISIBLE

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {}
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }
}
