package com.example.budgebuddy3.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

// Placeholder for Firebase Messaging Service
class BudgetFirebaseMessagingService : Service() {
    private val TAG = "BudgetService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
} 