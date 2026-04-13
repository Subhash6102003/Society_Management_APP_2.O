import 'dart:io';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../core/constants/app_constants.dart';

/// Handles file uploads to Supabase Storage.
class StorageService {
  StorageService._();
  static final StorageService instance = StorageService._();

  SupabaseClient get _client => Supabase.instance.client;

  /// Uploads a file and returns its public URL.
  Future<String> uploadFile({
    required File file,
    required String bucket,
    required String path,
  }) async {
    await _client.storage.from(bucket).upload(path, file, fileOptions: const FileOptions(upsert: true));
    final publicUrl = _client.storage.from(bucket).getPublicUrl(path);
    return publicUrl;
  }

  Future<String> uploadProfilePhoto(File file, String userId) async {
    return uploadFile(
      file: file,
      bucket: AppConstants.bucketProfiles,
      path: '$userId/avatar.jpg',
    );
  }

  Future<String> uploadIdProof(File file, String userId) async {
    return uploadFile(
      file: file,
      bucket: AppConstants.bucketIdProofs,
      path: '$userId/id_proof.jpg',
    );
  }

  Future<String> uploadVisitorPhoto(File file, String visitorId) async {
    return uploadFile(
      file: file,
      bucket: AppConstants.bucketVisitors,
      path: '$visitorId/photo.jpg',
    );
  }

  Future<String> uploadComplaintImage(File file, String complaintId) async {
    return uploadFile(
      file: file,
      bucket: AppConstants.bucketComplaints,
      path: '$complaintId/image_${DateTime.now().millisecondsSinceEpoch}.jpg',
    );
  }
}
