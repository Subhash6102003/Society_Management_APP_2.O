import Foundation
import Combine

// MARK: - UserModel

struct UserModel: Identifiable {
    var id: String
    var email: String
    var name: String
    var role: String
    var isApproved: Bool
    var isProfileComplete: Bool
}

// MARK: - AuthViewModel

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isLoggedIn = false
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var currentUser: UserModel?

    private let supabaseURL = "https://ggoqcgfjehfuupprchdl.supabase.co"
    // Set your Supabase anon key here
    private let supabaseAnonKey = "your_supabase_anon_key_here"

    init() {
        checkAuthState()
    }

    func checkAuthState() {
        guard let session = loadStoredSession() else {
            isLoggedIn = false
            return
        }
        isLoggedIn = true
        Task { await fetchUserProfile(userId: session) }
    }

    func loginWithEmailPassword(email: String, password: String) {
        isLoading = true
        errorMessage = nil
        Task {
            do {
                let uid = try await signInWithSupabase(email: email, password: password)
                storeSession(uid: uid)
                await fetchUserProfile(userId: uid)
                isLoggedIn = true
            } catch {
                errorMessage = error.localizedDescription
            }
            isLoading = false
        }
    }

    func submitSignupRequest(name: String, email: String, password: String = "") {
        let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedName.isEmpty, !trimmedEmail.isEmpty, !password.isEmpty else {
            errorMessage = "Please fill all required fields"
            return
        }
        isLoading = true
        errorMessage = nil
        Task {
            do {
                let uid = try await signUpWithSupabase(email: trimmedEmail, password: password, name: trimmedName)
                storeSession(uid: uid)
                errorMessage = "Account created. Waiting for admin approval."
            } catch {
                errorMessage = error.localizedDescription
            }
            isLoading = false
        }
    }

    func signOut() {
        clearSession()
        currentUser = nil
        isLoggedIn = false
        errorMessage = nil
    }

    // MARK: - Supabase Auth

    private func signInWithSupabase(email: String, password: String) async throws -> String {
        let url = URL(string: "\(supabaseURL)/auth/v1/token?grant_type=password")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(supabaseAnonKey, forHTTPHeaderField: "apikey")
        let body = ["email": email, "password": password]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        let (data, _) = try await URLSession.shared.data(for: request)
        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let uid = json["user"] as? [String: Any],
           let id = uid["id"] as? String { return id }
        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let msg = json["error_description"] as? String { throw NSError(domain: "", code: 401, userInfo: [NSLocalizedDescriptionKey: msg]) }
        throw NSError(domain: "", code: 401, userInfo: [NSLocalizedDescriptionKey: "Login failed"])
    }

    private func signUpWithSupabase(email: String, password: String, name: String) async throws -> String {
        let url = URL(string: "\(supabaseURL)/auth/v1/signup")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(supabaseAnonKey, forHTTPHeaderField: "apikey")
        let body: [String: Any] = ["email": email, "password": password, "data": ["name": name]]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        let (data, _) = try await URLSession.shared.data(for: request)
        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let id = json["id"] as? String { return id }
        throw NSError(domain: "", code: 400, userInfo: [NSLocalizedDescriptionKey: "Sign up failed"])
    }

    private func fetchUserProfile(userId: String) async {
        guard let url = URL(string: "\(supabaseURL)/rest/v1/users?id=eq.\(userId)&select=*") else { return }
        var request = URLRequest(url: url)
        request.setValue(supabaseAnonKey, forHTTPHeaderField: "apikey")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        guard let (data, _) = try? await URLSession.shared.data(for: request),
              let arr = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]],
              let row = arr.first else { return }
        currentUser = UserModel(
            id: row["id"] as? String ?? userId,
            email: row["email"] as? String ?? "",
            name: row["name"] as? String ?? "",
            role: row["role"] as? String ?? "RESIDENT",
            isApproved: row["is_approved"] as? Bool ?? false,
            isProfileComplete: row["is_profile_complete"] as? Bool ?? false
        )
    }

    // MARK: - Session persistence

    private func storeSession(uid: String) {
        UserDefaults.standard.set(uid, forKey: "mgb_user_id")
    }

    private func loadStoredSession() -> String? {
        UserDefaults.standard.string(forKey: "mgb_user_id")
    }

    private func clearSession() {
        UserDefaults.standard.removeObject(forKey: "mgb_user_id")
    }
}
