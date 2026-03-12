package com.mgbheights.android.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.mgbheights.android.data.local.MgbDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()


    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

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

