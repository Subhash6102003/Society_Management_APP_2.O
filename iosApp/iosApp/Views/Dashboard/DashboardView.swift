import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var pendingDues: Double = 0
    @State private var recentNotices: [NoticeItem] = []

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Greeting
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Hello, \(authViewModel.currentUser?.name ?? "Resident")")
                                .font(.title2.bold())
                            if let flat = authViewModel.currentUser?.flatNumber,
                               let tower = authViewModel.currentUser?.towerBlock,
                               !flat.isEmpty {
                                Text("\(flat), \(tower)")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                        Spacer()
                        Button(action: {}) {
                            Image(systemName: "bell.badge")
                                .font(.title3)
                                .foregroundColor(.primary)
                        }
                    }
                    .padding(.horizontal)

                    // Pending Dues Card
                    if pendingDues > 0 {
                        HStack {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Pending Dues")
                                    .font(.subheadline)
                                    .foregroundColor(.white.opacity(0.8))
                                Text("₹\(String(format: "%.0f", pendingDues))")
                                    .font(.system(size: 32, weight: .bold))
                                    .foregroundColor(.white)
                            }
                            Spacer()
                            Button("Pay Now") {}
                                .buttonStyle(.bordered)
                                .tint(.white)
                        }
                        .padding()
                        .background(Color("Primary"))
                        .cornerRadius(16)
                        .padding(.horizontal)
                    }

                    // Quick Actions
                    Text("Quick Actions")
                        .font(.headline)
                        .padding(.horizontal)

                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 4), spacing: 12) {
                        QuickActionButton(icon: "creditcard", label: "Pay")
                        QuickActionButton(icon: "person.2", label: "Visitors")
                        QuickActionButton(icon: "exclamationmark.bubble", label: "Complaints")
                        QuickActionButton(icon: "megaphone", label: "Notices")
                    }
                    .padding(.horizontal)

                    // Recent Announcements
                    HStack {
                        Text("Announcements")
                            .font(.headline)
                        Spacer()
                        Button("View All") {}
                            .font(.subheadline)
                            .foregroundColor(Color("Primary"))
                    }
                    .padding(.horizontal)

                    if recentNotices.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "tray")
                                .font(.system(size: 40))
                                .foregroundColor(.secondary.opacity(0.5))
                            Text("No announcements")
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 40)
                    } else {
                        ForEach(recentNotices) { notice in
                            NoticeCard(notice: notice)
                                .padding(.horizontal)
                        }
                    }
                }
                .padding(.vertical)
            }
            .refreshable {
                // Refresh dashboard data
            }
            .navigationTitle("Dashboard")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct QuickActionButton: View {
    let icon: String
    let label: String

    var body: some View {
        VStack(spacing: 8) {
            Circle()
                .fill(Color(.systemGray6))
                .frame(width: 48, height: 48)
                .overlay(
                    Image(systemName: icon)
                        .foregroundColor(.primary)
                )
            Text(label)
                .font(.caption)
                .foregroundColor(.primary)
        }
    }
}

struct NoticeItem: Identifiable {
    let id: String
    let title: String
    let body: String
    let category: String
    let priority: String
    let createdByName: String
    let isEmergency: Bool
    let createdAt: Int64
}

struct NoticeCard: View {
    let notice: NoticeItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                if notice.isEmergency {
                    Text("🚨 Emergency")
                        .font(.caption.bold())
                        .foregroundColor(.red)
                }
                Text(notice.category)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color("Primary").opacity(0.1))
                    .foregroundColor(Color("Primary"))
                    .cornerRadius(8)
                Spacer()
                Text(notice.createdByName)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
            Text(notice.title)
                .font(.subheadline.bold())
            Text(notice.body)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(2)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 8, y: 4)
    }
}

