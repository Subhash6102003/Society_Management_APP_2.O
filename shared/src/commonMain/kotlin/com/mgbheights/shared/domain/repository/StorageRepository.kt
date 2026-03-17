package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.util.Resource

interface StorageRepository {
    suspend fun uploadImage(path: String, byteArray: ByteArray): Resource<String>
    suspend fun deleteImage(url: String): Resource<Unit>
}
