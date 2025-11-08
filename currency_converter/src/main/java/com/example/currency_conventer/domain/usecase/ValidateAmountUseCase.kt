package com.example.currency_conventer.domain.usecase

import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.domain.model.dataclass.result.ValidationResult

class ValidateAmountUseCase {
    operator fun invoke(amount: Double, currency: Currency): ValidationResult {
        return when {
            amount <= 0 -> ValidationResult.Invalid("Amount must be greater than 0")
            amount > currency.sendingLimit -> ValidationResult.Invalid(
                "Exceeds limit of ${currency.sendingLimit} ${currency.code}"
            )
            else -> ValidationResult.Valid
        }
    }
}