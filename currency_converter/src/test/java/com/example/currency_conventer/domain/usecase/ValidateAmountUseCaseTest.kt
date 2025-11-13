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
    fun `Validate amount input, too small number, returns invalid`() {
        val result = validateAmountUseCase.amountInputValidation("0.99")
        assertTrue(result is ValidationResult.Invalid)
    }
    @Test
    fun `Validate amount input, negative number, returns invalid`() {
        val result = validateAmountUseCase.amountInputValidation("-50")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `Validate amount input, non-numeric text, returns invalid`() {
        val result = validateAmountUseCase.amountInputValidation("abc")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `Validate amount input, empty string, returns invalid`() {
        val result = validateAmountUseCase.amountInputValidation("")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `Validate currency limit, amount within limit, returns valid`() {
        val result = validateAmountUseCase.currencyLimitValidation("5000", eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validate currency limit, amount equals limit, returns valid`() {
        val amount = (eurCurrency.sendingLimit).toString()
        val result = validateAmountUseCase.currencyLimitValidation(amount, eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Validate currency limit, amount exceeds limit, returns warning`() {
        val amount = (eurCurrency.sendingLimit + 1).toString()
        val result = validateAmountUseCase.currencyLimitValidation(amount, eurCurrency)
        assertTrue(result is ValidationResult.Warning)
        assertEquals(
            "Maximum sending amount: ${eurCurrency.sendingLimit} EUR",
            (result as ValidationResult.Warning).warningMessage
        )
    }

    @Test
    fun `Validate currency limit, amount slightly exceeds limit, returns warning`() {
        val amount = (eurCurrency.sendingLimit + 0.01).toString()
        val result = validateAmountUseCase.currencyLimitValidation(amount, eurCurrency)
        assertTrue(result is ValidationResult.Warning)
        assertEquals(
            "Maximum sending amount: ${eurCurrency.sendingLimit} EUR",
            (result as ValidationResult.Warning).warningMessage
        )
    }

    @Test
    fun `Full validation, valid amount within limit, returns valid`() {
        val amount = (eurCurrency.sendingLimit - 1).toString()
        val result = validateAmountUseCase.fullValidation(amount, eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `Full validation, valid amount exceeds limit, returns warning`() {
        val amount = (eurCurrency.sendingLimit + 1).toString()
        val result = validateAmountUseCase.fullValidation(amount, eurCurrency)
        assertTrue(result is ValidationResult.Warning)
        assertEquals(
            "Maximum sending amount: ${eurCurrency.sendingLimit} EUR",
            (result as ValidationResult.Warning).warningMessage
        )
    }

    @Test
    fun `Full validation, negative number, returns invalid`() {
        val result = validateAmountUseCase.fullValidation("-50", eurCurrency)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `Full validation, non-numeric text, returns invalid`() {
        val result = validateAmountUseCase.fullValidation("abc", eurCurrency)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `Full validation, negative number exceeding limit, returns invalid not warning`() {
        val amount = (eurCurrency.sendingLimit * -1).toString()
        val result = validateAmountUseCase.fullValidation("-15000", eurCurrency)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `Full validation, amount at exact limit, returns valid`() {
        val amount = (eurCurrency.sendingLimit).toString()
        val result = validateAmountUseCase.fullValidation(amount, eurCurrency)
        assertTrue(result is ValidationResult.Valid)
    }
}