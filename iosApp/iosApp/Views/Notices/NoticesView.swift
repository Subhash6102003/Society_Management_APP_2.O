import SwiftUI

struct NoticesView: View {
    @State private var notices: [NoticeItem] = []
    @State private var isLoading = false
    @State private var showCreateNotice = false
    @State private var selectedCategory = "All"

    let categories = ["All", "General", "Maintenance", "Safety", "Events"]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories, id: \.self) { category in
                            Button(category) {
                                selectedCategory = category
                            }
                            .buttonStyle(.bordered)
                            .tint(selectedCategory == category ? Color("Primary") : .gray)
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }

                if isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if notices.isEmpty {
                    Spacer()
                    VStack(spacing: 16) {
                        Image(systemName: "megaphone")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary.opacity(0.3))
                        Text("No notices found")
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                } else {
                    List(notices) { notice in
                        NavigationLink(destination: NoticeDetailView(notice: notice)) {
                            NoticeCard(notice: notice)
                        }
                        .listRowSeparator(.hidden)
                        .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Notices")
            .toolbar {
                // Show create button for admins only
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { showCreateNotice = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showCreateNotice) {
                CreateNoticeView()
            }
            .refreshable { /* reload */ }
        }
    }
}

struct NoticeDetailView: View {
    let notice: NoticeItem

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    if notice.isEmergency {
                        Label("Emergency", systemImage: "exclamationmark.triangle.fill")
                            .font(.caption.bold())
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(.red)
                            .cornerRadius(8)
                    }

                    Text(notice.category)
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color("Primary").opacity(0.1))
                        .foregroundColor(Color("Primary"))
                        .cornerRadius(8)

                    Spacer()
                }

                Text(notice.title)
                    .font(.title2.bold())

                Text("By \(notice.createdByName)")
                    .font(.caption)
                    .foregroundColor(.secondary)

                Divider()

                Text(notice.body)
                    .font(.body)
                    .lineSpacing(6)
            }
            .padding()
        }
        .navigationTitle("Notice")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct CreateNoticeView: View {
    @Environment(\.dismiss) var dismiss
    @State private var title = ""
    @State private var noticeBody = ""
    @State private var category = "General"
    @State private var priority = "Normal"
    @State private var isEmergency = false

    let categories = ["General", "Maintenance", "Safety", "Events", "Community"]
    let priorities = ["Low", "Normal", "High", "Urgent"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Notice Content") {
                    TextField("Title", text: $title)
                    TextEditor(text: $noticeBody)
                        .frame(minHeight: 120)
                }

                Section("Settings") {
                    Picker("Category", selection: $category) {
                        ForEach(categories, id: \.self) { Text($0) }
                    }
                    Picker("Priority", selection: $priority) {
                        ForEach(priorities, id: \.self) { Text($0) }
                    }
                    Toggle("Emergency Broadcast", isOn: $isEmergency)
                        .tint(Color("Primary"))
                }

                Section {
                    Button(action: {}) {
                        Label("Add Image", systemImage: "photo.badge.plus")
                    }
                }
            }
            .navigationTitle("Create Notice")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Post") {
                        // Create notice
                        dismiss()
                    }
                    .disabled(title.isEmpty || noticeBody.isEmpty)
                }
            }
        }
    }
}


