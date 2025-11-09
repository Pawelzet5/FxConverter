package com.example.currency_conventer.data.repository

import com.example.currency_conventer.domain.common.Result
import com.example.currency_conventer.data.api.FxRatesService
import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.domain.model.dataclass.CurrencyConversion
import com.example.currency_conventer.domain.repository.FxRatesRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


class FxRatesRepositoryImpl @Inject constructor(
    private val fxRatesService: FxRatesService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FxRatesRepository {
    override suspend fun getCurrencyConversion(
        from: Currency,
        to: Currency,
        amount: Double
    ): Result<CurrencyConversion> {
        return withContext(ioDispatcher) {
            try {
                val response = fxRatesService.getExchangeInfo(
                    fromCurrencyCode = from.code,
                    toCurrencyCode = to.code,
                    amount = amount
                )
                Result.Success(
                    CurrencyConversion(
                        from,
                        to,
                        rate = response.rate,
                        convertedAmount = response.convertedAmount
                    )
                )
            } catch (e: IOException) {
                Result.Error(
                    exception = e,
                    message = "Network error: Unable to fetch exchange rate"
                )
            } catch (e: HttpException) {
                Result.Error(
                    exception = e,
                    message = "Server error: ${e.code()}"
                )
            } catch (e: Exception) {
                Result.Error(
                    exception = e,
                    message = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    override fun getSupportedCurrencies(): List<Currency> {
        return CurrencyDefaults.SUPPORTED_CURRENCIES
    }

    override fun getCurrencyByCode(code: String): Currency? {
        return CurrencyDefaults.SUPPORTED_CURRENCIES.find { it.code == code }
    }
}