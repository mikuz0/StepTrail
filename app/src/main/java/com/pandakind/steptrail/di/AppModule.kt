package com.pandakind.steptrail.di

import android.content.Context
import com.pandakind.steptrail.data.local.AppDatabase
import com.pandakind.steptrail.data.local.WalkDao
import com.pandakind.steptrail.data.location.LocationTracker
import com.pandakind.steptrail.data.repository.WalkRepositoryImpl
import com.pandakind.steptrail.domain.repository.WalkRepository
import com.pandakind.steptrail.domain.usecase.GetWalksUseCase
import com.pandakind.steptrail.domain.usecase.ObserveCurrentWalkUseCase
import com.pandakind.steptrail.domain.usecase.StartWalkUseCase
import com.pandakind.steptrail.domain.usecase.StopWalkUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль для предоставления зависимостей приложения.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Предоставляет синглтон-экземпляр базы данных.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /**
     * Предоставляет DAO для работы с прогулками.
     */
    @Provides
    @Singleton
    fun provideWalkDao(database: AppDatabase): WalkDao {
        return database.walkDao()
    }

    /**
     * Предоставляет репозиторий.
     */
    @Provides
    @Singleton
    fun provideWalkRepository(dao: WalkDao): WalkRepository {
        return WalkRepositoryImpl(dao)
    }

    /**
     * Предоставляет use case для запуска прогулки.
     */
    @Provides
    @Singleton
    fun provideStartWalkUseCase(repository: WalkRepository): StartWalkUseCase {
        return StartWalkUseCase(repository)
    }

    /**
     * Предоставляет use case для остановки прогулки.
     */
    @Provides
    @Singleton
    fun provideStopWalkUseCase(repository: WalkRepository): StopWalkUseCase {
        return StopWalkUseCase(repository)
    }

    /**
     * Предоставляет use case для наблюдения за текущей прогулкой.
     */
    @Provides
    @Singleton
    fun provideObserveCurrentWalkUseCase(repository: WalkRepository): ObserveCurrentWalkUseCase {
        return ObserveCurrentWalkUseCase(repository)
    }

    /**
     * Предоставляет use case для получения списка прогулок.
     */
    @Provides
    @Singleton
    fun provideGetWalksUseCase(repository: WalkRepository): GetWalksUseCase {
        return GetWalksUseCase(repository)
    }

    /**
     * Предоставляет LocationTracker.
     */
    @Provides
    @Singleton
    fun provideLocationTracker(@ApplicationContext context: Context): LocationTracker {
        return LocationTracker(context)
    }
}
