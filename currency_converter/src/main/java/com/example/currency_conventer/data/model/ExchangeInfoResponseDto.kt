package com.example.currency_conventer.data.model

import com.google.gson.annotations.SerializedName

data class ExchangeInfoResponseDto(
    @SerializedName("rate")
    val rate: Double,
    @SerializedName("toAmount")
    val convertedAmount: Double
)
