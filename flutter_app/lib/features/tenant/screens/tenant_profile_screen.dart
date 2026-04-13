import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../resident/screens/resident_profile_screen.dart';

class TenantProfileScreen extends ConsumerWidget {
  const TenantProfileScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      const ResidentProfileScreen();
}
