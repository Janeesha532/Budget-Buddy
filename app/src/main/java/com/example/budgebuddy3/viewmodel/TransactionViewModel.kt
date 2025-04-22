package com.example.budgebuddy3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import com.example.budgebuddy3.repository.TransactionRepository
import com.example.budgebuddy3.util.PreferencesHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val preferencesHelper: PreferencesHelper
) : ViewModel() {
    val transactions: LiveData<List<Transaction>> = repository.allTransactions
    
    private val _totalIncome = MutableLiveData<Double>(0.0)
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>(0.0)
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _monthlyBudget = MutableLiveData<Double>(preferencesHelper.monthlyBudget)
    val monthlyBudget: LiveData<Double> = _monthlyBudget

    private val _monthlyExpenses = MutableLiveData<Double>(0.0)
    val monthlyExpenses: LiveData<Double> = _monthlyExpenses

    private val _monthlyIncome = MutableLiveData<Double>(0.0)
    val monthlyIncome: LiveData<Double> = _monthlyIncome

    init {
        // Initialize values when ViewModel is created
        viewModelScope.launch {
            updateTotals()
            updateMonthlyCalculations()
        }

        // Observe transactions for changes
        transactions.observeForever { transactions ->
            viewModelScope.launch {
                updateTotals()
                updateMonthlyCalculations()
            }
        }
    }

    fun setMonthlyBudget(amount: Double) {
        _monthlyBudget.value = amount
        preferencesHelper.monthlyBudget = amount
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
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
            val allTransactions = repository.getAllTransactionsSync()
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            // Filter transactions for current month
            val currentMonthTransactions = allTransactions.filter { transaction ->
                val transactionCalendar = Calendar.getInstance().apply {
                    time = transaction.date
                }
                transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                transactionCalendar.get(Calendar.YEAR) == currentYear
            }

            _monthlyExpenses.value = currentMonthTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            _monthlyIncome.value = currentMonthTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
        }
    }
} 