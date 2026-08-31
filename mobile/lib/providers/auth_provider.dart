import 'package:flutter/foundation.dart';
import '../core/services/notification_service.dart';
import '../data/models/auth_models.dart';
import '../data/repositories/auth_repository.dart';

enum AuthStatus {
  initial,
  authenticating,
  authenticated,
  unauthenticated,
  error,
}

class AuthProvider extends ChangeNotifier {
  final AuthRepository _authRepository;
  NotificationService? _notificationService;

  AuthStatus _status = AuthStatus.initial;
  UserModel? _currentUser;
  String? _errorMessage;

  AuthProvider(this._authRepository, [this._notificationService]);

  void setNotificationService(NotificationService notificationService) {
    _notificationService = notificationService;
  }

  AuthStatus get status => _status;
  UserModel? get currentUser => _currentUser;
  String? get errorMessage => _errorMessage;
  bool get isAuthenticated => _status == AuthStatus.authenticated && _currentUser != null;

  Future<void> checkAuthStatus() async {
    _status = AuthStatus.authenticating;
    notifyListeners();

    try {
      final hasSession = await _authRepository.hasValidSession();
      if (!hasSession) {
        _status = AuthStatus.unauthenticated;
        _currentUser = null;
        notifyListeners();
        return;
      }

      // Load cached user profile first for instant UI response
      final cached = await _authRepository.getCachedUser();
      if (cached != null) {
        _currentUser = cached;
      }

      // Fetch fresh user profile from backend
      final fresh = await _authRepository.getCurrentUser();
      _currentUser = fresh;
      _status = AuthStatus.authenticated;
      _errorMessage = null;

      // Sync FCM device token with authenticated session
      _notificationService?.syncCurrentTokenWithBackend();
    } catch (e) {
      if (_currentUser != null) {
        // Keep offline/cached user if token exists
        _status = AuthStatus.authenticated;
      } else {
        _status = AuthStatus.unauthenticated;
      }
    }

    notifyListeners();
  }

  Future<bool> login(String email, String password) async {
    _status = AuthStatus.authenticating;
    _errorMessage = null;
    notifyListeners();

    try {
      final response = await _authRepository.login(email, password);
      _currentUser = response.user;
      _status = AuthStatus.authenticated;
      notifyListeners();

      // Register real FCM device token with backend immediately after login
      _notificationService?.syncCurrentTokenWithBackend();
      return true;
    } catch (e) {
      _status = AuthStatus.error;
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return false;
    }
  }

  Future<bool> register({
    required String email,
    required String password,
    required String firstName,
    required String lastName,
    String? phoneNumber,
    String role = 'CITIZEN',
  }) async {
    _status = AuthStatus.authenticating;
    _errorMessage = null;
    notifyListeners();

    try {
      final response = await _authRepository.register(
        email: email,
        password: password,
        firstName: firstName,
        lastName: lastName,
        phoneNumber: phoneNumber,
        role: role,
      );
      _currentUser = response.user;
      _status = AuthStatus.authenticated;
      notifyListeners();

      // Register real FCM device token with backend
      _notificationService?.syncCurrentTokenWithBackend();
      return true;
    } catch (e) {
      _status = AuthStatus.error;
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    // Unregister device token from backend before clearing session
    await _notificationService?.unregisterTokenOnLogout();

    await _authRepository.logout();
    _currentUser = null;
    _status = AuthStatus.unauthenticated;
    _errorMessage = null;
    notifyListeners();
  }

  void handleUnauthorized() {
    _notificationService?.unregisterTokenOnLogout();
    _currentUser = null;
    _status = AuthStatus.unauthenticated;
    _errorMessage = 'Session expired. Please sign in again.';
    notifyListeners();
  }
}
