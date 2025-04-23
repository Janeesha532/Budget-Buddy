package com.example.budgebuddy3.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgebuddy3.BudgetApplication
import com.example.budgebuddy3.R
import com.example.budgebuddy3.databinding.FragmentBudgetBinding
import com.example.budgebuddy3.util.PreferencesHelper
import com.example.budgebuddy3.viewmodel.TransactionViewModel
import com.example.budgebuddy3.viewmodel.TransactionViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val repository = (requireActivity().application as BudgetApplication).repository
        val preferencesHelper = PreferencesHelper(requireContext())
        val factory = TransactionViewModelFactory(repository, preferencesHelper, requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Add text change listener to format input as currency
        binding.budgetEditText.doAfterTextChanged { text ->
            if (text != null && text.isNotEmpty()) {
                try {
                    val amount = text.toString().toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        binding.btnSetBudget.isEnabled = true
                    }
                } catch (e: NumberFormatException) {
                    binding.btnSetBudget.isEnabled = false
                }
            } else {
                binding.btnSetBudget.isEnabled = false
            }
        }

        binding.btnSetBudget.setOnClickListener {
            val budgetAmount = binding.budgetEditText.text.toString().toDoubleOrNull() ?: 0.0
            if (budgetAmount > 0) {
                viewModel.setMonthlyBudget(budgetAmount)
                binding.budgetEditText.clearFocus()
                Snackbar.make(binding.root, "Monthly budget updated!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.monthlyBudget.observe(viewLifecycleOwner) { budget ->
            binding.budgetEditText.setText(budget.toString())
            updateBudgetSummary(budget)
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.tvMonthlyExpenses.text = getString(
                R.string.monthly_expenses,
                currencyFormat.format(expenses)
            )
            updateBudgetSummary(viewModel.monthlyBudget.value ?: 0.0)

            // Show warning if expenses exceed budget
            val budget = viewModel.monthlyBudget.value ?: 0.0
            if (budget > 0 && expenses > budget) {
                Snackbar.make(
                    binding.root,
                    "Warning: You've exceeded your monthly budget!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.monthlyIncome.observe(viewLifecycleOwner) { income ->
            binding.tvMonthlyIncome.text = getString(
                R.string.monthly_income,
                currencyFormat.format(income)
            )
            
            val expenses = viewModel.monthlyExpenses.value ?: 0.0
            val savings = income - expenses
            binding.tvMonthlySavings.text = getString(
                R.string.monthly_savings,
                currencyFormat.format(savings)
            )

            // Set text color based on savings
            binding.tvMonthlySavings.setTextColor(
                requireContext().getColor(
                    if (savings >= 0) android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )
        }
    }

    private fun updateBudgetSummary(budget: Double) {
        val expenses = viewModel.monthlyExpenses.value ?: 0.0
        val remaining = budget - expenses
        val progress = if (budget > 0) {
            ((expenses / budget) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        binding.apply {
            tvMonthlyBudget.text = getString(
                R.string.monthly_budget,
                currencyFormat.format(budget)
            )
            tvRemainingBudget.text = getString(
                R.string.remaining_budget,
                currencyFormat.format(remaining)
            )
            budgetProgressBar.progress = progress
            tvBudgetProgress.text = getString(
                R.string.budget_progress,
                progress
            )

            // Set color for remaining budget
            tvRemainingBudget.setTextColor(
                requireContext().getColor(
                    if (remaining >= 0) android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )

            // Update progress bar color based on progress
            budgetProgressBar.setIndicatorColor(
                requireContext().getColor(
                    when {
                        progress >= 90 -> android.R.color.holo_red_dark
                        progress >= 75 -> android.R.color.holo_orange_dark
                        else -> android.R.color.holo_green_dark
                    }
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 