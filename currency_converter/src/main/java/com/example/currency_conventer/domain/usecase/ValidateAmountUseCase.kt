package com.example.currency_conventer.domain.usecase

import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.domain.model.dataclass.result.ValidationResult
import javax.inject.Inject

class ValidateAmountUseCase @Inject constructor() {
    fun fullValidation(amount: String, currency: Currency): ValidationResult {
        val amountInputValidation = amountInputValidation(amount)
        return if (amountInputValidation is ValidationResult.Valid)
            currencyLimitValidation(amount, currency)
        else amountInputValidation
    }

    fun amountInputValidation(amount: String): ValidationResult = try {
        when {
            amount.isEmpty() -> ValidationResult.Invalid
            amount.isBlank() -> ValidationResult.Invalid
            amount.toDouble() < 1 -> ValidationResult.Invalid
            else -> ValidationResult.Valid
        }
    } catch (e: NumberFormatException) {
        ValidationResult.Invalid
    }

    fun currencyLimitValidation(amount: String, currency: Currency): ValidationResult =
        if (amount.toDouble() > currency.sendingLimit)
            ValidationResult.Warning("Maximum sending amount: ${currency.sendingLimit} ${currency.code}")
        else ValidationResult.Valid

}