package com.example.todolist.di

import android.app.Application
import androidx.room.Room
import com.example.todolist.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        application: Application,
        callback: TaskDatabase.CallBack
    ) = Room.databaseBuilder(application, TaskDatabase::class.java, "task_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope