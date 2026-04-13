


                .font(.subheadline)
    @State private var email = ""
    @State private var password = ""
    @State private var showPassword = false
    @State private var showSignUp = false
        }
        .onReceive(timer) { _ in
            if timeRemaining > 0 {
                timeRemaining -= 1
                VStack(alignment: .leading, spacing: 18) {
                    RoundedRectangle(cornerRadius: 26, style: .continuous)
                        .fill(Color("Primary").opacity(0.08))
                        .frame(height: 190)
                        .overlay(
                            Image(systemName: "building.2.fill")
                                .font(.system(size: 56, weight: .medium))
                                .foregroundColor(Color("Primary"))
                        )
                    } else {
                    Text("Welcome to")
                        .font(.system(size: 52, weight: .bold))

                    Text("MGB Heights")
                        .font(.system(size: 56, weight: .black))
                        .foregroundColor(Color("Primary"))
                    .foregroundColor(.white)
                    Text("Enter your email and password to continue")
                        .font(.title3)
                .disabled(otp.count != 6 || authViewModel.isLoading)
                        .padding(.bottom, 4)

                    authField(
                        title: "Email Address",
                        systemIcon: "person.fill",
                        text: $email,
                        isSecure: false,
                        reveal: $showPassword
                    )
                        Text("Verify")
                    authField(
                        title: "Password",
                        systemIcon: "rectangle.on.rectangle",
                        text: $password,
                        isSecure: true,
                        reveal: $showPassword
                    )
                    authViewModel.verifyOTP(otp: otp)
                    HStack {
                        Spacer()
                        Button("Forgot Password?") {
                            authViewModel.errorMessage = "Password reset is currently handled by admin support."
                        }
                        .font(.title3.weight(.medium))
                        .foregroundColor(Color("Primary"))
                    .keyboardType(.numberPad)
                    .textContentType(.oneTimeCode)
                    .multilineTextAlignment(.center)
                    .padding()
                            .font(.callout)
                    .cornerRadius(12)
                Text("Enter the 6-digit code sent to +91 \(phoneNumber)")
                    .font(.body)
        ScrollView {
                        authViewModel.loginWithEmailPasswordDemo(email: email, password: password)
                Text("Verify OTP")
                    .font(.system(size: 28, weight: .bold))
    @State private var otp = ""
    @State private var timeRemaining = 60
    @State private var canResend = false
    let phoneNumber: String
                            Text("Login")
                                .font(.title3.weight(.semibold))
                    // Error
import SwiftUI
                        .frame(height: 62)
                        .background(canLogin ? Color("Primary") : Color.gray.opacity(0.35))
    @EnvironmentObject var authViewModel: AuthViewModel
                        .cornerRadius(20)
    @State private var showOTPView = false
                    .disabled(!canLogin || authViewModel.isLoading)
                    .padding(.top, 8)

                    HStack(spacing: 8) {
                        Text("Don't have an account?")
                            .foregroundColor(Color("Primary"))
                        Button("Sign Up") {
                            showSignUp = true
                        }
                        .fontWeight(.semibold)
                        .foregroundColor(Color("Primary"))
                    }
                    .font(.title2)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 20)

                    Text("By continuing, you agree to MGB Heights\nTerms of Service and Privacy Policy")
                        .font(.title3)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                        .padding(.top, 18)
                VStack(alignment: .leading, spacing: 24) {
                    Spacer().frame(height: 40)

                .padding(26)
            }
            .sheet(isPresented: $showSignUp) {
                SignUpView()
                    .environmentObject(authViewModel)
                VStack(alignment: .leading, spacing: 24) {
        }
    }

    private var canLogin: Bool {
        !email.trimmingCharacters(in: .whitespaces).isEmpty && !password.isEmpty
                            .frame(width: 80, height: 80)

    @ViewBuilder
    private func authField(
        title: String,
        systemIcon: String,
        text: Binding<String>,
        isSecure: Bool,
        reveal: Binding<Bool>
    ) -> some View {
        HStack(spacing: 14) {
            Image(systemName: systemIcon)
                .foregroundColor(.secondary)
                .frame(width: 22)

            Group {
                if isSecure && !reveal.wrappedValue {
                    SecureField(title, text: text)
                        .textContentType(.password)
                } else {
                    TextField(title, text: text)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(true)
                        .textContentType(isSecure ? .password : .emailAddress)
                }
            }
            .font(.title3)
                                Image(systemName: "building.2")
            if isSecure {
                Button {
                    reveal.wrappedValue.toggle()
                } label: {
                    Image(systemName: reveal.wrappedValue ? "eye.slash" : "eye")
                        .foregroundColor(.secondary)
                }
            }
        }
        .frame(height: 64)
        .padding(.horizontal, 16)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(Color.secondary.opacity(0.45), lineWidth: 1)
        )
    }
}

struct SignUpView: View {
    @Environment(\.dismiss) private var dismiss
                    // Title
    @State private var fullName = ""
    @State private var email = ""
    @State private var password = ""
    @State private var showPassword = false
    @State private var agreedToTerms = false
                        .font(.body)
                    }
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Create Account")
                        .font(.largeTitle.bold())
                    // Phone Input
                    Text("Submit your details. Admin approval is required before login.")
                        .foregroundColor(.secondary)
                            .foregroundColor(.secondary)
                    Group {
                        TextField("Full Name", text: $fullName)
                        TextField("Email Address", text: $email)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled(true)
                            .textContentType(.telephoneNumber)
                        Group {
                            if showPassword {
                                TextField("Password", text: $password)
                            } else {
                                SecureField("Password", text: $password)
                            }
                    }) {
                    }
                    .textFieldStyle(.roundedBorder)

                    Toggle("I agree to Terms of Service and Privacy Policy", isOn: $agreedToTerms)
                        .toggleStyle(.switch)

                    Button {
                        authViewModel.submitSignupRequest(name: fullName, email: email)
                        dismiss()
                    } label: {
                        Text("Sign Up")
                            if authViewModel.isLoading {
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(canSubmit ? Color("Primary") : Color.gray.opacity(0.3))
                            .foregroundColor(.white)
                            .cornerRadius(14)
                    }
                    .disabled(!canSubmit)
                        .cornerRadius(16)
                    Button(showPassword ? "Hide Password" : "Show Password") {
                        showPassword.toggle()
                    }
                    .foregroundColor(Color("Primary"))

                    Spacer(minLength: 10)
                    .textContentType(.oneTimeCode)
                .padding(22)

                if let error = authViewModel.errorMessage {
                    Text(error)
                        .font(.callout)
                        .foregroundColor(.red)
                        .padding(.horizontal, 22)
                }
            }
            .navigationTitle("Sign Up")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }
                        if authViewModel.isLoading {
    private var canSubmit: Bool {
        !fullName.trimmingCharacters(in: .whitespaces).isEmpty &&
        !email.trimmingCharacters(in: .whitespaces).isEmpty &&
        !password.isEmpty &&
        agreedToTerms
    }
}
                get: { authViewModel.verificationId != nil },
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



