package com.example.currency_conventer.domain.repository

import com.example.currency_conventer.domain.common.Result
import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.domain.model.dataclass.CurrencyConversion

class FakeFxRatesRepository : FxRatesRepository {
    private val mockRates = mapOf(
        "PLN-UAH" to 10.0,
        "PLN-EUR" to 0.23,
        "PLN-GBP" to 0.20,
        "EUR-PLN" to 4.35,
        "EUR-UAH" to 43.5,
        "EUR-GBP" to 0.87,
        "GBP-PLN" to 5.0,
        "GBP-EUR" to 1.15,
        "GBP-UAH" to 50.0,
        "UAH-PLN" to 0.10,
        "UAH-EUR" to 0.023,
        "UAH-GBP" to 0.020
    )

    override suspend fun getCurrencyConversion(
        from: Currency,
        to: Currency,
        amount: Double
    ): Result<CurrencyConversion> {
        val key = "${from.code}-${to.code}"
        val rate = mockRates[key] ?: 1.0
        return Result.Success(
            CurrencyConversion(
                from = from,
                to = to,
                rate = rate,
                convertedAmount = amount * rate
            )
        )
    }

    override fun getSupportedCurrencies(): List<Currency> {
        return CurrencyDefaults.SUPPORTED_CURRENCIES
    }

    override fun getCurrencyByCode(code: String): Currency? {
        return CurrencyDefaults.SUPPORTED_CURRENCIES.find { it.code == code }
    }
}
