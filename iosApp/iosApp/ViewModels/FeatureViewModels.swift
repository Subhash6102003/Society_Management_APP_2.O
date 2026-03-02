import Foundation
import FirebaseFirestore
import Combine

@MainActor
class DashboardViewModel: ObservableObject {
    @Published var pendingDues: Double = 0
    @Published var recentNotices: [NoticeItem] = []
    @Published var isLoading = false
    @Published var error: String?

    private let db = Firestore.firestore()

    func loadDashboard(userId: String, flatNumber: String, role: String) {
        isLoading = true

        Task {
            do {
                // Load bills
                let billsSnap = try await db.collection("maintenance_bills")
                    .whereField("flatNumber", isEqualTo: flatNumber)
                    .getDocuments()

                let pending = billsSnap.documents
                    .filter { doc in
                        let status = doc.data()["status"] as? String ?? ""
                        return status == "PENDING" || status == "OVERDUE"
                    }
                    .compactMap { doc in doc.data()["totalAmount"] as? Double }
                    .reduce(0, +)

                self.pendingDues = pending

                // Load notices
                let noticesSnap = try await db.collection("notices")
                    .order(by: "createdAt", descending: true)
                    .limit(to: 5)
                    .getDocuments()

                self.recentNotices = noticesSnap.documents.compactMap { doc in
                    let data = doc.data()
                    return NoticeItem(
                        id: doc.documentID,
                        title: data["title"] as? String ?? "",
                        body: data["body"] as? String ?? "",
                        category: data["category"] as? String ?? "GENERAL",
                        priority: data["priority"] as? String ?? "NORMAL",
                        createdByName: data["createdByName"] as? String ?? "",
                        isEmergency: data["isEmergency"] as? Bool ?? false,
                        createdAt: data["createdAt"] as? Int64 ?? 0
                    )
                }

                self.isLoading = false
            } catch {
                self.error = error.localizedDescription
                self.isLoading = false
            }
        }
    }
}

@MainActor
class MaintenanceViewModel: ObservableObject {
    @Published var bills: [BillItem] = []
    @Published var isLoading = false
    @Published var selectedFilter = "All"

    private let db = Firestore.firestore()

    func loadBills(flatNumber: String, isAdmin: Bool) {
        isLoading = true

        Task {
            do {
                var query: Query = db.collection("maintenance_bills")
                    .order(by: "createdAt", descending: true)

                if !isAdmin {
                    query = db.collection("maintenance_bills")
                        .whereField("flatNumber", isEqualTo: flatNumber)
                        .order(by: "createdAt", descending: true)
                }

                let snap = try await query.getDocuments()
                var allBills = snap.documents.compactMap { doc -> BillItem? in
                    let data = doc.data()
                    let timestamp = data["createdAt"] as? Int64 ?? 0
                    let date = Date(timeIntervalSince1970: TimeInterval(timestamp) / 1000)
                    let formatter = DateFormatter()
                    formatter.dateFormat = "MMMM yyyy"

                    let dueTimestamp = data["dueDate"] as? Int64 ?? 0
                    let dueDate = Date(timeIntervalSince1970: TimeInterval(dueTimestamp) / 1000)
                    let dueFmt = DateFormatter()
                    dueFmt.dateFormat = "dd/MM/yyyy"

                    return BillItem(
                        id: doc.documentID,
                        flatNumber: data["flatNumber"] as? String ?? "",
                        towerBlock: data["towerBlock"] as? String ?? "",
                        month: formatter.string(from: date),
                        totalAmount: data["totalAmount"] as? Double ?? 0,
                        status: data["status"] as? String ?? "PENDING",
                        dueDate: dueFmt.string(from: dueDate)
                    )
                }

                if selectedFilter != "All" {
                    allBills = allBills.filter { $0.status == selectedFilter.uppercased() }
                }

                self.bills = allBills
                self.isLoading = false
            } catch {
                self.isLoading = false
            }
        }
    }
}

@MainActor
class VisitorViewModel: ObservableObject {
    @Published var visitors: [VisitorItem] = []
    @Published var isLoading = false

    private let db = Firestore.firestore()

    func loadVisitors(flatNumber: String, isGuard: Bool, isAdmin: Bool) {
        isLoading = true

        Task {
            do {
                var query: Query
                if isAdmin || isGuard {
                    query = db.collection("visitors")
                        .order(by: "createdAt", descending: true)
                        .limit(to: 50)
                } else {
                    query = db.collection("visitors")
                        .whereField("flatNumber", isEqualTo: flatNumber)
                        .order(by: "createdAt", descending: true)
                }

                let snap = try await query.getDocuments()
                self.visitors = snap.documents.compactMap { doc in
                    let data = doc.data()
                    return VisitorItem(
                        id: doc.documentID,
                        name: data["name"] as? String ?? "",
                        phoneNumber: data["phoneNumber"] as? String ?? "",
                        purpose: data["purpose"] as? String ?? "",
                        flatNumber: data["flatNumber"] as? String ?? "",
                        towerBlock: data["towerBlock"] as? String ?? "",
                        vehicleNumber: data["vehicleNumber"] as? String ?? "",
                        status: data["status"] as? String ?? "PENDING",
                        photoUrl: data["photoUrl"] as? String ?? "",
                        createdAt: data["createdAt"] as? Int64 ?? 0
                    )
                }
                self.isLoading = false
            } catch {
                self.isLoading = false
            }
        }
    }

    func approveVisitor(_ visitorId: String, approvedBy: String) {
        Task {
            try await db.collection("visitors").document(visitorId).updateData([
                "status": "APPROVED",
                "approvedBy": approvedBy,
                "approvedAt": Int64(Date().timeIntervalSince1970 * 1000)
            ])
        }
    }

    func denyVisitor(_ visitorId: String, reason: String) {
        Task {
            try await db.collection("visitors").document(visitorId).updateData([
                "status": "DENIED",
                "denialReason": reason,
                "updatedAt": Int64(Date().timeIntervalSince1970 * 1000)
            ])
        }
    }
}

@MainActor
class NoticeViewModel: ObservableObject {
    @Published var notices: [NoticeItem] = []
    @Published var isLoading = false

    private let db = Firestore.firestore()

    func loadNotices(role: String) {
        isLoading = true

        Task {
            do {
                let snap = try await db.collection("notices")
                    .order(by: "createdAt", descending: true)
                    .getDocuments()

                self.notices = snap.documents.compactMap { doc in
                    let data = doc.data()
                    let targetRoles = data["targetRoles"] as? [String] ?? []

                    // Filter by role
                    guard targetRoles.isEmpty || targetRoles.contains(role) else { return nil }

                    return NoticeItem(
                        id: doc.documentID,
                        title: data["title"] as? String ?? "",
                        body: data["body"] as? String ?? "",
                        category: data["category"] as? String ?? "GENERAL",
                        priority: data["priority"] as? String ?? "NORMAL",
                        createdByName: data["createdByName"] as? String ?? "",
                        isEmergency: data["isEmergency"] as? Bool ?? false,
                        createdAt: data["createdAt"] as? Int64 ?? 0
                    )
                }
                self.isLoading = false
            } catch {
                self.isLoading = false
            }
        }
    }
}

@MainActor
class ComplaintViewModel: ObservableObject {
    @Published var complaints: [ComplaintItem] = []
    @Published var isLoading = false

    private let db = Firestore.firestore()

    func loadComplaints(userId: String, isAdmin: Bool) {
        isLoading = true

        Task {
            do {
                var query: Query
                if isAdmin {
                    query = db.collection("complaints")
                        .order(by: "createdAt", descending: true)
                } else {
                    query = db.collection("complaints")
                        .whereField("userId", isEqualTo: userId)
                        .order(by: "createdAt", descending: true)
                }

                let snap = try await query.getDocuments()
                let formatter = DateFormatter()
                formatter.dateFormat = "dd MMM yyyy"

                self.complaints = snap.documents.compactMap { doc in
                    let data = doc.data()
                    let timestamp = data["createdAt"] as? Int64 ?? 0
                    let date = Date(timeIntervalSince1970: TimeInterval(timestamp) / 1000)

                    return ComplaintItem(
                        id: doc.documentID,
                        title: data["title"] as? String ?? "",
                        description: data["description"] as? String ?? "",
                        category: data["category"] as? String ?? "OTHER",
                        status: data["status"] as? String ?? "OPEN",
                        priority: data["priority"] as? String ?? "MEDIUM",
                        flatNumber: data["flatNumber"] as? String ?? "",
                        towerBlock: data["towerBlock"] as? String ?? "",
                        createdAtFormatted: formatter.string(from: date)
                    )
                }
                self.isLoading = false
            } catch {
                self.isLoading = false
            }
        }
    }
}

