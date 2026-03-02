package com.mgbheights.android.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage service for uploading and managing files.
 */
@Singleton
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {

    /**
     * Upload a profile photo and return the download URL.
     */
    suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): Resource<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_PROFILE_PHOTOS}/$userId/profile.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Resource.success(downloadUrl)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to upload profile photo", e)
    }

    /**
     * Upload a visitor photo and return the download URL.
     */
    suspend fun uploadVisitorPhoto(visitorId: String, imageUri: Uri): Resource<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_VISITOR_PHOTOS}/$visitorId/photo.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Resource.success(downloadUrl)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to upload visitor photo", e)
    }

    /**
     * Upload a visitor photo from bytes.
     */
    suspend fun uploadVisitorPhotoBytes(visitorId: String, imageBytes: ByteArray): Resource<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_VISITOR_PHOTOS}/$visitorId/photo.jpg")
        ref.putBytes(imageBytes).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Resource.success(downloadUrl)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to upload visitor photo", e)
    }

    /**
     * Upload an ID proof document.
     */
    suspend fun uploadIdProof(documentId: String, fileUri: Uri): Resource<String> = try {
        val fileName = fileUri.lastPathSegment ?: "id_proof"
        val ref = storage.reference.child("${Constants.STORAGE_ID_PROOFS}/$documentId/$fileName")
        ref.putFile(fileUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Resource.success(downloadUrl)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to upload ID proof", e)
    }

    /**
     * Upload a notice image.
     */
    suspend fun uploadNoticeImage(noticeId: String, imageUri: Uri): Resource<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_NOTICE_IMAGES}/$noticeId/image.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Resource.success(downloadUrl)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to upload notice image", e)
    }

    /**
     * Upload a complaint image.
     */
    suspend fun uploadComplaintImage(complaintId: String, imageUri: Uri, index: Int): Resource<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_COMPLAINT_IMAGES}/$complaintId/image_$index.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Resource.success(downloadUrl)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to upload complaint image", e)
    }

    /**
     * Delete a file from storage.
     */
    suspend fun deleteFile(path: String): Resource<Unit> = try {
        storage.reference.child(path).delete().await()
        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to delete file", e)
    }
}

