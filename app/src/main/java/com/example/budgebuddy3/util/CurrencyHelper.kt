package com.example.budgebuddy3.util

import java.text.NumberFormat
import java.util.*

object CurrencyHelper {
    
    private val currencyMap = mapOf(
        "USD" to Locale.US,
        "EUR" to Locale.GERMANY,
        "GBP" to Locale.UK,
        "JPY" to Locale.JAPAN,
        "INR" to Locale("en", "IN"),
        "CNY" to Locale.CHINA,
        "AUD" to Locale("en", "AU"),
        "CAD" to Locale("en", "CA"),
        "CHF" to Locale("de", "CH"),
        "LKR" to Locale("si", "LK")
    )
    
    fun getAvailableCurrencies(): List<String> {
        return currencyMap.keys.toList()
    }
    
    fun getCurrencyFormatter(currencyCode: String): NumberFormat {
        val locale = currencyMap[currencyCode] ?: Locale.US
        return NumberFormat.getCurrencyInstance(locale)
    }
    
    fun formatAmount(amount: Double, currencyCode: String): String {
        return getCurrencyFormatter(currencyCode).format(amount)
    }
} 