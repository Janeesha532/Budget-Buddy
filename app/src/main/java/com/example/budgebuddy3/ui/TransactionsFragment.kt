package com.example.budgebuddy3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgebuddy3.BudgetApplication
import com.example.budgebuddy3.R
import com.example.budgebuddy3.adapter.TransactionAdapter
import com.example.budgebuddy3.databinding.FragmentTransactionsBinding
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import com.example.budgebuddy3.util.PreferencesHelper
import com.example.budgebuddy3.viewmodel.TransactionViewModel
import com.example.budgebuddy3.viewmodel.TransactionViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class TransactionsFragment : Fragment(), TransactionAdapter.TransactionClickListener {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupViewModel() {
        val application = requireActivity().application as BudgetApplication
        val preferencesHelper = PreferencesHelper(requireContext())
        val factory = TransactionViewModelFactory(application.repository, preferencesHelper)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(this)
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TransactionsFragment.adapter
        }
    }

    private fun setupFab() {
        binding.addTransactionFab.setOnClickListener {
            showTransactionDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            updateSummary(transactions)
        }
    }

    private fun updateSummary(transactions: List<Transaction>) {
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        val totalExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val balance = totalIncome - totalExpenses

        binding.totalIncomeText.text = getString(R.string.monthly_income, currencyFormat.format(totalIncome))
        binding.totalExpensesText.text = getString(R.string.monthly_expenses, currencyFormat.format(totalExpenses))
        binding.balanceText.text = getString(R.string.monthly_savings, currencyFormat.format(balance))

        // Set text color for balance based on whether it's positive or negative
        binding.balanceText.setTextColor(
            requireContext().getColor(
                if (balance >= 0) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            )
        )
    }

    private fun showTransactionDialog(existingTransaction: Transaction? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.amountEditText)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText)
        val categoryEditText = dialogView.findViewById<TextInputEditText>(R.id.categoryEditText)
        val typeToggleGroup = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.typeToggleGroup)

        // Pre-fill fields if editing
        existingTransaction?.let { transaction ->
            // Remove currency symbol and formatting for clean number
            val amountStr = transaction.amount.toString()
            amountEditText.setText(amountStr)
            descriptionEditText.setText(transaction.description)
            categoryEditText.setText(transaction.category)
            typeToggleGroup.check(
                when (transaction.type) {
                    TransactionType.INCOME -> R.id.incomeButton
                    TransactionType.EXPENSE -> R.id.expenseButton
                }
            )
        } ?: typeToggleGroup.check(R.id.expenseButton) // Default to expense for new transactions

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existingTransaction == null) R.string.add_transaction else R.string.edit_transaction)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
                val description = descriptionEditText.text.toString()
                val category = categoryEditText.text.toString()
                val type = when (typeToggleGroup.checkedButtonId) {
                    R.id.incomeButton -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }

                if (existingTransaction != null) {
                    val updatedTransaction = existingTransaction.copy(
                        amount = amount,
                        description = description,
                        category = category,
                        type = type
                    )
                    viewModel.update(updatedTransaction)
                } else {
                    val newTransaction = Transaction(
                        amount = amount,
                        description = description,
                        category = category,
                        type = type,
                        date = Date()
                    )
                    viewModel.insert(newTransaction)
                }
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

    override fun onTransactionClick(transaction: Transaction) {
        // Handle transaction click if needed
    }

    override fun onEditClick(transaction: Transaction) {
        showTransactionDialog(transaction)
    }

    override fun onDeleteClick(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                viewModel.delete(transaction)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
} 