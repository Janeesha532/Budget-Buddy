package com.example.budgebuddy3.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgebuddy3.BudgetApplication
import com.example.budgebuddy3.R
import com.example.budgebuddy3.databinding.FragmentBudgetBinding
import com.example.budgebuddy3.viewmodel.TransactionViewModel
import com.example.budgebuddy3.viewmodel.TransactionViewModelFactory
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
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSetBudget.setOnClickListener {
            val budgetAmount = binding.budgetEditText.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.setMonthlyBudget(budgetAmount)
        }
    }

    private fun observeViewModel() {
        viewModel.monthlyBudget.observe(viewLifecycleOwner) { budget ->
            binding.budgetEditText.setText(budget?.toString() ?: "")
            updateBudgetSummary(budget)
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.tvMonthlyExpenses.text = String.format(
                getString(R.string.monthly_expenses),
                currencyFormat.format(expenses)
            )
            updateBudgetSummary(viewModel.monthlyBudget.value)
        }

        viewModel.monthlyIncome.observe(viewLifecycleOwner) { income ->
            binding.tvMonthlyIncome.text = String.format(
                getString(R.string.monthly_income),
                currencyFormat.format(income)
            )
            updateIncomeSummary(income)
        }
    }

    private fun updateBudgetSummary(budget: Double?) {
        budget?.let {
            if (it > 0) {
                val expenses = viewModel.monthlyExpenses.value ?: 0.0
                val remaining = it - expenses
                val progress = ((expenses / it) * 100).toInt().coerceIn(0, 100)

                binding.apply {
                    tvMonthlyBudget.text = String.format(
                        getString(R.string.monthly_budget),
                        currencyFormat.format(it)
                    )
                    tvRemainingBudget.text = String.format(
                        getString(R.string.remaining_budget),
                        currencyFormat.format(remaining)
                    )
                    budgetProgressBar.progress = progress
                    tvBudgetProgress.text = String.format(
                        getString(R.string.budget_progress),
                        progress
                    )
                }
            }
        }
    }

    private fun updateIncomeSummary(income: Double) {
        viewModel.monthlyExpenses.value?.let { expenses ->
            val savings = income - expenses
            binding.tvMonthlySavings.text = String.format(
                getString(R.string.monthly_savings),
                currencyFormat.format(savings)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 