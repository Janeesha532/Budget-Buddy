package com.example.budgebuddy3.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgebuddy3.BudgetApplication
import com.example.budgebuddy3.R
import com.example.budgebuddy3.adapter.TransactionAdapter
import com.example.budgebuddy3.databinding.FragmentTransactionsBinding
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import com.example.budgebuddy3.util.DataExportImportUtil
import com.example.budgebuddy3.util.CurrencyHelper
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
    private lateinit var dataExportImportUtil: DataExportImportUtil
    private lateinit var preferencesHelper: PreferencesHelper
    private var currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val success = dataExportImportUtil.exportTransactions(viewModel.transactions.value ?: emptyList(), uri)
                Toast.makeText(
                    requireContext(),
                    if (success) "Transactions exported successfully" else "Failed to export transactions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val importedTransactions = dataExportImportUtil.importTransactions(uri)
                if (importedTransactions != null) {
                    // Show confirmation dialog with the number of transactions
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Import Transactions")
                        .setMessage("Do you want to import ${importedTransactions.size} transactions?")
                        .setPositiveButton("Import") { _, _ ->
                            importedTransactions.forEach { transaction ->
                                viewModel.insert(transaction)
                            }
                            Toast.makeText(requireContext(), "Transactions imported successfully", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Failed to import transactions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

        dataExportImportUtil = DataExportImportUtil(requireContext())
        setupViewModel()
        setupRecyclerView()
        setupFab()
        observeViewModel()
        setupMenu()

        preferencesHelper = PreferencesHelper(requireContext())
        currencyFormat = CurrencyHelper.getCurrencyFormatter(preferencesHelper.currency)
    }

    private fun setupMenu() {
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.transactions_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_export -> {
                        startExport()
                        true
                    }
                    R.id.action_import -> {
                        startImport()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onResume() {
        super.onResume()
        currencyFormat = CurrencyHelper.getCurrencyFormatter(preferencesHelper.currency)
        adapter.updateCurrencyFormat()
        updateUI()
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "budget_buddy_transactions.json")
        }
        exportLauncher.launch(intent)
    }

    private fun startImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importLauncher.launch(intent)
    }

    private fun setupViewModel() {
        val application = requireActivity().application as BudgetApplication
        preferencesHelper = PreferencesHelper(requireContext())
        val factory = TransactionViewModelFactory(application.repository, preferencesHelper, application)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(requireContext(), this)
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

    private fun updateUI() {
        // Call updateSummary with the current transactions
        viewModel.transactions.value?.let { transactions ->
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
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
            
            val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.amountEditText)
            val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText)
            val categoryEditText = dialogView.findViewById<TextInputEditText>(R.id.categoryEditText)
            val typeToggleGroup = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.typeToggleGroup)

            if (amountEditText == null || descriptionEditText == null || 
                categoryEditText == null || typeToggleGroup == null) {
                Toast.makeText(requireContext(), "Error loading dialog layout", Toast.LENGTH_SHORT).show()
                return
            }

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
                    try {
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
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error saving transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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