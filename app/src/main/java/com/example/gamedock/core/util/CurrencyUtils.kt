package com.example.gamedock.core.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility for presenting monetary values in a localized fashion.
 */
object CurrencyUtils {
    fun format(amount: Double, currencyCode: String = "USD"): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
}
