import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../routes/app_router.dart';

class MaidShell extends ConsumerStatefulWidget {
  final Widget child;
  const MaidShell({super.key, required this.child});

  @override
  ConsumerState<MaidShell> createState() => _MaidShellState();
}

class _MaidShellState extends ConsumerState<MaidShell> {
  int _selectedIndex = 0;

  final List<_NavItem> _navItems = const [
    _NavItem(icon: Icons.dashboard_outlined, activeIcon: Icons.dashboard, label: 'Dashboard', route: AppRoutes.maidDashboard),
    _NavItem(icon: Icons.fact_check_outlined, activeIcon: Icons.fact_check, label: 'Attendance', route: AppRoutes.maidAttendance),
    _NavItem(icon: Icons.store_outlined, activeIcon: Icons.store, label: 'Shop', route: AppRoutes.maidShop),
    _NavItem(icon: Icons.person_outline, activeIcon: Icons.person, label: 'Profile', route: AppRoutes.maidProfile),
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
