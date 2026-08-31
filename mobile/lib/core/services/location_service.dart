import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';

class LocationResult {
  final double latitude;
  final double longitude;
  final String? locality;
  final String? district;
  final String? state;
  final String? pincode;
  final String? displayName;

  LocationResult({
    required this.latitude,
    required this.longitude,
    this.locality,
    this.district,
    this.state,
    this.pincode,
    this.displayName,
  });
}

class LocationService {
  final Dio _dio = Dio(BaseOptions(
    connectTimeout: const Duration(seconds: 8),
    receiveTimeout: const Duration(seconds: 8),
    headers: {'User-Agent': 'SamadhanX-Citizen-App/1.0'},
  ));

  Future<LocationResult?> getCurrentLocation() async {
    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        debugPrint('Location services are disabled');
      }

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          return null;
        }
      }

      if (permission == LocationPermission.deniedForever) {
        return null;
      }

      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.high,
          timeLimit: Duration(seconds: 10),
        ),
      );

      String? locality;
      String? district;
      String? state;
      String? pincode;
      String? displayName;

      // Real reverse-geocoding from coordinates to human address
      try {
        final geoRes = await _dio.get(
          'https://nominatim.openstreetmap.org/reverse',
          queryParameters: {
            'format': 'jsonv2',
            'lat': position.latitude,
            'lon': position.longitude,
            'addressdetails': 1,
          },
        );

        if (geoRes.statusCode == 200 && geoRes.data is Map) {
          final data = geoRes.data as Map<String, dynamic>;
          displayName = data['display_name']?.toString();
          final addr = data['address'] as Map<String, dynamic>?;
          if (addr != null) {
            locality = addr['suburb'] ?? addr['neighbourhood'] ?? addr['residential'] ?? addr['road'] ?? addr['village'] ?? addr['town'];
            district = addr['state_district'] ?? addr['county'] ?? addr['city'] ?? addr['district'];
            state = addr['state'];
            pincode = addr['postcode'];
          }
        }
      } catch (e) {
        debugPrint('Reverse geocoding error: $e');
      }

      return LocationResult(
        latitude: position.latitude,
        longitude: position.longitude,
        locality: locality,
        district: district,
        state: state,
        pincode: pincode,
        displayName: displayName,
      );
    } catch (e) {
      debugPrint('Error getting GPS location: $e');
      return null;
    }
  }
}
