import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../providers/data_providers.dart';
import '../../../widgets/common_widgets.dart';

class ResidentNoticesScreen extends ConsumerWidget {
  const ResidentNoticesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final noticesAsync = ref.watch(noticesProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Notices')),
      body: noticesAsync.when(
        data: (notices) => notices.isEmpty
            ? const EmptyState(icon: Icons.campaign_outlined, title: 'No Notices', subtitle: 'Check back later for updates')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: notices.length,
                itemBuilder: (ctx, i) {
                  final n = notices[i];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: AppColors.primary.withOpacity(0.12),
                        child: const Icon(Icons.campaign, color: AppColors.primary),
                      ),
                      title: Text(n.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(n.body),
                          const SizedBox(height: 4),
                          Row(children: [
                            StatusBadge(label: n.category.name, color: AppColors.info),
                            const SizedBox(width: 8),
                            if (n.createdByName != null)
                              Text('By ${n.createdByName}', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                          ]),
                        ],
                      ),
                      isThreeLine: true,
                    ),
                  );
                },
              ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorState(message: e.toString()),
      ),
    );
  }
}
