import 'dart:async';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import '../network/api_client.dart';
import '../storage/secure_storage_service.dart';

@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  // Ensure Firebase is initialized for background processing if needed
  try {
    await Firebase.initializeApp();
  } catch (_) {}
  debugPrint('Handling FCM background/terminated message ID: ${message.messageId}, Data: ${message.data}');
}

typedef NotificationNavigationCallback = void Function(String referenceType, String referenceId);

class NotificationService {
  final ApiClient _apiClient;
  final SecureStorageService _storageService;
  final FlutterLocalNotificationsPlugin _localNotifications = FlutterLocalNotificationsPlugin();

  NotificationNavigationCallback? onNotificationTap;
  String? _fcmToken;
  bool _isInitialized = false;

  NotificationService(this._apiClient, this._storageService);

  String? get fcmToken => _fcmToken;

  Future<void> initialize({NotificationNavigationCallback? onNavigate}) async {
    if (_isInitialized) return;
    onNotificationTap = onNavigate;

    try {
      // 1. Initialize Local Notifications Plugin
      const androidInit = AndroidInitializationSettings('@mipmap/ic_launcher');
      const darwinInit = DarwinInitializationSettings(
        requestAlertPermission: true,
        requestBadgePermission: true,
        requestSoundPermission: true,
      );
      const initSettings = InitializationSettings(android: androidInit, iOS: darwinInit);

      await _localNotifications.initialize(
        initSettings,
        onDidReceiveNotificationResponse: (response) {
          final payload = response.payload;
          if (payload != null && payload.isNotEmpty) {
            _handlePayload(payload);
          }
        },
      );

      // Create Android Notification Channel with High Importance
      const channel = AndroidNotificationChannel(
        'samadhanx_civic_channel',
        'SamadhanX Civic Alerts',
        description: 'Real-time updates regarding your reported societal challenges and departmental progress.',
        importance: Importance.max,
        playSound: true,
        enableVibration: true,
      );

      await _localNotifications
          .resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>()
          ?.createNotificationChannel(channel);

      // 2. Initialize Firebase Core & Messaging
      try {
        await Firebase.initializeApp();
        final messaging = FirebaseMessaging.instance;

        // Set presentation options for foreground notifications
        await FirebaseMessaging.instance.setForegroundNotificationPresentationOptions(
          alert: true,
          badge: true,
          sound: true,
        );

        // Request Permissions
        final settings = await messaging.requestPermission(
          alert: true,
          badge: true,
          sound: true,
          provisional: false,
        );

        debugPrint('FCM Notification permission status: ${settings.authorizationStatus}');

        if (settings.authorizationStatus == AuthorizationStatus.authorized ||
            settings.authorizationStatus == AuthorizationStatus.provisional) {
          
          // Get Real FCM Registration Token
          _fcmToken = await messaging.getToken();
          debugPrint('FCM Registration Token obtained: $_fcmToken');
          if (_fcmToken != null) {
            await syncCurrentTokenWithBackend();
          }

          // Listen for Token Refresh
          messaging.onTokenRefresh.listen((newToken) {
            _fcmToken = newToken;
            debugPrint('FCM Token refreshed: $_fcmToken');
            syncCurrentTokenWithBackend();
          });

          // Foreground Message Listener
          FirebaseMessaging.onMessage.listen((RemoteMessage message) {
            debugPrint('FCM Foreground message received: ${message.notification?.title}');
            _showLocalNotification(message);
          });

          // Background Message Open Handler
          FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
            debugPrint('FCM Notification opened from background: ${message.data}');
            _handleRemoteMessageNavigation(message);
          });

          // Terminated State Message Open Handler
          final initialMessage = await messaging.getInitialMessage();
          if (initialMessage != null) {
            debugPrint('FCM Notification opened from terminated state: ${initialMessage.data}');
            _handleRemoteMessageNavigation(initialMessage);
          }
        }
      } catch (fbError) {
        debugPrint('Firebase messaging initialization note: $fbError (In-app notification fallback active)');
      }

      _isInitialized = true;
    } catch (e) {
      debugPrint('Notification service initialization notice: $e');
    }
  }

  /// Syncs the current FCM token with the backend device token registry
  Future<void> syncCurrentTokenWithBackend() async {
    if (_fcmToken == null || _fcmToken!.isEmpty) {
      try {
        _fcmToken = await FirebaseMessaging.instance.getToken();
      } catch (_) {}
    }
    if (_fcmToken == null || _fcmToken!.isEmpty) return;

    try {
      final token = await _storageService.getToken();
      if (token == null || token.isEmpty) {
        debugPrint('Skip token sync: No active user session token');
        return;
      }

      await _apiClient.post(
        '/api/v1/notifications/device-tokens',
        data: {
          'token': _fcmToken,
          'deviceType': defaultTargetPlatform == TargetPlatform.iOS ? 'IOS' : 'ANDROID',
        },
      );
      debugPrint('Successfully registered device token with SamadhanX backend for current user');
    } catch (e) {
      debugPrint('Notice: Device token sync with backend skipped or failed: $e');
    }
  }

  /// Unregister device token from backend on logout
  Future<void> unregisterTokenOnLogout() async {
    if (_fcmToken == null) return;
    try {
      await _apiClient.delete('/api/v1/notifications/device-tokens/$_fcmToken');
      debugPrint('Unregistered device token on logout');
      _fcmToken = null;
    } catch (e) {
      debugPrint('Notice: Failed to unregister device token on logout: $e');
    }
  }

  void _showLocalNotification(RemoteMessage message) {
    final notification = message.notification;
    final data = message.data;

    final title = notification?.title ?? data['title'] ?? 'SamadhanX Civic Alert';
    final body = notification?.body ?? data['body'] ?? 'You have a new challenge update.';
    final refId = data['referenceId'] ?? '';
    final refType = data['referenceType'] ?? 'CHALLENGE';

    _localNotifications.show(
      DateTime.now().millisecondsSinceEpoch ~/ 1000,
      title,
      body,
      const NotificationDetails(
        android: AndroidNotificationDetails(
          'samadhanx_civic_channel',
          'SamadhanX Civic Alerts',
          importance: Importance.max,
          priority: Priority.high,
          icon: '@mipmap/ic_launcher',
        ),
      ),
      payload: '$refType|$refId',
    );
  }

  void _handlePayload(String payload) {
    final parts = payload.split('|');
    if (parts.length == 2 && parts[1].isNotEmpty) {
      onNotificationTap?.call(parts[0], parts[1]);
    }
  }

  void _handleRemoteMessageNavigation(RemoteMessage message) {
    final refType = message.data['referenceType']?.toString() ?? 'CHALLENGE';
    final refId = message.data['referenceId']?.toString();
    if (refId != null && refId.isNotEmpty) {
      onNotificationTap?.call(refType, refId);
    }
  }
}
