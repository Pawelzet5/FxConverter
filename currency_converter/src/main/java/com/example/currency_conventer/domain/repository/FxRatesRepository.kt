package com.example.currency_conventer.domain.repository

import com.example.currency_conventer.domain.common.Result
import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.domain.model.dataclass.CurrencyConversion

interface FxRatesRepository {
    suspend fun getCurrencyConversion(
        from: Currency,
        to: Currency,
        amount: Double
    ): Result<CurrencyConversion>

    fun getSupportedCurrencies(): List<Currency>

    fun getCurrencyByCode(code: String): Currency?
}