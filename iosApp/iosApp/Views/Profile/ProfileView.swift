import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var showLogoutAlert = false

    var body: some View {
        NavigationStack {
            List {
                // Profile Header
                Section {
                    HStack(spacing: 16) {
                        Circle()
                            .fill(Color(.systemGray5))
                            .frame(width: 72, height: 72)
                            .overlay(
                                Image(systemName: "person.fill")
                                    .font(.title)
                                    .foregroundColor(.secondary)
                            )
                            .overlay(alignment: .bottomTrailing) {
                                Button(action: {}) {
                                    Image(systemName: "camera.fill")
                                        .font(.caption)
                                        .foregroundColor(.white)
                                        .padding(6)
                                        .background(Color("Primary"))
                                        .clipShape(Circle())
                                }
                            }

                        VStack(alignment: .leading, spacing: 4) {
                            Text(authViewModel.currentUser?.name ?? "Resident")
                                .font(.title3.bold())
                            Text(authViewModel.currentUser?.role.replacingOccurrences(of: "_", with: " ") ?? "RESIDENT")
                                .font(.caption)
                                .foregroundColor(Color("Primary"))
                            Text(authViewModel.currentUser?.phoneNumber ?? "")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.vertical, 8)
                }

                // House Details
                Section("House Details") {
                    LabeledRow(label: "Flat Number", value: authViewModel.currentUser?.flatNumber ?? "-")
                    LabeledRow(label: "Tower / Block", value: authViewModel.currentUser?.towerBlock ?? "-")
                    LabeledRow(label: "Email", value: authViewModel.currentUser?.email.isEmpty == false ? authViewModel.currentUser!.email : "Not set")
                }

                // Quick Links
                Section("Quick Links") {
                    NavigationLink(destination: Text("Payment History")) {
                        Label("Payment History", systemImage: "creditcard")
                    }
                    NavigationLink(destination: Text("My Complaints")) {
                        Label("My Complaints", systemImage: "exclamationmark.bubble")
                    }
                    NavigationLink(destination: Text("Emergency Contacts")) {
                        Label("Emergency Contacts", systemImage: "phone.fill")
                    }
                }

                // Logout
                Section {
                    Button(role: .destructive, action: {
                        showLogoutAlert = true
                    }) {
                        HStack {
                            Spacer()
                            Text("Logout")
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("Profile")
            .alert("Logout", isPresented: $showLogoutAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Logout", role: .destructive) {
                    authViewModel.signOut()
                }
            } message: {
                Text("Are you sure you want to logout?")
            }
        }
    }
}

struct LabeledRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .fontWeight(.medium)
        }
    }
}

