import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../resident/screens/resident_complaints_screen.dart';

class TenantComplaintsScreen extends ConsumerWidget {
  const TenantComplaintsScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      const ResidentComplaintsScreen();
}
