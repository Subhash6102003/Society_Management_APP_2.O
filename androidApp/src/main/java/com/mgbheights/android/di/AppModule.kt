package com.mgbheights.android.di

import android.content.Context
import androidx.room.Room
import com.mgbheights.android.BuildConfig
import com.mgbheights.android.data.local.MgbDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MgbDatabase {
        return Room.databaseBuilder(
            context,
            MgbDatabase::class.java,
            "mgb_heights_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: MgbDatabase) = database.userDao()

    @Provides
    @Singleton
    fun provideMaintenanceBillDao(database: MgbDatabase) = database.maintenanceBillDao()

    @Provides
    @Singleton
    fun providePaymentDao(database: MgbDatabase) = database.paymentDao()

    @Provides
    @Singleton
    fun provideNoticeDao(database: MgbDatabase) = database.noticeDao()

    @Provides
    @Singleton
    fun provideComplaintDao(database: MgbDatabase) = database.complaintDao()

    @Provides
    @Singleton
    fun provideVisitorDao(database: MgbDatabase) = database.visitorDao()
}
