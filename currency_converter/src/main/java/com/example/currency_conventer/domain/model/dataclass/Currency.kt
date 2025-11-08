package com.example.currency_conventer.domain.model.dataclass

data class Currency(
    val code: String,
    val name: String,
    val countryName: String,
    val sendingLimit: Double
)