import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../providers/auth_provider.dart';
import '../../../widgets/common_widgets.dart';

class WorkerProfileScreen extends ConsumerWidget {
  const WorkerProfileScreen({super.key});

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
          Center(child: UserAvatar(photoUrl: user.photoUrl, name: user.name, radius: 44)),
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
          const SectionHeader(title: 'Details'),
          const SizedBox(height: 12),
          ListTile(
            leading: const Icon(Icons.email_outlined, color: AppColors.primary),
            title: const Text('Email', style: TextStyle(color: Colors.grey, fontSize: 12)),
            subtitle: Text(user.email, style: const TextStyle(fontWeight: FontWeight.w600)),
          ),
          if (user.phoneNumber != null)
            ListTile(
              leading: const Icon(Icons.phone_outlined, color: AppColors.primary),
              title: const Text('Phone', style: TextStyle(color: Colors.grey, fontSize: 12)),
              subtitle: Text(user.phoneNumber!, style: const TextStyle(fontWeight: FontWeight.w600)),
            ),
        ],
      ),
    );
  }
}
