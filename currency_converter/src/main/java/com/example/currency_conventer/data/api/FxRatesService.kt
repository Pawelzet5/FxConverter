package com.example.currency_conventer.data.api

import com.example.currency_conventer.data.model.ExchangeInfoResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FxRatesService {
    @GET("fx-rates")
    suspend fun getExchangeInfo(
        @Query("from") fromCurrencyCode: String,
        @Query("to") toCurrencyCode: String,
        @Query("amount") amount: Double
    ): ExchangeInfoResponseDto
}