package com.example.budgebuddy3.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgebuddy3.BudgetApplication
import com.example.budgebuddy3.R
import com.example.budgebuddy3.adapter.CategorySummaryAdapter
import com.example.budgebuddy3.databinding.FragmentAnalysisBinding
import com.example.budgebuddy3.model.CategorySummary
import com.example.budgebuddy3.model.Transaction
import com.example.budgebuddy3.model.TransactionType
import com.example.budgebuddy3.util.PreferencesHelper
import com.example.budgebuddy3.viewmodel.TransactionViewModel
import com.example.budgebuddy3.viewmodel.TransactionViewModelFactory
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.Locale

class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private lateinit var categorySummaryAdapter: CategorySummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupExpensePieChart()
        setupIncomePieChart()
        setupRecyclerView()
        observeTransactions()
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as BudgetApplication).repository
        val preferencesHelper = PreferencesHelper(requireContext())
        val factory = TransactionViewModelFactory(repository, preferencesHelper, requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]
    }

    private fun setupExpensePieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            centerText = "Expenses by Category"
        }
    }

    private fun setupIncomePieChart() {
        binding.incomePieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            centerText = "Income by Category"
        }
    }

    private fun setupRecyclerView() {
        categorySummaryAdapter = CategorySummaryAdapter()
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categorySummaryAdapter
        }
    }

    private fun observeTransactions() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            updateChartData(transactions)
            updateSummaryTexts(transactions)
        }
    }

    private fun updateChartData(transactions: List<Transaction>) {
        // Update Expenses Chart
        val expensesByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        val totalExpenses = expensesByCategory.values.sum()

        val expenseEntries = expensesByCategory.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val expenseDataSet = PieDataSet(expenseEntries, "Expense Categories").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueFormatter = PercentFormatter(binding.pieChart)
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.4f
            valueLineColor = Color.BLACK
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }

        binding.pieChart.data = PieData(expenseDataSet)
        binding.pieChart.invalidate()

        // Update Income Chart
        val incomeByCategory = transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        val totalIncome = incomeByCategory.values.sum()

        val incomeEntries = incomeByCategory.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val incomeDataSet = PieDataSet(incomeEntries, "Income Categories").apply {
            colors = ColorTemplate.PASTEL_COLORS.toList()
            valueFormatter = PercentFormatter(binding.incomePieChart)
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.4f
            valueLineColor = Color.BLACK
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }

        binding.incomePieChart.data = PieData(incomeDataSet)
        binding.incomePieChart.invalidate()

        // Update category summaries for expenses
        val categorySummaries = expensesByCategory.map { (category, amount) ->
            CategorySummary(
                category = category,
                amount = amount,
                percentage = (amount / totalExpenses * 100).toInt()
            )
        }.sortedByDescending { it.amount }

        categorySummaryAdapter.submitList(categorySummaries)
    }

    private fun updateSummaryTexts(transactions: List<Transaction>) {
        val totalExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        val savings = totalIncome - totalExpenses

        binding.totalExpensesText.text = getString(R.string.monthly_expenses, currencyFormat.format(totalExpenses))
        binding.totalIncomeText.text = getString(R.string.monthly_income, currencyFormat.format(totalIncome))
        binding.savingsText.text = getString(R.string.monthly_savings, currencyFormat.format(savings))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 