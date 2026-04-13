package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.UserDto
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.android.data.remote.dto.toUser
import com.mgbheights.shared.domain.model.ApprovalStatus
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun loginWithEmail(email: String, password: String): Resource<User> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val uid = supabase.auth.currentUserOrNull()?.id
                ?: return Resource.error("Authentication failed")

            val dto = supabase.from(Constants.COLLECTION_USERS)
                .select { filter { eq("id", uid) } }
                .decodeSingleOrNull<UserDto>()
                ?: return Resource.error("Your account has not been set up. Please contact the administrator.")

            var user = dto.toUser()

            // Auto-promote the designated admin email
            if (email.equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true) &&
                (user.role != UserRole.ADMIN || user.approvalStatus != ApprovalStatus.APPROVED)
            ) {
                val promoted = user.copy(
                    role = UserRole.ADMIN,
                    approvalStatus = ApprovalStatus.APPROVED,
                    isProfileComplete = true
                )
                supabase.from(Constants.COLLECTION_USERS).update(promoted.toDto()) {
                    filter { eq("id", uid) }
                }
                user = promoted
            }

            // Enforce approval gate
            when (user.approvalStatus) {
                ApprovalStatus.REJECTED -> {
                    supabase.auth.signOut()
                    return Resource.error("ACCESS_DENIED: Your account has been rejected. Contact the administrator.")
                }
                ApprovalStatus.PENDING -> { /* allow — UI routes to pending screen */ }
                ApprovalStatus.APPROVED -> { /* full access */ }
            }

            Resource.success(user)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Login failed", e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        role: UserRole,
        name: String
    ): Resource<User> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val uid = supabase.auth.currentUserOrNull()?.id
                ?: return Resource.error("Account creation failed")

            val isAdmin = email.equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true)
            val finalRole = if (isAdmin) UserRole.ADMIN else role

            // Custom UID format: MGB-<ROLE_PREFIX>-<last8_of_uuid>
            val rolePrefix = when (finalRole) {
                UserRole.ADMIN -> "ADM"
                UserRole.RESIDENT -> "RES"
                UserRole.TENANT -> "TEN"
                UserRole.SECURITY_GUARD, UserRole.SECURITY_GUARD_WORKER -> "SEC"
                UserRole.WORKER -> "WRK"
            }
            val customUid = "MGB-$rolePrefix-${uid.takeLast(8).uppercase()}"
            val nameToUse = if (name.isBlank()) customUid else name

            val newUser = User(
                id = uid,
                email = email,
                name = nameToUse,
                role = finalRole,
                approvalStatus = if (isAdmin) ApprovalStatus.APPROVED else ApprovalStatus.PENDING,
                isProfileComplete = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            supabase.from(Constants.COLLECTION_USERS).insert(newUser.toDto())
            Resource.success(newUser)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Sign up failed", e)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val uid = supabase.auth.currentUserOrNull()?.id
                ?: return Resource.error("Not logged in")

            val dto = supabase.from(Constants.COLLECTION_USERS)
                .select { filter { eq("id", uid) } }
                .decodeSingleOrNull<UserDto>()
                ?: return Resource.error("User profile not found")

            Resource.success(dto.toUser())
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to get user", e)
        }
    }

    override fun observeAuthState(): Flow<User?> {
        return supabase.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    try {
                        val uid = status.session.user?.id ?: return@map null
                        supabase.from(Constants.COLLECTION_USERS)
                            .select { filter { eq("id", uid) } }
                            .decodeSingleOrNull<UserDto>()?.toUser()
                    } catch (_: Exception) { null }
                }
                else -> null
            }
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            supabase.auth.signOut()
            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Sign out failed", e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val uid = supabase.auth.currentUserOrNull()?.id
                ?: return Resource.error("Not logged in")
            supabase.from(Constants.COLLECTION_USERS).delete { filter { eq("id", uid) } }
            supabase.auth.signOut()
            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to delete account", e)
        }
    }
}
