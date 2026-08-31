import 'dart:convert';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../core/storage/secure_storage_service.dart';
import '../models/auth_models.dart';

class AuthRepository {
  final SecureStorageService _storageService;

  AuthRepository(this._storageService);

  Future<AuthResponseModel> login(String email, String password) async {
    final result = await Supabase.instance.client.auth.signInWithPassword(
      email: email.trim(),
      password: password,
    );
    final session = result.session;
    if (session == null) throw Exception('Supabase did not return an active session');
    await _storageService.saveToken(session.accessToken);
    final user = await getCurrentUser();
    return AuthResponseModel(
      accessToken: session.accessToken,
      tokenType: 'Bearer',
      expiresIn: session.expiresIn ?? 0,
      user: user,
    );
  }

  Future<AuthResponseModel> register({
    required String email,
    required String password,
    required String firstName,
    required String lastName,
    String? phoneNumber,
    String role = 'CITIZEN',
  }) async {
    final result = await Supabase.instance.client.auth.signUp(
      email: email.trim(),
      password: password,
      data: {
        'first_name': firstName.trim(),
        'last_name': lastName.trim(),
        'role': role,
        if (phoneNumber != null && phoneNumber.isNotEmpty) 'phone_number': phoneNumber.trim(),
      },
    );
    final session = result.session;
    if (session == null) {
      throw Exception('Check your email to confirm your Supabase account, then sign in.');
    }
    await _storageService.saveToken(session.accessToken);
    final user = await getCurrentUser();
    return AuthResponseModel(
      accessToken: session.accessToken,
      tokenType: 'Bearer',
      expiresIn: session.expiresIn ?? 0,
      user: user,
    );
  }

  Future<UserModel> getCurrentUser() async {
    final currentUser = Supabase.instance.client.auth.currentUser;
    if (currentUser == null) throw Exception('No authenticated user');

    // Fetch profile and roles from Supabase separately to avoid ambiguous foreign key embedding
    final profileRes = await Supabase.instance.client
        .from('profiles')
        .select()
        .eq('id', currentUser.id)
        .maybeSingle();

    final userRolesRes = await Supabase.instance.client
        .from('user_roles')
        .select('role_name')
        .eq('user_id', currentUser.id);

    List<String> roles = [];
    if (userRolesRes is List) {
      roles = (userRolesRes as List)
          .map((r) => r['role_name'].toString())
          .toList();
    }
    if (roles.isEmpty && currentUser.userMetadata?['role'] != null) {
      roles = [currentUser.userMetadata!['role'].toString()];
    }
    if (roles.isEmpty) {
      roles = ['CITIZEN'];
    }

    final user = UserModel(
      id: currentUser.id,
      email: currentUser.email ?? '',
      firstName: profileRes?['first_name'] ?? currentUser.userMetadata?['first_name'] ?? '',
      lastName: profileRes?['last_name'] ?? currentUser.userMetadata?['last_name'] ?? '',
      phoneNumber: profileRes?['phone_number'] ?? currentUser.userMetadata?['phone_number'],
      isActive: profileRes?['is_active'] ?? true,
      roles: roles,
      createdAt: DateTime.tryParse(currentUser.createdAt),
    );

    await _storageService.saveUserJson(jsonEncode(user.toJson()));
    return user;
  }

  Future<UserModel?> getCachedUser() async {
    final userJson = await _storageService.getUserJson();
    if (userJson != null && userJson.isNotEmpty) {
      try {
        return UserModel.fromJson(jsonDecode(userJson) as Map<String, dynamic>);
      } catch (_) {}
    }
    return null;
  }

  Future<bool> hasValidSession() async {
    return Supabase.instance.client.auth.currentSession != null;
  }

  Future<void> logout() async {
    await Supabase.instance.client.auth.signOut();
    await _storageService.clearAll();
  }
}
