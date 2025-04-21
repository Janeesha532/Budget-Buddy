package com.example.budgebuddy3.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgebuddy3.R
import com.example.budgebuddy3.database.TransactionDatabase
import com.example.budgebuddy3.databinding.FragmentHomeBinding
import com.example.budgebuddy3.repository.TransactionRepository
import com.example.budgebuddy3.viewmodel.TransactionViewModel
import com.example.budgebuddy3.viewmodel.TransactionViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao())
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.setBudgetButton.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.incomeTextView.text = getString(R.string.monthly_income, currencyFormat.format(income ?: 0.0))
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.expensesTextView.text = getString(R.string.monthly_expenses, currencyFormat.format(expenses ?: 0.0))
        }

        viewModel.monthlyBudget.observe(viewLifecycleOwner) { budget ->
            updateBalance(budget)
        }
    }

    private fun updateBalance(budget: Double?) {
        val income = viewModel.totalIncome.value ?: 0.0
        val expenses = viewModel.totalExpenses.value ?: 0.0
        val balance = income - expenses
        binding.balanceTextView.text = getString(R.string.monthly_savings, currencyFormat.format(balance))

        // Update budget progress if budget is set
        budget?.let {
            if (it > 0) {
                val progress = ((expenses / it) * 100).toInt().coerceIn(0, 100)
                binding.budgetProgressBar.progress = progress
                binding.budgetProgressText.text = getString(R.string.budget_progress, progress)
            }
        }
    }

    private fun showAddTransactionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_budget, null)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.budgetAmountEditText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.set_monthly_budget)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.setMonthlyBudget(amount)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 