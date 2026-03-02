package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.mgbheights.android.data.local.dao.NoticeDao
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.Notice
import com.mgbheights.shared.domain.model.NoticeCategory
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.NoticeRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val noticeDao: NoticeDao
) : NoticeRepository {

    private val noticesRef = firestore.collection(Constants.COLLECTION_NOTICES)

    override suspend fun getNoticeById(noticeId: String): Resource<Notice> = try {
        val doc = noticesRef.document(noticeId).get().await()
        if (doc.exists()) Resource.success(doc.data!!.toNotice().copy(id = doc.id))
        else Resource.error("Notice not found")
    } catch (e: Exception) {
        val cached = noticeDao.getNoticeById(noticeId)
        if (cached != null) Resource.success(cached.toDomain()) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeNotice(noticeId: String): Flow<Resource<Notice>> = callbackFlow {
        val listener = noticesRef.document(noticeId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toNotice().copy(id = snap.id)))
            else trySend(Resource.error("Not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getNoticesByRole(role: UserRole): Resource<List<Notice>> = try {
        val snap = noticesRef.whereArrayContains("targetRoles", role.name)
            .orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val notices = snap.documents.mapNotNull { it.data?.toNotice()?.copy(id = it.id) }
        noticeDao.insertNotices(notices.map { it.toEntity() })
        Resource.success(notices)
    } catch (e: Exception) {
        val cached = noticeDao.getAllNotices()
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeNoticesByRole(role: UserRole): Flow<Resource<List<Notice>>> = callbackFlow {
        val listener = noticesRef.whereArrayContains("targetRoles", role.name)
            .orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                trySend(Resource.success(snap?.documents?.mapNotNull { it.data?.toNotice()?.copy(id = it.id) } ?: emptyList()))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getNoticesByCategory(category: NoticeCategory): Resource<List<Notice>> = try {
        val snap = noticesRef.whereEqualTo("category", category.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toNotice()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllNotices(): Resource<List<Notice>> = try {
        val snap = noticesRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val notices = snap.documents.mapNotNull { it.data?.toNotice()?.copy(id = it.id) }
        noticeDao.insertNotices(notices.map { it.toEntity() })
        Resource.success(notices)
    } catch (e: Exception) {
        val cached = noticeDao.getAllNotices()
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeAllNotices(): Flow<Resource<List<Notice>>> =
        noticeDao.observeAllNotices().map { Resource.success(it.map { e -> e.toDomain() }) }

    override suspend fun createNotice(notice: Notice): Resource<Notice> = try {
        val docRef = noticesRef.document()
        val newNotice = notice.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(newNotice.toFirestoreMap()).await()
        noticeDao.insertNotice(newNotice.toEntity())
        Resource.success(newNotice)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateNotice(notice: Notice): Resource<Notice> = try {
        val updated = notice.copy(updatedAt = System.currentTimeMillis())
        noticesRef.document(notice.id).set(updated.toFirestoreMap()).await()
        noticeDao.updateNotice(updated.toEntity())
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun deleteNotice(noticeId: String): Resource<Unit> = try {
        noticesRef.document(noticeId).delete().await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun markAsRead(noticeId: String, userId: String): Resource<Unit> = try {
        noticesRef.document(noticeId).update("readBy", FieldValue.arrayUnion(userId)).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getUnreadCount(userId: String, role: UserRole): Resource<Int> = try {
        val snap = noticesRef.whereArrayContains("targetRoles", role.name).get().await()
        val unread = snap.documents.count { doc ->
            val readBy = (doc.data?.get("readBy") as? List<*>) ?: emptyList<String>()
            !readBy.contains(userId)
        }
        Resource.success(unread)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getEmergencyNotices(): Resource<List<Notice>> = try {
        val snap = noticesRef.whereEqualTo("isEmergency", true).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toNotice()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeEmergencyNotices(): Flow<Resource<List<Notice>>> =
        noticeDao.observeEmergencyNotices().map { Resource.success(it.map { e -> e.toDomain() }) }
}

