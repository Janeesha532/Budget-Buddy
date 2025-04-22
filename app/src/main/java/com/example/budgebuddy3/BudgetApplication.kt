package com.example.budgebuddy3

import android.app.Application
import com.example.budgebuddy3.database.TransactionDatabase
import com.example.budgebuddy3.repository.TransactionRepository

class BudgetApplication : Application() {
    val database by lazy { TransactionDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }

    override fun onCreate() {
        super.onCreate()
        // Initialize database
        database
    }
} 