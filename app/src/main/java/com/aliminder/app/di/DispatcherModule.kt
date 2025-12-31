package com.aliminder.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executors
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.asCoroutineDispatcher

/**
 * Hilt module providing custom coroutine dispatchers.
 * 
 * Critical for Audio Engine: Dedicated single-threaded dispatcher ensures
 * precise 250ms gaps in Mad-Lib stitching without UI lag interference.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    @DefaultDispatcher
    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    @IoDispatcher
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @MainDispatcher
    @Provides
    @Singleton
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    
    /**
     * Audio Engine Dispatcher: Dedicated single-threaded dispatcher with
     * maximum priority for precise audio timing.
     * 
     * Used exclusively by VinylStackEngine for:
     * - 10-second prep window scheduling
     * - Needle drop timing
     * - Mad-Lib gap precision (250ms)
     */
    @AudioDispatcher
    @Provides
    @Singleton
    fun provideAudioDispatcher(): CoroutineDispatcher {
        return Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "VinylStackThread").apply {
                priority = Thread.MAX_PRIORITY
                isDaemon = false
            }
        }.asCoroutineDispatcher()
    }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class AudioDispatcher
