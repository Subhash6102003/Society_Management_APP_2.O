package com.mgbheights.android.di

import com.mgbheights.shared.domain.repository.*
import com.mgbheights.shared.domain.usecase.auth.*
import com.mgbheights.shared.domain.usecase.complaint.*
import com.mgbheights.shared.domain.usecase.maintenance.*
import com.mgbheights.shared.domain.usecase.notice.*
import com.mgbheights.shared.domain.usecase.payment.*
import com.mgbheights.shared.domain.usecase.visitor.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Auth
    @Provides @Singleton
    fun provideLoginWithEmailUseCase(repo: AuthRepository) = LoginWithEmailUseCase(repo)

    @Provides @Singleton
    fun provideSignUpWithEmailUseCase(repo: AuthRepository) = SignUpWithEmailUseCase(repo)

    @Provides @Singleton
    fun provideGetCurrentUserUseCase(repo: AuthRepository) = GetCurrentUserUseCase(repo)

    // Maintenance
    @Provides @Singleton
    fun provideGetBillsUseCase(repo: MaintenanceRepository) = GetBillsUseCase(repo)

    @Provides @Singleton
    fun provideGenerateBillUseCase(repo: MaintenanceRepository) = GenerateBillUseCase(repo)

    @Provides @Singleton
    fun provideMarkBillPaidUseCase(repo: MaintenanceRepository) = MarkBillPaidUseCase(repo)

    // Payment
    @Provides @Singleton
    fun provideInitiatePaymentUseCase(repo: PaymentRepository) = InitiatePaymentUseCase(repo)

    @Provides @Singleton
    fun provideGetPaymentHistoryUseCase(repo: PaymentRepository) = GetPaymentHistoryUseCase(repo)

    // Notice
    @Provides @Singleton
    fun provideGetNoticesUseCase(repo: NoticeRepository) = GetNoticesUseCase(repo)

    @Provides @Singleton
    fun provideCreateNoticeUseCase(repo: NoticeRepository) = CreateNoticeUseCase(repo)

    // Complaint
    @Provides @Singleton
    fun provideGetComplaintsUseCase(repo: ComplaintRepository) = GetComplaintsUseCase(repo)

    @Provides @Singleton
    fun provideCreateComplaintUseCase(repo: ComplaintRepository) = CreateComplaintUseCase(repo)

    @Provides @Singleton
    fun provideUpdateComplaintStatusUseCase(repo: ComplaintRepository) = UpdateComplaintStatusUseCase(repo)

    // Visitor
    @Provides @Singleton
    fun provideGetVisitorsUseCase(repo: VisitorRepository) = GetVisitorsUseCase(repo)

    @Provides @Singleton
    fun provideAddVisitorUseCase(repo: VisitorRepository) = AddVisitorUseCase(repo)

    @Provides @Singleton
    fun provideApproveVisitorUseCase(repo: VisitorRepository) = ApproveVisitorUseCase(repo)
}
