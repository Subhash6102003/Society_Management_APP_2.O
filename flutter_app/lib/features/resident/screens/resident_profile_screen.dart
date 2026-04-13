import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../providers/auth_provider.dart';
import '../../../widgets/common_widgets.dart';

class ResidentProfileScreen extends ConsumerWidget {
  const ResidentProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(currentUserProvider);
    if (user == null) return const Center(child: CircularProgressIndicator());

    return Scaffold(
      appBar: AppBar(
        title: const Text('My Profile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => ref.read(authProvider.notifier).signOut(),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Center(
            child: UserAvatar(photoUrl: user.photoUrl, name: user.name, radius: 44),
          ),
          const SizedBox(height: 16),
          Center(
            child: Column(
              children: [
                Text(user.name, style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                StatusBadge(label: user.role.displayName, color: user.role.color),
              ],
            ),
          ),
          const SizedBox(height: 28),
          const SectionHeader(title: 'Profile Details'),
          const SizedBox(height: 12),
          _InfoRow(icon: Icons.email_outlined, label: 'Email', value: user.email),
          if (user.phoneNumber != null)
            _InfoRow(icon: Icons.phone_outlined, label: 'Phone', value: user.phoneNumber!),
          if (user.flatNumber != null)
            _InfoRow(icon: Icons.home_outlined, label: 'Flat', value: user.flatNumber!),
          if (user.tower != null)
            _InfoRow(icon: Icons.apartment_outlined, label: 'Tower', value: user.tower!),
        ],
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  const _InfoRow({required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon, color: AppColors.primary),
      title: Text(label, style: const TextStyle(color: Colors.grey, fontSize: 12)),
      subtitle: Text(value, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15)),
    );
  }
}
