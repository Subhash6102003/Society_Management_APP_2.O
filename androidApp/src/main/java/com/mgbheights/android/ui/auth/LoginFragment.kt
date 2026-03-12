package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentLoginBinding
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()
    private var hasNavigated = false

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hasNavigated = false

        // Check if already logged in via Firebase — auto-navigate
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { loggedIn ->
            if (loggedIn && !hasNavigated) {
                viewModel.currentUser.value?.let { state ->
                    if (state is Resource.Success) {
                        navigateBasedOnUser(state.data)
                    }
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success && viewModel.isLoggedIn.value == true && !hasNavigated) {
                navigateBasedOnUser(state.data)
            }
        }

        setupUI()
        observeViewModel()
    }

    private fun navigateBasedOnUser(user: com.mgbheights.shared.domain.model.User) {
        if (hasNavigated) return
        if (findNavController().currentDestination?.id != R.id.loginFragment) return

        val isAdminEmail = (firebaseAuth.currentUser?.email ?: "").equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true)
        val isEmailVerified = firebaseAuth.currentUser?.isEmailVerified == true

        // Admin skips email verification
        if (!isAdminEmail && !isEmailVerified) {
            hasNavigated = true
            viewModel.isFromSignUp = false
            viewModel.sendEmailVerification()
            findNavController().navigate(R.id.action_login_to_emailVerification)
            return
        }

        hasNavigated = true
        when {
            !user.isProfileComplete -> {
                findNavController().navigate(R.id.action_login_to_onboarding)
            }
            !user.isApproved && user.role != UserRole.ADMIN -> {
                findNavController().navigate(R.id.action_login_to_awaiting)
            }
            else -> {
                findNavController().navigate(R.id.action_login_to_dashboard)
            }
        }
    }

    private fun setupUI() {
        binding.etEmail.doAfterTextChanged {
            binding.tvError.isVisible = false
            updateLoginButtonState()
        }
        binding.etPassword.doAfterTextChanged {
            binding.tvError.isVisible = false
            updateLoginButtonState()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            if (Validators.isValidEmailRequired(email) && Validators.isValidPassword(password)) {
                viewModel.loginWithEmail(email, password)
            } else {
                if (!Validators.isValidEmailRequired(email)) {
                    showError("Please enter a valid email address")
                } else {
                    showError("Password must be at least 6 characters")
                }
            }
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_roleSelection)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }
    }

    private fun updateLoginButtonState() {
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        binding.btnLogin.isEnabled = email.isNotBlank() && password.isNotBlank()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    // Navigation handled by isLoggedIn / currentUser observer
                }
                is Resource.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressLoading.isVisible = loading
        binding.btnLogin.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
