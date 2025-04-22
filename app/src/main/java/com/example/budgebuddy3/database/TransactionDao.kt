package com.example.budgebuddy3.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import java.util.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    fun getTotalByType(type: TransactionType): LiveData<Double?>

    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%m', date / 1000, 'unixepoch') = :month 
        AND strftime('%Y', date / 1000, 'unixepoch') = :year 
        ORDER BY date DESC
    """)
    suspend fun getTransactionsByMonthYear(month: Int, year: Int): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
} 