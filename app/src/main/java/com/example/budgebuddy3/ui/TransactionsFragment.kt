package com.example.budgebuddy3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgebuddy3.R
import com.example.budgebuddy3.adapter.TransactionAdapter
import com.example.budgebuddy3.database.TransactionDatabase
import com.example.budgebuddy3.databinding.FragmentTransactionsBinding
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import com.example.budgebuddy3.repository.TransactionRepository
import com.example.budgebuddy3.viewmodel.TransactionViewModel
import com.example.budgebuddy3.viewmodel.TransactionViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.Date

class TransactionsFragment : Fragment(), TransactionAdapter.TransactionClickListener {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter

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
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao())
        val factory = TransactionViewModelFactory(repository)
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
            showAddTransactionDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
        }
    }

    private fun showAddTransactionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.amountEditText)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText)
        val categoryEditText = dialogView.findViewById<TextInputEditText>(R.id.categoryEditText)
        val typeToggleGroup = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.typeToggleGroup)

        // Set default selection to expense
        typeToggleGroup.check(R.id.expenseButton)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_transaction)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
                val description = descriptionEditText.text.toString()
                val category = categoryEditText.text.toString()
                val type = when (typeToggleGroup.checkedButtonId) {
                    R.id.incomeButton -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }

                val transaction = Transaction(
                    amount = amount,
                    description = description,
                    category = category,
                    type = type,
                    date = Date()
                )
                
                viewModel.insert(transaction)
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
        // Handle edit click
        showAddTransactionDialog()
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