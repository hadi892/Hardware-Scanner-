package com.example.di

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.repository.HardwareAnalysisRepository

object DIContainer {
    @Volatile
    private var repository: HardwareAnalysisRepository? = null

    fun provideRepository(context: Context): HardwareAnalysisRepository {
        return repository ?: synchronized(this) {
            val db = AppDatabase.getDatabase(context)
            val repo = HardwareAnalysisRepository(context.applicationContext, db.scanReportDao())
            repository = repo
            repo
        }
    }
}
