import 'dart:io';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:uuid/uuid.dart';
import '../../data/models/challenge_models.dart';
import '../constants/api_endpoints.dart';
import '../network/api_client.dart';

class MediaService {
  final ImagePicker _imagePicker = ImagePicker();
  final Uuid _uuid = const Uuid();

  MediaService(ApiClient _);

  /// Captures live camera photo and embeds instant GPS coordinates
  Future<AttachmentDto?> captureLiveGeotaggedImage({
    String? caption,
  }) async {
    try {
      // 1. Capture instant live GPS location at the moment of photo shutter
      Position? currentPosition;
      try {
        currentPosition = await Geolocator.getCurrentPosition(
          locationSettings: const LocationSettings(
            accuracy: LocationAccuracy.high,
            timeLimit: Duration(seconds: 6),
          ),
        );
      } catch (e) {
        debugPrint('Live geotag capture notice (using last known or best effort): $e');
        try {
          currentPosition = await Geolocator.getLastKnownPosition();
        } catch (_) {}
      }

      // 2. Enforce live camera capture
      final XFile? picked = await _imagePicker.pickImage(
        source: ImageSource.camera,
        maxWidth: 1920,
        maxHeight: 1920,
        imageQuality: 85,
      );

      if (picked == null) return null;

      final fileBytes = await picked.readAsBytes();
      final extension = (picked.name.contains('.') ? picked.name.split('.').last : 'jpg').toLowerCase();
      final mimeType = (extension == 'jpg' || extension == 'jpeg') 
          ? 'image/jpeg' 
          : (extension == 'png' ? 'image/png' : 'image/webp');
      
      final fileName = '${_uuid.v4()}.$extension';
      final path = 'mobile/$fileName';

      // 3. Upload to Supabase Storage
      await Supabase.instance.client.storage
          .from(ApiEndpoints.challengeMediaBucket)
          .uploadBinary(
            path,
            fileBytes,
            fileOptions: FileOptions(
              contentType: mimeType,
              upsert: true,
            ),
          );

      final publicUrl = Supabase.instance.client.storage
          .from(ApiEndpoints.challengeMediaBucket)
          .getPublicUrl(path);

      return AttachmentDto(
        mediaType: 'IMAGE',
        fileName: picked.name,
        fileUrl: publicUrl,
        fileSizeBytes: fileBytes.length,
        caption: caption ?? 'Live On-Site Geotagged Evidence',
        geoLatitude: currentPosition?.latitude,
        geoLongitude: currentPosition?.longitude,
      );
    } catch (e) {
      debugPrint('Error uploading live geotagged image: $e');
      rethrow;
    }
  }

  Future<AttachmentDto?> pickAndUploadDocument({String? caption}) async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['pdf', 'doc', 'docx', 'txt', 'png', 'jpg', 'jpeg'],
        withData: true,
      );

      if (result == null || result.files.isEmpty) return null;
      final file = result.files.first;

      Uint8List? bytes = file.bytes;
      if (bytes == null && file.path != null) {
        final ioFile = File(file.path!);
        bytes = await ioFile.readAsBytes();
      }

      if (bytes == null) return null;

      final extension = (file.name.contains('.') ? file.name.split('.').last : 'pdf').toLowerCase();
      final fileName = '${_uuid.v4()}.$extension';
      final path = 'mobile/$fileName';

      await Supabase.instance.client.storage
          .from(ApiEndpoints.challengeDocumentsBucket)
          .uploadBinary(
            path,
            bytes,
            fileOptions: const FileOptions(upsert: true),
          );

      final publicUrl = Supabase.instance.client.storage
          .from(ApiEndpoints.challengeDocumentsBucket)
          .getPublicUrl(path);

      return AttachmentDto(
        mediaType: 'DOCUMENT',
        fileName: file.name,
        fileUrl: publicUrl,
        fileSizeBytes: bytes.length,
        caption: caption,
      );
    } catch (e) {
      debugPrint('Error uploading document to Supabase Storage: $e');
      rethrow;
    }
  }
}
