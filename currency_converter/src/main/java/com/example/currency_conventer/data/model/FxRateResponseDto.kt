package com.example.currency_conventer.data.model

import com.google.gson.annotations.SerializedName

data class FxRateResponseDto(
    @SerializedName("rate")
    val rate: Double,
    @SerializedName("toAmount")
    val convertedAmount: Double
)
