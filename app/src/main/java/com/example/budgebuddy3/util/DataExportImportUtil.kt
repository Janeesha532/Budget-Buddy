package com.example.budgebuddy3.util

import android.content.Context
import android.net.Uri
import com.example.budgebuddy3.model.Transaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.OutputStreamWriter

class DataExportImportUtil(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

    fun exportTransactions(transactions: List<Transaction>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(transactions, writer)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importTransactions(uri: Uri): List<Transaction>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.reader()).use { reader ->
                    val type = object : TypeToken<List<Transaction>>() {}.type
                    gson.fromJson<List<Transaction>>(reader, type)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 