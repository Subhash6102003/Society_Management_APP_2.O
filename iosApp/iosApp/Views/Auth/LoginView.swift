import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var phoneNumber = ""
    @State private var showOTPView = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    Spacer().frame(height: 40)

                    // Logo
                    HStack {
                        Spacer()
                        Circle()
                            .fill(Color("Primary").opacity(0.1))
                            .frame(width: 80, height: 80)
                            .overlay(
                                Image(systemName: "building.2")
                                    .font(.system(size: 32))
                                    .foregroundColor(Color("Primary"))
                            )
                        Spacer()
                    }

                    // Title
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Welcome to")
                            .font(.system(size: 28, weight: .bold))
                        Text("MGB Heights")
                            .font(.system(size: 32, weight: .black))
                            .foregroundColor(Color("Primary"))
                    }

                    Text("Enter your phone number to continue")
                        .font(.body)
                        .foregroundColor(.secondary)

                    // Phone Input
                    HStack(spacing: 12) {
                        Text("+91")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 16)
                            .background(Color(.systemGray6))
                            .cornerRadius(12)

                        TextField("Phone Number", text: $phoneNumber)
                            .keyboardType(.phonePad)
                            .textContentType(.telephoneNumber)
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(12)
                    }

                    // Error
                    if let error = authViewModel.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }

                    // Send OTP Button
                    Button(action: {
                        authViewModel.sendOTP(phoneNumber: phoneNumber)
                    }) {
                        HStack {
                            if authViewModel.isLoading {
                                ProgressView()
                                    .tint(.white)
                            }
                            Text("Send OTP")
                                .fontWeight(.semibold)
                            Image(systemName: "arrow.right")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(phoneNumber.count == 10 ? Color("Primary") : Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(16)
                    }
                    .disabled(phoneNumber.count != 10 || authViewModel.isLoading)

                    Spacer()
                }
                .padding(24)
            }
            .navigationDestination(isPresented: Binding(
                get: { authViewModel.verificationId != nil },
                set: { if !$0 { authViewModel.verificationId = nil } }
            )) {
                OTPVerificationView(phoneNumber: phoneNumber)
            }
        }
    }
}

struct OTPVerificationView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var otp = ""
    @State private var timeRemaining = 60
    @State private var canResend = false
    let phoneNumber: String
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                Text("Verify OTP")
                    .font(.system(size: 28, weight: .bold))

                Text("Enter the 6-digit code sent to +91 \(phoneNumber)")
                    .font(.body)
                    .foregroundColor(.secondary)

                // OTP Input
                TextField("Enter OTP", text: $otp)
                    .keyboardType(.numberPad)
                    .textContentType(.oneTimeCode)
                    .font(.system(size: 24, weight: .medium))
                    .multilineTextAlignment(.center)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)

                // Error
                if let error = authViewModel.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                }

                // Verify Button
                Button(action: {
                    authViewModel.verifyOTP(otp: otp)
                }) {
                    HStack {
                        if authViewModel.isLoading {
                            ProgressView().tint(.white)
                        }
                        Text("Verify")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(otp.count == 6 ? Color("Primary") : Color.gray)
                    .foregroundColor(.white)
                    .cornerRadius(16)
                }
                .disabled(otp.count != 6 || authViewModel.isLoading)

                // Resend
                HStack {
                    Spacer()
                    if canResend {
                        Button("Resend OTP") {
                            authViewModel.sendOTP(phoneNumber: phoneNumber)
                            timeRemaining = 60
                            canResend = false
                        }
                        .foregroundColor(Color("Primary"))
                    } else {
                        Text("Resend in \(timeRemaining)s")
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                }
                .font(.subheadline)
            }
            .padding(24)
        }
        .onReceive(timer) { _ in
            if timeRemaining > 0 {
                timeRemaining -= 1
            } else {
                canResend = true
            }
        }
    }
}



