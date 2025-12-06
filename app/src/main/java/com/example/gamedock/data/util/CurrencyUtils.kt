package com.example.gamedock.data.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    fun format(amount: Double, currencyCode: String = "USD"): String {
        // Format a double as localized currency using the provided ISO code.
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
}
