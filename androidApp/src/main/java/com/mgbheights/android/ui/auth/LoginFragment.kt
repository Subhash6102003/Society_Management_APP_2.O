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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentLoginBinding
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if already logged in via Firebase — auto-navigate to dashboard
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { loggedIn ->
            if (loggedIn) {
                findNavController().navigate(R.id.action_login_to_dashboard)
            }
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.etPhone.doAfterTextChanged { text ->
            binding.btnSendOtp.isEnabled = Validators.isValidPhoneNumber(text?.toString() ?: "")
            binding.tvError.isVisible = false
        }

        binding.btnSendOtp.setOnClickListener {
            val phone = binding.etPhone.text?.toString() ?: ""
            if (Validators.isValidPhoneNumber(phone)) {
                sendOtp(phone)
            }
        }
    }

    private fun sendOtp(phone: String) {
        setLoading(true)
        viewModel.phoneNumber = phone

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber("+91$phone")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Timber.d("Auto verification completed")
                    setLoading(false)
                    // Auto-sign in with the credential
                    viewModel.signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Timber.e(e, "Verification failed")
                    setLoading(false)
                    showError(e.message ?: getString(R.string.error_generic))
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    Timber.d("Code sent: $verificationId")
                    setLoading(false)
                    viewModel.storedVerificationId = verificationId

                    val action = LoginFragmentDirections.actionLoginToOtp(
                        phoneNumber = phone,
                        verificationId = verificationId
                    )
                    findNavController().navigate(action)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun observeViewModel() {
        viewModel.sendOtpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> setLoading(false)
                is Resource.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressLoading.isVisible = loading
        binding.btnSendOtp.isEnabled = !loading
        binding.etPhone.isEnabled = !loading
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
