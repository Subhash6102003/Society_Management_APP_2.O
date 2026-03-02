import SwiftUI

struct ComplaintsView: View {
    @State private var complaints: [ComplaintItem] = []
    @State private var isLoading = false
    @State private var showCreateComplaint = false
    @State private var selectedFilter = "All"

    let filters = ["All", "Open", "In Progress", "Resolved"]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("Filter", selection: $selectedFilter) {
                    ForEach(filters, id: \.self) { filter in
                        Text(filter).tag(filter)
                    }
                }
                .pickerStyle(.segmented)
                .padding()

                if isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if complaints.isEmpty {
                    Spacer()
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.bubble")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary.opacity(0.3))
                        Text("No complaints found")
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                } else {
                    List(complaints) { complaint in
                        ComplaintRow(complaint: complaint)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Complaints")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { showCreateComplaint = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showCreateComplaint) {
                CreateComplaintView()
            }
            .refreshable { /* reload */ }
        }
    }
}

struct ComplaintRow: View {
    let complaint: ComplaintItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(complaint.category)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color("Primary").opacity(0.1))
                    .foregroundColor(Color("Primary"))
                    .cornerRadius(8)
                Spacer()
                StatusChip(status: complaint.status)
            }
            Text(complaint.title)
                .font(.subheadline.bold())
            Text(complaint.description)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(2)
            HStack {
                Text("\(complaint.flatNumber), \(complaint.towerBlock)")
                    .font(.caption2)
                    .foregroundColor(.secondary)
                Spacer()
                Text(complaint.createdAtFormatted)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}

struct CreateComplaintView: View {
    @Environment(\.dismiss) var dismiss
    @State private var title = ""
    @State private var description = ""
    @State private var category = "Plumbing"
    @State private var priority = "Medium"

    let categories = ["Plumbing", "Electrical", "Cleaning", "Security", "Parking", "Noise", "Other"]
    let priorities = ["Low", "Medium", "High", "Critical"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Complaint Details") {
                    TextField("Subject", text: $title)
                    Picker("Category", selection: $category) {
                        ForEach(categories, id: \.self) { Text($0) }
                    }
                    Picker("Priority", selection: $priority) {
                        ForEach(priorities, id: \.self) { Text($0) }
                    }
                    TextEditor(text: $description)
                        .frame(minHeight: 120)
                        .overlay(
                            Group {
                                if description.isEmpty {
                                    Text("Describe the issue in detail...")
                                        .foregroundColor(.secondary.opacity(0.5))
                                        .padding(.top, 8)
                                        .padding(.leading, 4)
                                }
                            },
                            alignment: .topLeading
                        )
                }

                Section {
                    Button(action: {}) {
                        Label("Add Photos", systemImage: "photo.badge.plus")
                    }
                }
            }
            .navigationTitle("New Complaint")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Submit") {
                        dismiss()
                    }
                    .disabled(title.isEmpty || description.isEmpty)
                }
            }
        }
    }
}

struct ComplaintItem: Identifiable {
    let id: String
    let title: String
    let description: String
    let category: String
    let status: String
    let priority: String
    let flatNumber: String
    let towerBlock: String
    let createdAtFormatted: String
}

