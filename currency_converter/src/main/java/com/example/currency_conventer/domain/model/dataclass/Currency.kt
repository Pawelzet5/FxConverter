package com.example.currency_conventer.domain.model.dataclass

data class Currency(
    val code: String,
    val nameResId: Int,
    val countryNameResId: Int,
    val sendingLimit: Double
)