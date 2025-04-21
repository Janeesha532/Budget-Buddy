package com.example.budgebuddy3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import com.example.budgebuddy3.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    val transactions: LiveData<List<Transaction>> = repository.allTransactions
    
    private val _totalIncome = MutableLiveData<Double>(0.0)
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>(0.0)
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _monthlyBudget = MutableLiveData<Double>(0.0)
    val monthlyBudget: LiveData<Double> = _monthlyBudget

    private val _monthlyExpenses = MutableLiveData<Double>(0.0)
    val monthlyExpenses: LiveData<Double> = _monthlyExpenses

    private val _monthlyIncome = MutableLiveData<Double>(0.0)
    val monthlyIncome: LiveData<Double> = _monthlyIncome

    init {
        updateTotals()
        updateMonthlyCalculations()
    }

    fun setMonthlyBudget(amount: Double) {
        _monthlyBudget.value = amount
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
        updateTotals()
        updateMonthlyCalculations()
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
        updateTotals()
        updateMonthlyCalculations()
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
        updateTotals()
        updateMonthlyCalculations()
    }

    private fun updateTotals() {
        viewModelScope.launch {
            val allTransactions = repository.getAllTransactionsSync()
            _totalIncome.value = allTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            _totalExpenses.value = allTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
        }
    }

    private fun updateMonthlyCalculations() {
        viewModelScope.launch {
            val allTransactions = transactions.value ?: emptyList()
            
            _monthlyExpenses.value = allTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            _monthlyIncome.value = allTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
        }
    }
} 