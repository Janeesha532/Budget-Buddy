package com.example.budgebuddy3.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import com.example.budgebuddy3.repository.TransactionRepository
import com.example.budgebuddy3.util.CurrencyHelper
import com.example.budgebuddy3.util.NotificationHelper
import com.example.budgebuddy3.util.PreferencesHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val preferencesHelper: PreferencesHelper,
    private val application: Application
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

    private val notificationHelper = NotificationHelper(application)

    init {
        // Initialize values when ViewModel is created
        viewModelScope.launch {
            updateTotals()
            updateMonthlyCalculations()
        }

        // Observe transactions for changes
        transactions.observeForever { _ ->
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

            val monthlyExpenses = currentMonthTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            _monthlyExpenses.value = monthlyExpenses

            _monthlyIncome.value = currentMonthTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }

            // Check budget limits and show notifications
            val budget = _monthlyBudget.value ?: 0.0
            if (budget > 0) {
                val progress = (monthlyExpenses / budget * 100).toInt()
                val threshold = preferencesHelper.budgetAlertThreshold

                when {
                    progress >= 100 -> {
                        notificationHelper.sendBudgetPushNotification(
                            "Budget Exceeded!",
                            "You have exceeded your monthly budget by ${getCurrencyFormat().format(monthlyExpenses - budget)}"
                        )
                    }
                    progress >= threshold -> {
                        notificationHelper.sendBudgetPushNotification(
                            "Budget Warning",
                            "You have reached ${progress}% of your monthly budget"
                        )
                    }
                }
            }
        }
    }

    private fun getCurrencyFormat(): java.text.NumberFormat {
        return CurrencyHelper.getCurrencyFormatter(preferencesHelper.currency)
    }

    companion object {
        // Remove static currencyFormat, we'll use dynamic one
    }
} 