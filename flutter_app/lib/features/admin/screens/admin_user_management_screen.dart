import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/user_model.dart';
import '../../../providers/user_provider.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class AdminUserManagementScreen extends ConsumerStatefulWidget {
  const AdminUserManagementScreen({super.key});

  @override
  ConsumerState<AdminUserManagementScreen> createState() => _AdminUserManagementScreenState();
}

class _AdminUserManagementScreenState extends ConsumerState<AdminUserManagementScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final pendingUsers = ref.watch(pendingUsersStreamProvider);
    final allUsers = ref.watch(allUsersProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('User Management'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Pending'),
            Tab(text: 'Approved'),
            Tab(text: 'All Users'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          // Pending Tab — Realtime stream auto-updates on new signups
          pendingUsers.when(
            data: (users) => users.isEmpty
                ? const SingleChildScrollView(
                    physics: AlwaysScrollableScrollPhysics(),
                    child: SizedBox(
                      height: 400,
                      child: EmptyState(
                        icon: Icons.check_circle_outline,
                        title: 'No Pending Requests',
                        subtitle: 'All registrations have been processed',
                      ),
                    ),
                  )
                : ListView.builder(
                    physics: const AlwaysScrollableScrollPhysics(),
                    itemCount: users.length,
                    padding: const EdgeInsets.all(12),
                    itemBuilder: (ctx, i) => _UserCard(
                      user: users[i],
                      showActions: true,
                      onActionDone: () {
                        ref.invalidate(allUsersProvider);
                      },
                    ),
                  ),
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, _) => ErrorState(message: e.toString()),
          ),
          // Approved Tab
          allUsers.when(
            data: (users) {
              final approved = users.where((u) => u.approvalStatus == ApprovalStatus.approved && !u.isBlocked).toList();
              return approved.isEmpty
                  ? const EmptyState(icon: Icons.people_outline, title: 'No Approved Users', subtitle: '')
                  : ListView.builder(
                      itemCount: approved.length,
                      padding: const EdgeInsets.all(12),
                      itemBuilder: (ctx, i) => _UserCard(user: approved[i], showActions: false, onActionDone: () {
                        ref.invalidate(allUsersProvider);
                      }),
                    );
            },
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, _) => ErrorState(message: e.toString()),
          ),
          // All Users Tab
          allUsers.when(
            data: (users) => ListView.builder(
              itemCount: users.length,
              padding: const EdgeInsets.all(12),
              itemBuilder: (ctx, i) => _UserCard(user: users[i], showActions: false, showBlockToggle: true, onActionDone: () {
                ref.invalidate(allUsersProvider);
              }),
            ),
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, _) => ErrorState(message: e.toString()),
          ),
        ],
      ),
    );
  }
}

class _UserCard extends StatelessWidget {
  final UserModel user;
  final bool showActions;
  final bool showBlockToggle;
  final VoidCallback onActionDone;

  const _UserCard({
    required this.user,
    required this.showActions,
    this.showBlockToggle = false,
    required this.onActionDone,
  });

  @override
  Widget build(BuildContext context) {
    final profileIncomplete = !user.isProfileComplete;

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Profile incomplete banner
          if (profileIncomplete && showActions)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: AppColors.warning.withOpacity(0.12),
                borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
              ),
              child: Row(
                children: [
                  Icon(Icons.info_outline, size: 14, color: AppColors.warning),
                  const SizedBox(width: 6),
                  Text(
                    'Profile not yet completed by user',
                    style: TextStyle(fontSize: 12, color: AppColors.warning),
                  ),
                ],
              ),
            ),
          ListTile(
            leading: UserAvatar(
              photoUrl: user.photoUrl,
              name: user.name.isNotEmpty ? user.name : user.email,
              radius: 22,
            ),
            title: Text(
              user.name.isNotEmpty ? user.name : '(Name not set)',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(user.email),
                if (user.phoneNumber.isNotEmpty) Text(user.phoneNumber),
                if (user.flatNumber.isNotEmpty)
                  Text('Flat: ${user.flatNumber}${user.tower != null ? ' · Tower: ${user.tower}' : ''}'),
              ],
            ),
            trailing: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                StatusBadge(label: user.role.displayName, color: user.role.color),
                const SizedBox(height: 4),
                if (user.isBlocked)
                  const StatusBadge(label: 'Blocked', color: AppColors.error),
              ],
            ),
            isThreeLine: true,
          ),
          if (showActions || showBlockToggle)
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  if (showActions) ...[
                    OutlinedButton(
                      onPressed: () => _updateStatus(context, ApprovalStatus.rejected),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: AppColors.error,
                        side: const BorderSide(color: AppColors.error),
                        minimumSize: const Size(80, 36),
                      ),
                      child: const Text('Reject'),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton(
                      onPressed: profileIncomplete
                          ? null
                          : () => _updateStatus(context, ApprovalStatus.approved),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.success,
                        minimumSize: const Size(90, 36),
                      ),
                      child: const Text('Approve'),
                    ),
                  ],
                  if (showBlockToggle) ...[
                    ElevatedButton(
                      onPressed: () => _toggleBlock(context),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: user.isBlocked ? AppColors.success : AppColors.error,
                        minimumSize: const Size(90, 36),
                      ),
                      child: Text(user.isBlocked ? 'Unblock' : 'Block'),
                    ),
                  ],
                ],
              ),
            ),
        ],
      ),
    );
  }

  Future<void> _updateStatus(BuildContext context, ApprovalStatus status) async {
    try {
      await DatabaseService.instance.updateUserStatus(userId: user.id, status: status);
      onActionDone();
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e'), backgroundColor: AppColors.error));
      }
    }
  }

  Future<void> _toggleBlock(BuildContext context) async {
    try {
      await DatabaseService.instance.updateUserStatus(userId: user.id, isBlocked: !user.isBlocked);
      onActionDone();
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e'), backgroundColor: AppColors.error));
      }
    }
  }
}
