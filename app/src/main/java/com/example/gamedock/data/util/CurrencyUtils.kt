package com.example.gamedock.data.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    fun format(amount: Double, currencyCode: String = "USD"): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
}
