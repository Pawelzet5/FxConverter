package com.example.currency_conventer.domain.model.dataclass.result

sealed class ValidationResult {
    object Valid : ValidationResult()
    object Invalid : ValidationResult()
    data class Warning(val warningMessage: String) : ValidationResult()
}