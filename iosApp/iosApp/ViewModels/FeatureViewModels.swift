import Foundation
import Combine

// MARK: - Data Models

struct NoticeItem: Identifiable {
    var id: String
    var title: String
    var body: String
    var category: String
    var priority: String
    var createdByName: String
    var isEmergency: Bool
    var createdAt: Int64
}

struct BillItem: Identifiable {
    var id: String
    var flatNumber: String
    var towerBlock: String
    var month: String
    var totalAmount: Double
    var status: String
    var dueDate: String
}

struct VisitorItem: Identifiable {
    var id: String
    var name: String
    var phoneNumber: String
    var purpose: String
    var flatNumber: String
    var towerBlock: String
    var vehicleNumber: String
    var status: String
    var photoUrl: String
    var createdAt: Int64
}

struct ComplaintItem: Identifiable {
    var id: String
    var title: String
    var description: String
    var category: String
    var status: String
    var priority: String
    var flatNumber: String
    var towerBlock: String
    var createdAtFormatted: String
}

// MARK: - Supabase REST Helper

private struct SupabaseConfig {
    static let url = "https://ggoqcgfjehfuupprchdl.supabase.co/rest/v1"
    static let anonKey = "your_supabase_anon_key_here"

    static func request(_ table: String, query: String = "") -> URLRequest {
        let urlStr = "\(url)/\(table)\(query.isEmpty ? "" : "?\(query)")"
        var req = URLRequest(url: URL(string: urlStr)!)
        req.setValue(anonKey, forHTTPHeaderField: "apikey")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        return req
    }

    static func patchRequest(_ table: String, query: String, body: [String: Any]) -> URLRequest {
        var req = request(table, query: query)
        req.httpMethod = "PATCH"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.httpBody = try? JSONSerialization.data(withJSONObject: body)
        return req
    }
}

// MARK: - DashboardViewModel

@MainActor
class DashboardViewModel: ObservableObject {
    @Published var pendingDues: Double = 0
    @Published var recentNotices: [NoticeItem] = []
    @Published var isLoading = false
    @Published var error: String?

    func loadDashboard(userId: String, flatNumber: String, role: String) {
        isLoading = true
        Task {
            async let bills = fetchBills(flatNumber: flatNumber)
            async let notices = fetchNotices(limit: 5)
            let (b, n) = await (bills, notices)
            pendingDues = b.filter { $0.status == "PENDING" || $0.status == "OVERDUE" }
                           .reduce(0.0) { $0 + $1.totalAmount }
            recentNotices = n
            isLoading = false
        }
    }

    private func fetchBills(flatNumber: String) async -> [BillItem] {
        let req = SupabaseConfig.request("maintenance_bills",
            query: "flat_number=eq.\(flatNumber.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? flatNumber)&order=created_at.desc")
        guard let (data, _) = try? await URLSession.shared.data(for: req),
              let rows = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else { return [] }
        return rows.map { parseBill(id: $0["id"] as? String ?? "", row: $0) }
    }

    private func fetchNotices(limit: Int) async -> [NoticeItem] {
        let req = SupabaseConfig.request("notices", query: "order=created_at.desc&limit=\(limit)")
        guard let (data, _) = try? await URLSession.shared.data(for: req),
              let rows = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else { return [] }
        return rows.map { parseNotice(id: $0["id"] as? String ?? "", row: $0) }
    }
}

// MARK: - MaintenanceViewModel

@MainActor
class MaintenanceViewModel: ObservableObject {
    @Published var bills: [BillItem] = []
    @Published var isLoading = false
    @Published var selectedFilter = "All"

    func loadBills(flatNumber: String, isAdmin: Bool) {
        isLoading = true
        Task {
            let query = isAdmin
                ? "order=created_at.desc"
                : "flat_number=eq.\(flatNumber.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? flatNumber)&order=created_at.desc"
            let req = SupabaseConfig.request("maintenance_bills", query: query)
            guard let (data, _) = try? await URLSession.shared.data(for: req),
                  let rows = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else {
                isLoading = false; return
            }
            var result = rows.map { parseBill(id: $0["id"] as? String ?? "", row: $0) }
            if selectedFilter != "All" {
                result = result.filter { $0.status == selectedFilter.uppercased() }
            }
            bills = result
            isLoading = false
        }
    }
}

// MARK: - VisitorViewModel

@MainActor
class VisitorViewModel: ObservableObject {
    @Published var visitors: [VisitorItem] = []
    @Published var isLoading = false

    func loadVisitors(flatNumber: String, isGuard: Bool, isAdmin: Bool) {
        isLoading = true
        Task {
            let query = (isAdmin || isGuard)
                ? "order=created_at.desc&limit=50"
                : "flat_number=eq.\(flatNumber.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? flatNumber)&order=created_at.desc"
            let req = SupabaseConfig.request("visitors", query: query)
            guard let (data, _) = try? await URLSession.shared.data(for: req),
                  let rows = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else {
                isLoading = false; return
            }
            visitors = rows.map { row in
                VisitorItem(
                    id: row["id"] as? String ?? "",
                    name: row["visitor_name"] as? String ?? "",
                    phoneNumber: row["visitor_phone"] as? String ?? "",
                    purpose: row["purpose"] as? String ?? "",
                    flatNumber: row["flat_number"] as? String ?? "",
                    towerBlock: row["tower_block"] as? String ?? "",
                    vehicleNumber: row["vehicle_number"] as? String ?? "",
                    status: row["status"] as? String ?? "PENDING",
                    photoUrl: row["photo_url"] as? String ?? "",
                    createdAt: row["created_at"] as? Int64 ?? 0
                )
            }
            isLoading = false
        }
    }

    func approveVisitor(_ visitorId: String, approvedBy: String) {
        Task {
            let req = SupabaseConfig.patchRequest("visitors", query: "id=eq.\(visitorId)",
                body: ["status": "APPROVED", "updated_at": Int64(Date().timeIntervalSince1970 * 1000)])
            _ = try? await URLSession.shared.data(for: req)
            if let idx = visitors.firstIndex(where: { $0.id == visitorId }) {
                visitors[idx].status = "APPROVED"
            }
        }
    }

    func denyVisitor(_ visitorId: String, reason: String) {
        Task {
            let req = SupabaseConfig.patchRequest("visitors", query: "id=eq.\(visitorId)",
                body: ["status": "DENIED", "updated_at": Int64(Date().timeIntervalSince1970 * 1000)])
            _ = try? await URLSession.shared.data(for: req)
            if let idx = visitors.firstIndex(where: { $0.id == visitorId }) {
                visitors[idx].status = "DENIED"
            }
        }
    }
}

// MARK: - NoticeViewModel

@MainActor
class NoticeViewModel: ObservableObject {
    @Published var notices: [NoticeItem] = []
    @Published var isLoading = false

    func loadNotices(role: String) {
        isLoading = true
        Task {
            let req = SupabaseConfig.request("notices", query: "order=created_at.desc")
            guard let (data, _) = try? await URLSession.shared.data(for: req),
                  let rows = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else {
                isLoading = false; return
            }
            notices = rows.compactMap { row in
                let targetRoles = row["target_roles"] as? [String] ?? []
                guard targetRoles.isEmpty || targetRoles.contains(role) else { return nil }
                return parseNotice(id: row["id"] as? String ?? "", row: row)
            }
            isLoading = false
        }
    }
}

// MARK: - ComplaintViewModel

@MainActor
class ComplaintViewModel: ObservableObject {
    @Published var complaints: [ComplaintItem] = []
    @Published var isLoading = false

    func loadComplaints(userId: String, isAdmin: Bool) {
        isLoading = true
        Task {
            let query = isAdmin
                ? "order=created_at.desc"
                : "user_id=eq.\(userId)&order=created_at.desc"
            let req = SupabaseConfig.request("complaints", query: query)
            guard let (data, _) = try? await URLSession.shared.data(for: req),
                  let rows = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else {
                isLoading = false; return
            }
            let formatter = DateFormatter()
            formatter.dateFormat = "dd MMM yyyy"
            complaints = rows.map { row in
                let ts = row["created_at"] as? Int64 ?? 0
                let date = Date(timeIntervalSince1970: TimeInterval(ts) / 1000)
                return ComplaintItem(
                    id: row["id"] as? String ?? "",
                    title: row["title"] as? String ?? "",
                    description: row["description"] as? String ?? "",
                    category: row["category"] as? String ?? "OTHER",
                    status: row["status"] as? String ?? "PENDING",
                    priority: "MEDIUM",
                    flatNumber: row["flat_number"] as? String ?? "",
                    towerBlock: row["tower_block"] as? String ?? "",
                    createdAtFormatted: formatter.string(from: date)
                )
            }
            isLoading = false
        }
    }
}

// MARK: - Parse Helpers

private func parseBill(id: String, row: [String: Any]) -> BillItem {
    let ts = row["created_at"] as? Int64 ?? 0
    let date = Date(timeIntervalSince1970: TimeInterval(ts) / 1000)
    let monthFmt = DateFormatter(); monthFmt.dateFormat = "MMMM yyyy"
    let dueTs = row["due_date"] as? Int64 ?? 0
    let dueDate = Date(timeIntervalSince1970: TimeInterval(dueTs) / 1000)
    let dueFmt = DateFormatter(); dueFmt.dateFormat = "dd/MM/yyyy"
    return BillItem(
        id: id,
        flatNumber: row["flat_number"] as? String ?? "",
        towerBlock: row["tower_block"] as? String ?? "",
        month: monthFmt.string(from: date),
        totalAmount: row["total_amount"] as? Double ?? 0,
        status: row["status"] as? String ?? "PENDING",
        dueDate: dueFmt.string(from: dueDate)
    )
}

private func parseNotice(id: String, row: [String: Any]) -> NoticeItem {
    NoticeItem(
        id: id,
        title: row["title"] as? String ?? "",
        body: row["content"] as? String ?? "",
        category: row["category"] as? String ?? "GENERAL",
        priority: "NORMAL",
        createdByName: row["author_name"] as? String ?? "",
        isEmergency: false,
        createdAt: row["created_at"] as? Int64 ?? 0
    )
}
