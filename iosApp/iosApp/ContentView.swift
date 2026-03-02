import SwiftUI

struct ContentView: View {
    @EnvironmentObject var authViewModel: AuthViewModel

    var body: some View {
        Group {
            if authViewModel.isLoggedIn {
                MainTabView()
            } else {
                LoginView()
            }
        }
        .preferredColorScheme(nil) // Follow system
    }
}

struct MainTabView: View {
    @EnvironmentObject var authViewModel: AuthViewModel

    var body: some View {
        TabView {
            DashboardView()
                .tabItem {
                    Label("Dashboard", systemImage: "square.grid.2x2")
                }

            MaintenanceView()
                .tabItem {
                    Label("Maintenance", systemImage: "creditcard")
                }

            VisitorsView()
                .tabItem {
                    Label("Visitors", systemImage: "person.2")
                }

            NoticesView()
                .tabItem {
                    Label("Notices", systemImage: "bell")
                }

            ProfileView()
                .tabItem {
                    Label("Profile", systemImage: "person.circle")
                }
        }
        .tint(Color("Primary"))
    }
}

