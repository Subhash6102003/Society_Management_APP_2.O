package com.mgbheights.android.data.repository

import com.google.firebase.storage.FirebaseStorage
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.domain.repository.StorageRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadImage(path: String, byteArray: ByteArray): Resource<String> = try {
        val ref = storage.reference.child(path)
        ref.putBytes(byteArray).await()
        val url = ref.downloadUrl.await().toString()
        Resource.success(url)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Upload failed", e)
    }

    override suspend fun deleteImage(url: String): Resource<Unit> = try {
        storage.getReferenceFromUrl(url).delete().await()
        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Delete failed", e)
    }
}
