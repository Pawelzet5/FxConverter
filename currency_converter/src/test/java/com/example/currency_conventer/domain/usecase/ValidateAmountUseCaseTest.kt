package com.example.currency_conventer.domain.usecase

import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.result.ValidationResult
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateAmountUseCaseTest {

    private lateinit var useCase: ValidateAmountUseCase

    @Before
    fun setup() {
        useCase = ValidateAmountUseCase()
    }

    @Test
    fun `Validating amount for PLN, zero amount, returns Invalid`() {
        val result = useCase(0.0, CurrencyDefaults.PLN)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("greater than 0"))
    }

    @Test
    fun `Validating amount for PLN, negative amount, returns Invalid`() {
        val result = useCase(-100.0, CurrencyDefaults.PLN)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `Validating amount for PLN, valid amount within PLN limit, returns Valid`() {
        val result = useCase(10000.0, CurrencyDefaults.PLN)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validating amount for PLN, amount at exact PLN limit, returns Valid`() {
        val result = useCase(CurrencyDefaults.PLN.sendingLimit, CurrencyDefaults.PLN)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validating amount for PLN, amount exceeding PLN limit, returns Invalid`() {
        val result = useCase(CurrencyDefaults.PLN.sendingLimit + 1, CurrencyDefaults.PLN)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("20000"))
    }

    @Test
    fun `Validating amount for PLN, very small positive amount, returns Valid`() {
        val result = useCase(0.01, CurrencyDefaults.PLN)
        assertTrue(result is ValidationResult.Valid)
    }
}