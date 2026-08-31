import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/formatters.dart';
import '../../../providers/notification_provider.dart';
import '../../widgets/empty_state_view.dart';
import '../challenge/challenge_detail_screen.dart';

class NotificationsScreen extends StatefulWidget {
  const NotificationsScreen({super.key});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<NotificationProvider>().fetchNotifications();
    });
  }

  @override
  Widget build(BuildContext context) {
    final notifProvider = context.watch<NotificationProvider>();
    final notifications = notifProvider.notifications;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Civic Alerts & Updates'),
        actions: [
          if (notifications.isNotEmpty)
            TextButton(
              onPressed: () => notifProvider.markAllAsRead(),
              child: const Text('Mark All Read', style: TextStyle(fontSize: 12)),
            ),
        ],
      ),
      body: notifProvider.isLoading
          ? const Center(child: CircularProgressIndicator())
          : notifications.isEmpty
              ? const EmptyStateView(
                  icon: Icons.notifications_none_rounded,
                  title: 'All Caught Up!',
                  message: 'You have no new alerts. Real-time updates regarding your reported societal challenges will appear here.',
                )
              : RefreshIndicator(
                  onRefresh: () => notifProvider.fetchNotifications(refresh: true),
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16.0),
                    itemCount: notifications.length,
                    itemBuilder: (context, idx) {
                      final notif = notifications[idx];
                      return Card(
                        margin: const EdgeInsets.only(bottom: 10),
                        color: notif.isRead ? AppColors.surface : AppColors.primary.withValues(alpha: 0.04),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                          side: BorderSide(
                            color: notif.isRead ? AppColors.divider : AppColors.primary.withValues(alpha: 0.3),
                            width: 1,
                          ),
                        ),
                        child: ListTile(
                          contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                          leading: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: _getTypeColor(notif.notificationType).withValues(alpha: 0.12),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              _getTypeIcon(notif.notificationType),
                              color: _getTypeColor(notif.notificationType),
                              size: 20,
                            ),
                          ),
                          title: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Text(
                                  notif.title,
                                  style: TextStyle(
                                    fontSize: 14,
                                    fontWeight: notif.isRead ? FontWeight.w600 : FontWeight.bold,
                                    color: AppColors.textPrimary,
                                  ),
                                ),
                              ),
                              if (notif.createdAt != null)
                                Text(
                                  Formatters.formatTimeAgo(notif.createdAt),
                                  style: const TextStyle(fontSize: 11, color: AppColors.textMuted),
                                ),
                            ],
                          ),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const SizedBox(height: 4),
                              Text(
                                notif.body,
                                style: const TextStyle(fontSize: 13, color: AppColors.textSecondary, height: 1.3),
                              ),
                              if (notif.referenceId != null && notif.referenceId!.isNotEmpty) ...[
                                const SizedBox(height: 6),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: AppColors.surfaceVariant,
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    'REF: ${notif.referenceId}',
                                    style: const TextStyle(
                                      fontSize: 11,
                                      fontWeight: FontWeight.bold,
                                      color: AppColors.primary,
                                    ),
                                  ),
                                ),
                              ],
                            ],
                          ),
                          onTap: () {
                            notifProvider.markAsRead(notif.id);
                            if (notif.referenceId != null && notif.referenceId!.isNotEmpty) {
                              Navigator.of(context).push(
                                MaterialPageRoute(
                                  builder: (_) => ChallengeDetailScreen(challengeId: notif.referenceId!),
                                ),
                              );
                            }
                          },
                        ),
                      );
                    },
                  ),
                ),
    );
  }

  Color _getTypeColor(String type) {
    switch (type) {
      case 'CHALLENGE_RESOLVED':
        return AppColors.emerald;
      case 'INNOVATION_REQUIRED':
        return AppColors.amethyst;
      case 'CHALLENGE_TRIAGED':
      case 'CHALLENGE_ROUTED':
        return AppColors.saffron;
      default:
        return AppColors.sapphire;
    }
  }

  IconData _getTypeIcon(String type) {
    switch (type) {
      case 'CHALLENGE_RESOLVED':
        return Icons.task_alt_rounded;
      case 'INNOVATION_REQUIRED':
        return Icons.lightbulb_outline_rounded;
      case 'CHALLENGE_TRIAGED':
      case 'CHALLENGE_ROUTED':
        return Icons.alt_route_rounded;
      default:
        return Icons.notifications_active_outlined;
    }
  }
}
