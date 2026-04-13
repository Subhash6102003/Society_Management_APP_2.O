import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../resident/screens/resident_notices_screen.dart';

class TenantNoticesScreen extends ConsumerWidget {
  const TenantNoticesScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      const ResidentNoticesScreen();
}
