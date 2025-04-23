package com.example.budgebuddy3.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.budgebuddy3.repository.TransactionRepository
import com.example.budgebuddy3.util.PreferencesHelper

class TransactionViewModelFactory(
    private val repository: TransactionRepository,
    private val preferencesHelper: PreferencesHelper,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, preferencesHelper, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 