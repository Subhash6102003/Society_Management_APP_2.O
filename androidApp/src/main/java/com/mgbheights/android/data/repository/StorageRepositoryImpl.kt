package com.mgbheights.android.data.repository

import com.mgbheights.shared.domain.repository.StorageRepository
import com.mgbheights.shared.util.Resource
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor() : StorageRepository {

    override suspend fun uploadImage(path: String, byteArray: ByteArray): Resource<String> = try {
        val encoded = Base64.getEncoder().encodeToString(byteArray)
        Resource.success("data:image/jpeg;base64,$encoded")
    } catch (e: Exception) {
        Resource.error(e.message ?: "Base64 conversion failed", e)
    }

    override suspend fun deleteImage(url: String): Resource<Unit> = Resource.success(Unit)
}
