package com.learningtutor.di

import android.content.Context
import androidx.room.Room
import com.learningtutor.data.database.AppDatabase
import com.learningtutor.data.database.ProgressDao
import com.learningtutor.data.database.QuestionDao
import com.learningtutor.data.database.ScheduleDao
import com.learningtutor.data.database.UserDao
import com.learningtutor.data.repositories.*
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase) = database.userDao()

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase) = database.questionDao()

    @Provides
    @Singleton
    fun provideProgressDao(database: AppDatabase) = database.progressDao()

    @Provides
    @Singleton
    fun provideScheduleDao(database: AppDatabase) = database.scheduleDao()

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideQuestionRepository(
        questionDao: QuestionDao,
        progressDao: ProgressDao,
        scheduleDao: ScheduleDao,
        userRepository: UserRepository
    ): QuestionRepository {
        return QuestionRepository(questionDao, progressDao, scheduleDao, userRepository)
    }
}