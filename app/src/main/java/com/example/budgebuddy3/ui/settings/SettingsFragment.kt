package com.example.budgebuddy3.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.budgebuddy3.R
import com.example.budgebuddy3.databinding.FragmentSettingsBinding
import com.example.budgebuddy3.util.CurrencyHelper
import com.example.budgebuddy3.util.PreferencesHelper

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesHelper = PreferencesHelper(requireContext())
        
        setupCurrencySpinner()
    }
    
    private fun setupCurrencySpinner() {
        val spinner = binding.currencySpinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            CurrencyHelper.getAvailableCurrencies()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // Set current selection based on saved preference
        val currentCurrency = preferencesHelper.currency
        val position = CurrencyHelper.getAvailableCurrencies().indexOf(currentCurrency)
        if (position >= 0) {
            spinner.setSelection(position)
        }
        
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCurrency = parent?.getItemAtPosition(position) as String
                if (selectedCurrency != preferencesHelper.currency) {
                    preferencesHelper.currency = selectedCurrency
                    // Show feedback to user
                    binding.currencyChangeStatus.text = getString(R.string.currency_updated, selectedCurrency)
                    binding.currencyChangeStatus.visibility = View.VISIBLE
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 