package com.learningtutor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LearningTutorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация при необходимости
    }
}