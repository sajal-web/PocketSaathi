package com.sajalweb.pocketsaathi.di

// di/AppModule.kt
import android.content.Context
import androidx.room.Room
import com.sajalweb.pocketsaathi.data.db.AppDatabase
import com.sajalweb.pocketsaathi.data.prefs.UserPrefsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "spendsense.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideExpenseDao(db: AppDatabase) = db.expenseDao()

    @Provides @Singleton
    fun provideUserPrefsDataStore(@ApplicationContext ctx: Context) = UserPrefsDataStore(ctx)
}