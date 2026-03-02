import SwiftUI

struct MaintenanceView: View {
    @State private var selectedFilter = "All"
    @State private var bills: [BillItem] = []
    @State private var isLoading = false

    let filters = ["All", "Pending", "Overdue", "Paid"]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Filter tabs
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(filters, id: \.self) { filter in
                            Button(filter) {
                                selectedFilter = filter
                            }
                            .buttonStyle(.bordered)
                            .tint(selectedFilter == filter ? Color("Primary") : .gray)
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }

                if isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if bills.isEmpty {
                    Spacer()
                    VStack(spacing: 16) {
                        Image(systemName: "doc.text")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary.opacity(0.3))
                        Text("No bills found")
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                } else {
                    List(bills) { bill in
                        NavigationLink(destination: BillDetailView(billId: bill.id)) {
                            BillRow(bill: bill)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Maintenance")
            .refreshable { /* reload */ }
        }
    }
}

struct BillRow: View {
    let bill: BillItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(bill.month)
                    .font(.headline)
                Spacer()
                StatusChip(status: bill.status)
            }
            Text("\(bill.flatNumber), \(bill.towerBlock)")
                .font(.caption)
                .foregroundColor(.secondary)
            HStack {
                Text("₹\(String(format: "%.0f", bill.totalAmount))")
                    .font(.title2.bold())
                    .foregroundColor(Color("Primary"))
                Spacer()
                if bill.status != "PAID" {
                    Button("Pay Now") {}
                        .buttonStyle(.borderedProminent)
                        .tint(Color("Primary"))
                        .controlSize(.small)
                }
            }
            Text("Due: \(bill.dueDate)")
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 4)
    }
}

struct BillDetailView: View {
    let billId: String

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Bill Details")
                    .font(.headline)
                // Bill detail content would be loaded here
            }
            .padding()
        }
        .navigationTitle("Bill Details")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct StatusChip: View {
    let status: String

    var color: Color {
        switch status {
        case "PAID": return .green
        case "OVERDUE": return .red
        case "PENDING": return .orange
        default: return .gray
        }
    }

    var body: some View {
        Text(status)
            .font(.caption2.bold())
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(color.opacity(0.15))
            .foregroundColor(color)
            .cornerRadius(8)
    }
}

struct BillItem: Identifiable {
    let id: String
    let flatNumber: String
    let towerBlock: String
    let month: String
    let totalAmount: Double
    let status: String
    let dueDate: String
}

