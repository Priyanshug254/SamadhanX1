import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class NotificationItem {
  final String id;
  final String title;
  final String body;
  final String notificationType;
  final String? referenceId;
  final String? referenceType;
  bool isRead;
  final DateTime? createdAt;

  NotificationItem({
    required this.id,
    required this.title,
    required this.body,
    required this.notificationType,
    this.referenceId,
    this.referenceType,
    required this.isRead,
    this.createdAt,
  });

  factory NotificationItem.fromJson(Map<String, dynamic> json) {
    return NotificationItem(
      id: json['id']?.toString() ?? '',
      title: json['title']?.toString() ?? 'Civic Alert',
      body: json['message']?.toString() ?? json['body']?.toString() ?? '',
      notificationType: json['type']?.toString() ?? json['notificationType']?.toString() ?? 'GENERAL',
      referenceId: json['reference_id']?.toString() ?? json['referenceId']?.toString(),
      referenceType: json['reference_type']?.toString() ?? json['referenceType']?.toString(),
      isRead: json['is_read'] ?? json['isRead'] ?? json['read'] ?? false,
      createdAt: json['created_at'] != null
          ? DateTime.tryParse(json['created_at'].toString())
          : (json['createdAt'] != null ? DateTime.tryParse(json['createdAt'].toString()) : null),
    );
  }
}

class NotificationProvider extends ChangeNotifier {
  List<NotificationItem> _notifications = [];
  int _unreadCount = 0;
  bool _isLoading = false;
  String? _errorMessage;

  NotificationProvider();

  List<NotificationItem> get notifications => _notifications;
  int get unreadCount => _unreadCount;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> fetchNotifications({bool refresh = false}) async {
    final user = Supabase.instance.client.auth.currentUser;
    if (user == null) return;

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final res = await Supabase.instance.client
          .from('notifications')
          .select('*')
          .eq('user_id', user.id)
          .order('created_at', ascending: false)
          .limit(30);

      _notifications = (res as List)
          .map((item) => NotificationItem.fromJson(item as Map<String, dynamic>))
          .toList();
      _unreadCount = _notifications.where((n) => !n.isRead).length;
      _isLoading = false;
    } catch (e) {
      _isLoading = false;
      _errorMessage = e.toString().replaceAll('Exception: ', '');
    }

    notifyListeners();
  }

  Future<void> markAsRead(String id) async {
    try {
      final notif = _notifications.firstWhere((n) => n.id == id);
      if (!notif.isRead) {
        notif.isRead = true;
        if (_unreadCount > 0) _unreadCount--;
        notifyListeners();

        await Supabase.instance.client
            .from('notifications')
            .update({'is_read': true})
            .eq('id', id);
      }
    } catch (e) {
      debugPrint('Failed to mark notification read: $e');
    }
  }

  Future<void> markAllAsRead() async {
    final user = Supabase.instance.client.auth.currentUser;
    if (user == null) return;

    try {
      for (var n in _notifications) {
        n.isRead = true;
      }
      _unreadCount = 0;
      notifyListeners();

      await Supabase.instance.client
          .from('notifications')
          .update({'is_read': true})
          .eq('user_id', user.id);
    } catch (e) {
      debugPrint('Failed to mark all notifications read: $e');
    }
  }
}
