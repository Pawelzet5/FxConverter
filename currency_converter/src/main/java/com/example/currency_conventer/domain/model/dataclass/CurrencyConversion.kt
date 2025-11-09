package com.example.currency_conventer.domain.model.dataclass

data class CurrencyConversion(
    val from: Currency,
    val to: Currency,
    val rate: Double,
    val convertedAmount: Double
)
