package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentEmailVerificationBinding
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EmailVerificationFragment : Fragment() {

    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show the email address
        val email = firebaseAuth.currentUser?.email ?: viewModel.signUpEmail
        binding.tvEmailAddress.text = email

        setupUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Auto-check verification when user returns to the app (e.g., after clicking email link)
        checkVerification()
    }

    private fun setupUI() {
        binding.btnCheckVerification.setOnClickListener {
            checkVerification()
        }

        binding.btnOpenEmailApp.setOnClickListener {
            openEmailApp()
        }

        binding.tvResendEmail.setOnClickListener {
            resendVerificationEmail()
        }

        binding.tvLogout.setOnClickListener {
            viewModel.signOut()
            findNavController().navigate(R.id.action_emailVerification_to_login)
        }
    }

    private fun checkVerification() {
        setLoading(true)
        val user = firebaseAuth.currentUser
        if (user == null) {
            setLoading(false)
            showStatus("Not logged in. Please login again.", isError = true)
            return
        }

        // Reload user to get fresh email verification status
        user.reload().addOnCompleteListener { task ->
            if (_binding == null) return@addOnCompleteListener
            setLoading(false)
            if (task.isSuccessful) {
                val refreshedUser = firebaseAuth.currentUser
                if (refreshedUser?.isEmailVerified == true) {
                    Timber.d("Email verified successfully!")
                    showStatus(getString(R.string.email_verified_success), isError = false)

                    // Check if this is from signup flow (profile not complete yet)
                    if (viewModel.isFromSignUp) {
                        // Complete the profile now
                        viewModel.completeSignUpProfile(
                            viewModel.signUpName,
                            viewModel.signUpFlatNumber,
                            viewModel.signUpTowerBlock
                        )
                    } else {
                        // From login flow - navigate based on user state
                        viewModel.getCurrentUser()
                    }
                } else {
                    showStatus(getString(R.string.email_not_verified), isError = true)
                }
            } else {
                Timber.e(task.exception, "Failed to reload user")
                showStatus("Failed to check verification status. Try again.", isError = true)
            }
        }
    }

    private fun resendVerificationEmail() {
        setLoading(true)
        val user = firebaseAuth.currentUser
        if (user == null) {
            setLoading(false)
            showStatus("Not logged in. Please login again.", isError = true)
            return
        }

        user.sendEmailVerification().addOnCompleteListener { task ->
            if (_binding == null) return@addOnCompleteListener
            setLoading(false)
            if (task.isSuccessful) {
                showStatus(getString(R.string.verification_email_sent), isError = false)
            } else {
                showStatus(
                    task.exception?.message ?: "Failed to send verification email",
                    isError = true
                )
            }
        }
    }

    private fun openEmailApp() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_EMAIL)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showStatus("No email app found on this device", isError = true)
        }
    }

    private fun observeViewModel() {
        // Observe signup state (for when profile is completed after email verification)
        viewModel.signUpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    val user = state.data
                    if (user.role == UserRole.ADMIN) {
                        findNavController().navigate(R.id.action_emailVerification_to_dashboard)
                    } else {
                        findNavController().navigate(R.id.action_emailVerification_to_awaiting)
                    }
                }

                is Resource.Error -> {
                    setLoading(false)
                    showStatus(state.message, isError = true)
                }
            }
        }

        // Observe current user (for login flow after email verification)
        viewModel.currentUser.observe(viewLifecycleOwner) { state ->
            if (!viewModel.isFromSignUp && state is Resource.Success) {
                val user = state.data
                when {
                    !user.isProfileComplete -> {
                        findNavController().navigate(R.id.action_emailVerification_to_onboarding)
                    }

                    !user.isApproved && user.role != UserRole.ADMIN -> {
                        findNavController().navigate(R.id.action_emailVerification_to_awaiting)
                    }

                    else -> {
                        findNavController().navigate(R.id.action_emailVerification_to_dashboard)
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        _binding?.let {
            it.progressLoading.isVisible = loading
            it.btnCheckVerification.isEnabled = !loading
        }
    }

    private fun showStatus(message: String, isError: Boolean) {
        val currentContext = context ?: return
        _binding?.let {
            it.tvStatus.text = message
            it.tvStatus.setTextColor(
                ContextCompat.getColor(
                    currentContext,
                    if (isError) R.color.error else R.color.primary
                )
            )
            it.tvStatus.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
