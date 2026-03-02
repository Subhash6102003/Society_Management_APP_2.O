package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentOtpVerificationBinding
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpVerificationFragment : Fragment() {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()
    private val args: OtpVerificationFragmentArgs by navArgs()
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.storedVerificationId = args.verificationId

        binding.tvSubtitle.text = getString(R.string.otp_description, "+91 ${args.phoneNumber}")

        setupUI()
        observeViewModel()
        startResendTimer()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.etOtp.doAfterTextChanged { text ->
            binding.btnVerifyOtp.isEnabled = Validators.isValidOtp(text?.toString() ?: "")
            binding.tvError.isVisible = false
        }

        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtp.text?.toString() ?: ""
            if (Validators.isValidOtp(otp)) {
                viewModel.verifyOtp(otp)
            }
        }

        binding.btnResendOtp.setOnClickListener {
            binding.btnResendOtp.isEnabled = false
            startResendTimer()
            // Trigger resend via viewModel
        }
    }

    private fun observeViewModel() {
        viewModel.verifyOtpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    // Navigate to dashboard
                    findNavController().navigate(R.id.action_otp_to_dashboard)
                }
                is Resource.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        binding.btnResendOtp.isEnabled = false
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = getString(R.string.resend_otp_timer, seconds.toInt())
                binding.tvTimer.isVisible = true
            }

            override fun onFinish() {
                binding.tvTimer.isVisible = false
                binding.btnResendOtp.isEnabled = true
            }
        }.start()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressLoading.isVisible = loading
        binding.btnVerifyOtp.isEnabled = !loading
        binding.etOtp.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}

