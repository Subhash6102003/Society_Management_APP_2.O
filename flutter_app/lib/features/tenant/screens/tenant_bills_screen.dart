import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../resident/screens/resident_bills_screen.dart';

/// Tenant bills screen — delegates to the shared resident bills UI.
class TenantBillsScreen extends ConsumerWidget {
  const TenantBillsScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      const ResidentBillsScreen();
}
