package com.example.currency_conventer.domain.repository

import com.example.currency_conventer.domain.model.dataclass.Currency

interface FxRatesRepository {
    suspend fun getExchangeRate(
        from: Currency,
        to: Currency,
        amount: Double
    ): Result<Double>

    fun getSupportedCurrencies(): List<Currency>

    fun getCurrencyByCode(code: String): Currency?
}