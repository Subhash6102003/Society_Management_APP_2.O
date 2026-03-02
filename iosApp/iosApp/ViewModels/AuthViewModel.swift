import Foundation
import FirebaseAuth
import Combine

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isLoggedIn = false
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var verificationId: String?
    @Published var currentUser: UserModel?

    private var authStateHandle: AuthStateDidChangeListenerHandle?

    init() {
        checkAuthState()
    }

    func checkAuthState() {
        authStateHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            DispatchQueue.main.async {
                self?.isLoggedIn = user != nil
                if let user = user {
                    self?.fetchUserProfile(userId: user.uid)
                }
            }
        }
    }

    func sendOTP(phoneNumber: String) {
        isLoading = true
        errorMessage = nil

        PhoneAuthProvider.provider().verifyPhoneNumber(
            "+91\(phoneNumber)",
            uiDelegate: nil
        ) { [weak self] verificationId, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                if let error = error {
                    self?.errorMessage = error.localizedDescription
                    return
                }
                self?.verificationId = verificationId
            }
        }
    }

    func verifyOTP(otp: String) {
        guard let verificationId = verificationId else {
            errorMessage = "Verification ID not found"
            return
        }

        isLoading = true
        errorMessage = nil

        let credential = PhoneAuthProvider.provider().credential(
            withVerificationID: verificationId,
            verificationCode: otp
        )

        Auth.auth().signIn(with: credential) { [weak self] result, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                if let error = error {
                    self?.errorMessage = error.localizedDescription
                    return
                }
                self?.isLoggedIn = true
                if let userId = result?.user.uid {
                    self?.fetchUserProfile(userId: userId)
                }
            }
        }
    }

    func signOut() {
        do {
            try Auth.auth().signOut()
            isLoggedIn = false
            currentUser = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func fetchUserProfile(userId: String) {
        // Fetch from Firestore using shared KMP business logic
    }

    deinit {
        if let handle = authStateHandle {
            Auth.auth().removeStateDidChangeListener(handle)
        }
    }
}

struct UserModel: Identifiable, Codable {
    var id: String = ""
    var phoneNumber: String = ""
    var name: String = ""
    var email: String = ""
    var profilePhotoUrl: String = ""
    var role: String = "RESIDENT"
    var flatNumber: String = ""
    var towerBlock: String = ""
    var houseNumber: String = ""
    var isApproved: Bool = false
    var isBlocked: Bool = false
    var isProfileComplete: Bool = false
    var isOnboarded: Bool = false
    var tenantOf: String = ""
    var createdAt: Int64 = 0
    var updatedAt: Int64 = 0
}

