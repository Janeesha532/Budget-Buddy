package com.example.budgebuddy3.util

import android.content.Context
import com.example.budgebuddy3.database.AppDatabase
import com.example.budgebuddy3.model.Budget
import com.example.budgebuddy3.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class DataBackupHelper(private val context: Context) {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    suspend fun exportData(): String = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context)
        val transactions = database.transactionDao().getAllTransactionsSync()
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val budgets = database.budgetDao().getBudgetsByMonthSync(currentMonth, currentYear)

        val backupData = BackupData(
            transactions = transactions,
            budgets = budgets
        )

        val fileName = "budgebuddy_backup_${dateFormat.format(Date())}.json"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            gson.toJson(backupData, writer)
        }

        file.absolutePath
    }

    suspend fun importData(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext false

            val backupData: BackupData = FileReader(file).use { reader ->
                gson.fromJson(reader, object : TypeToken<BackupData>() {}.type)
            }

            val database = AppDatabase.getDatabase(context)
            
            // Clear existing data
            database.transactionDao().deleteAllTransactions()
            database.budgetDao().deleteAllBudgets()

            // Insert backup data
            backupData.transactions.forEach { transaction ->
                database.transactionDao().insert(transaction)
            }

            backupData.budgets.forEach { budget ->
                database.budgetDao().insertBudget(budget)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>
    )
} 