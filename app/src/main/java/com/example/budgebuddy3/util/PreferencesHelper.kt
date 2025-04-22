package com.example.budgebuddy3.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencesHelper(context: Context) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET_ALERT_THRESHOLD = "budget_alert_threshold"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
        private const val KEY_DAILY_REMINDER_TIME = "daily_reminder_time"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
    }

    var currency: String
        get() = preferences.getString(KEY_CURRENCY, "USD") ?: "USD"
        set(value) = preferences.edit().putString(KEY_CURRENCY, value).apply()

    var monthlyBudget: Double
        get() = preferences.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        set(value) = preferences.edit().putFloat(KEY_MONTHLY_BUDGET, value.toFloat()).apply()

    var budgetAlertThreshold: Int
        get() = preferences.getInt(KEY_BUDGET_ALERT_THRESHOLD, 80)
        set(value) = preferences.edit().putInt(KEY_BUDGET_ALERT_THRESHOLD, value).apply()

    var dailyReminderEnabled: Boolean
        get() = preferences.getBoolean(KEY_DAILY_REMINDER, false)
        set(value) = preferences.edit().putBoolean(KEY_DAILY_REMINDER, value).apply()

    var dailyReminderTime: String
        get() = preferences.getString(KEY_DAILY_REMINDER_TIME, "20:00") ?: "20:00"
        set(value) = preferences.edit().putString(KEY_DAILY_REMINDER_TIME, value).apply()

    fun clearAll() {
        preferences.edit().clear().apply()
    }
} 