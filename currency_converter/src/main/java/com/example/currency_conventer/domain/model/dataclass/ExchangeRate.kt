package com.example.currency_conventer.domain.model.dataclass

data class ExchangeRate(
    val from: Currency,
    val to: Currency,
    val rate: Float
)
