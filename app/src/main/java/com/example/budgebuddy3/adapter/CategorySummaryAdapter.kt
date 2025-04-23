package com.example.budgebuddy3.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgebuddy3.R
import com.example.budgebuddy3.model.CategorySummary
import com.example.budgebuddy3.util.CurrencyHelper
import com.example.budgebuddy3.util.PreferencesHelper

class CategorySummaryAdapter(private val context: Context) : ListAdapter<CategorySummary, CategorySummaryAdapter.ViewHolder>(DiffCallback) {

    private val preferencesHelper = PreferencesHelper(context)
    private var currencyFormat = CurrencyHelper.getCurrencyFormatter(preferencesHelper.currency)

    fun updateCurrencyFormat() {
        currencyFormat = CurrencyHelper.getCurrencyFormatter(preferencesHelper.currency)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val amount: TextView = itemView.findViewById(R.id.amountTextView)
        private val percentage: TextView = itemView.findViewById(R.id.percentageTextView)

        fun bind(item: CategorySummary) {
            categoryName.text = item.category
            amount.text = currencyFormat.format(item.amount)
            percentage.text = "${item.percentage}%"
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CategorySummary>() {
        override fun areItemsTheSame(oldItem: CategorySummary, newItem: CategorySummary): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategorySummary, newItem: CategorySummary): Boolean {
            return oldItem == newItem
        }
    }
} 