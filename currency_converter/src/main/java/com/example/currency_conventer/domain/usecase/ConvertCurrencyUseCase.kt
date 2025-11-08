package com.example.currency_conventer.domain.usecase

import com.example.currency_conventer.domain.exception.ValidationException
import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.domain.model.dataclass.result.ConversionResult
import com.example.currency_conventer.domain.model.dataclass.result.ValidationResult
import com.example.currency_conventer.domain.repository.FxRatesRepository

class ConvertCurrencyUseCase(
    private val validateAmount: ValidateAmountUseCase,
    private val repository: FxRatesRepository
) {
    suspend operator fun invoke(
        fromCurrency: Currency,
        toCurrency: Currency,
        amount: Double
    ): Result<ConversionResult> {
        val validation = validateAmount(amount, fromCurrency)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(ValidationException(validation.errorMessage))
        }

        return repository.getExchangeRate(fromCurrency, toCurrency, amount)
            .map { rate ->
                ConversionResult(
                    fromAmount = amount,
                    fromCurrency = fromCurrency,
                    toAmount = amount * rate,
                    toCurrency = toCurrency,
                    exchangeRate = rate
                )
            }
    }
}
