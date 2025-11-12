package com.example.currency_conventer.domain.usecase

import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.result.ValidationResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ValidateAmountUseCaseTest {
    private lateinit var validateAmountUseCase: ValidateAmountUseCase
    private val eurCurrency = CurrencyDefaults.EUR

    @Before
    fun setup() {
        validateAmountUseCase = ValidateAmountUseCase()
    }

    @Test
    fun `Validate amount input, valid positive number, returns valid`() {
        val result = validateAmountUseCase.amountInputValidation("100")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validate amount input, valid decimal number, returns valid`() {
        val result = validateAmountUseCase.amountInputValidation("123.45")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validate amount input, zero, returns valid`() {
        val result = validateAmountUseCase.amountInputValidation("0")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validate amount input, negative number, returns invalid`() {
        val result = validateAmountUseCase.amountInputValidation("-50")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Amount cannot be negative", (result as ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun `Validate amount input, non-numeric text, returns invalid`() {
        val result = validateAmountUseCase.amountInputValidation("abc")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Amount must be a number", (result as ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun `Validate amount input, empty string, returns invalid`() {
        val result = validateAmountUseCase.amountInputValidation("")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Amount must be a number", (result as ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun `Validate currency limit, amount within limit, returns valid`() {
        val result = validateAmountUseCase.currencyLimitValidation("5000", eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validate currency limit, amount equals limit, returns valid`() {
        val result = validateAmountUseCase.currencyLimitValidation("10000", eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validate currency limit, amount exceeds limit, returns warning`() {
        val result = validateAmountUseCase.currencyLimitValidation("15000", eurCurrency)
        assertTrue(result is ValidationResult.Warning)
        assertEquals(
            "Maximum sending amount: 10000.0 EUR",
            (result as ValidationResult.Warning).warningMessage
        )
    }

    @Test
    fun `Validate currency limit, amount slightly exceeds limit, returns warning`() {
        val result = validateAmountUseCase.currencyLimitValidation("10000.01", eurCurrency)
        assertTrue(result is ValidationResult.Warning)
        assertEquals(
            "Maximum sending amount: 10000.0 EUR",
            (result as ValidationResult.Warning).warningMessage
        )
    }

    @Test
    fun `Full validation, valid amount within limit, returns valid`() {
        val result = validateAmountUseCase.fullValidation("5000", eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Full validation, valid amount exceeds limit, returns warning`() {
        val result = validateAmountUseCase.fullValidation("15000", eurCurrency)
        assertTrue(result is ValidationResult.Warning)
        assertEquals(
            "Maximum sending amount: 10000.0 EUR",
            (result as ValidationResult.Warning).warningMessage
        )
    }

    @Test
    fun `Full validation, negative number, returns invalid`() {
        val result = validateAmountUseCase.fullValidation("-50", eurCurrency)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Amount cannot be negative", (result as ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun `Full validation, non-numeric text, returns invalid`() {
        val result = validateAmountUseCase.fullValidation("abc", eurCurrency)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Amount must be a number", (result as ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun `Full validation, negative number exceeding limit, returns invalid not warning`() {
        val result = validateAmountUseCase.fullValidation("-15000", eurCurrency)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Amount cannot be negative", (result as ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun `Full validation, amount at exact limit, returns valid`() {
        val result = validateAmountUseCase.fullValidation("10000", eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }
}