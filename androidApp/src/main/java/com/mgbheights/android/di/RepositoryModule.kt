package com.mgbheights.android.di

import com.mgbheights.android.data.repository.*
import com.mgbheights.shared.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindFlatRepository(impl: FlatRepositoryImpl): FlatRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(impl: NoticeRepositoryImpl): NoticeRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(impl: ComplaintRepositoryImpl): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindVisitorRepository(impl: VisitorRepositoryImpl): VisitorRepository

    @Binds
    @Singleton
    abstract fun bindWorkerRepository(impl: WorkerRepositoryImpl): WorkerRepository

    @Binds
    @Singleton
    abstract fun bindEditRequestRepository(impl: EditRequestRepositoryImpl): EditRequestRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository
}
