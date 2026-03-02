import SwiftUI

struct VisitorsView: View {
    @State private var selectedTab = "All"
    @State private var visitors: [VisitorItem] = []
    @State private var isLoading = false
    @State private var showAddVisitor = false

    let tabs = ["All", "Pending", "Active", "History"]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("Filter", selection: $selectedTab) {
                    ForEach(tabs, id: \.self) { tab in
                        Text(tab).tag(tab)
                    }
                }
                .pickerStyle(.segmented)
                .padding()

                if isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if visitors.isEmpty {
                    Spacer()
                    VStack(spacing: 16) {
                        Image(systemName: "person.2.slash")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary.opacity(0.3))
                        Text("No visitors found")
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                } else {
                    List(visitors) { visitor in
                        VisitorRow(
                            visitor: visitor,
                            onApprove: { approveVisitor(visitor) },
                            onDeny: { denyVisitor(visitor) }
                        )
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Visitors")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { showAddVisitor = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAddVisitor) {
                AddVisitorView()
            }
            .refreshable { /* reload */ }
        }
    }

    private func approveVisitor(_ visitor: VisitorItem) {}
    private func denyVisitor(_ visitor: VisitorItem) {}
}

struct VisitorRow: View {
    let visitor: VisitorItem
    var onApprove: () -> Void = {}
    var onDeny: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Circle()
                    .fill(Color(.systemGray5))
                    .frame(width: 44, height: 44)
                    .overlay(
                        Image(systemName: "person.fill")
                            .foregroundColor(.secondary)
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text(visitor.name)
                        .font(.subheadline.bold())
                    Text(visitor.purpose)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text("\(visitor.flatNumber), \(visitor.towerBlock)")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }

                Spacer()

                StatusChip(status: visitor.status)
            }

            if !visitor.vehicleNumber.isEmpty {
                Label(visitor.vehicleNumber, systemImage: "car")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if visitor.status == "PENDING" {
                HStack(spacing: 12) {
                    Button(action: onApprove) {
                        Text("Approve")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(Color("Primary"))
                    .controlSize(.small)

                    Button(action: onDeny) {
                        Text("Deny")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.small)
                }
            }
        }
        .padding(.vertical, 4)
    }
}

struct AddVisitorView: View {
    @Environment(\.dismiss) var dismiss
    @State private var name = ""
    @State private var phone = ""
    @State private var purpose = ""
    @State private var vehicleNumber = ""
    @State private var selectedFlat = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Photo") {
                    Button(action: {}) {
                        HStack {
                            Spacer()
                            VStack(spacing: 8) {
                                Image(systemName: "camera")
                                    .font(.largeTitle)
                                Text("Capture Photo")
                                    .font(.caption)
                            }
                            .foregroundColor(.secondary)
                            .padding(.vertical, 40)
                            Spacer()
                        }
                    }
                }

                Section("Visitor Details") {
                    TextField("Full Name", text: $name)
                    TextField("Phone Number", text: $phone)
                        .keyboardType(.phonePad)
                    TextField("Purpose of Visit", text: $purpose)
                    TextField("Destination Flat", text: $selectedFlat)
                    TextField("Vehicle Number (optional)", text: $vehicleNumber)
                        .textInputAutocapitalization(.characters)
                }

                Section {
                    Button(action: {}) {
                        Label("Upload ID Proof", systemImage: "doc.badge.plus")
                    }
                }
            }
            .navigationTitle("Register Visitor")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Submit") {
                        // Submit visitor
                        dismiss()
                    }
                    .disabled(name.isEmpty || phone.isEmpty || purpose.isEmpty)
                }
            }
        }
    }
}

struct VisitorItem: Identifiable {
    let id: String
    let name: String
    let phoneNumber: String
    let purpose: String
    let flatNumber: String
    let towerBlock: String
    let vehicleNumber: String
    let status: String
    let photoUrl: String
    let createdAt: Int64
}

