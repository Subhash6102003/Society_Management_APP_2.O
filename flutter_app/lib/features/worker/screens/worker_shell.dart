import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../routes/app_router.dart';

class WorkerShell extends ConsumerStatefulWidget {
  final Widget child;
  const WorkerShell({super.key, required this.child});

  @override
  ConsumerState<WorkerShell> createState() => _WorkerShellState();
}

class _WorkerShellState extends ConsumerState<WorkerShell> {
  int _selectedIndex = 0;

  final List<_NavItem> _navItems = const [
    _NavItem(icon: Icons.dashboard_outlined, activeIcon: Icons.dashboard, label: 'Dashboard', route: AppRoutes.workerDashboard),
    _NavItem(icon: Icons.assignment_outlined, activeIcon: Icons.assignment, label: 'Bookings', route: AppRoutes.workerBookings),
    _NavItem(icon: Icons.attach_money, activeIcon: Icons.attach_money, label: 'Earnings', route: AppRoutes.workerEarnings),
    _NavItem(icon: Icons.store_outlined, activeIcon: Icons.store, label: 'Shop', route: AppRoutes.workerShop),
    _NavItem(icon: Icons.person_outline, activeIcon: Icons.person, label: 'Profile', route: AppRoutes.workerProfile),
  ];

  void _onItemTapped(int index) {
    setState(() => _selectedIndex = index);
    context.go(_navItems[index].route);
  }

  @override
  Widget build(BuildContext context) {
    final location = GoRouterState.of(context).uri.toString();
    for (int i = 0; i < _navItems.length; i++) {
      if (location.startsWith(_navItems[i].route)) {
        _selectedIndex = i;
        break;
      }
    }

    return Scaffold(
      body: widget.child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: _onItemTapped,
        destinations: _navItems.map((item) => NavigationDestination(
          icon: Icon(item.icon),
          selectedIcon: Icon(item.activeIcon),
          label: item.label,
        )).toList(),
      ),
    );
  }
}

class _NavItem {
  final IconData icon;
  final IconData activeIcon;
  final String label;
  final String route;
  const _NavItem({required this.icon, required this.activeIcon, required this.label, required this.route});
}
