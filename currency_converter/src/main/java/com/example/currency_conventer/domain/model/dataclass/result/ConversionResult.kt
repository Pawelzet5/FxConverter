package com.example.currency_conventer.domain.model.dataclass.result

import com.example.currency_conventer.domain.model.dataclass.Currency

data class ConversionResult(
    val fromAmount: Double,
    val fromCurrency: Currency,
    val toAmount: Double,
    val toCurrency: Currency,
    val exchangeRate: Double
)