package com.example.currency_conventer.domain.model.dataclass.result

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}