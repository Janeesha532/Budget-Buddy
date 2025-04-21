package com.example.budgebuddy3.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE type = :transactionType ORDER BY date DESC")
    fun getTransactionsByType(transactionType: TransactionType): LiveData<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :transactionType")
    fun getTotalByType(transactionType: TransactionType): LiveData<Double?>

    @Query("SELECT * FROM transactions WHERE type = :type")
    fun getTransactionsByTypeString(type: String): LiveData<List<Transaction>>
} 