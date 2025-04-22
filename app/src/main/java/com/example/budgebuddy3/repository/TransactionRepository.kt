package com.example.budgebuddy3.repository

import androidx.lifecycle.LiveData
import com.example.budgebuddy3.database.TransactionDao
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import java.util.Calendar

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    val totalIncome: LiveData<Double?> = transactionDao.getTotalByType(TransactionType.INCOME)
    val totalExpenses: LiveData<Double?> = transactionDao.getTotalByType(TransactionType.EXPENSE)

    suspend fun getAllTransactionsSync(): List<Transaction> {
        return transactionDao.getAllTransactionsSync()
    }

    suspend fun getCurrentMonthTransactions(): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        return transactionDao.getTransactionsByMonthYear(currentMonth, currentYear)
    }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    fun getIncomeTransactions(): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(TransactionType.INCOME)
    }

    fun getExpenseTransactions(): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(TransactionType.EXPENSE)
    }

    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }
} 