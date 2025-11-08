package com.example.currency_conventer.domain.usecase

import com.example.currency_conventer.domain.exception.ValidationException
import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.repository.FakeFxRatesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConvertCurrencyUseCaseTest {

    private lateinit var repository: FakeFxRatesRepository
    private lateinit var validateUseCase: ValidateAmountUseCase
    private lateinit var convertUseCase: ConvertCurrencyUseCase

    @Before
    fun setup() {
        repository = FakeFxRatesRepository()
        validateUseCase = ValidateAmountUseCase()
        convertUseCase = ConvertCurrencyUseCase(validateUseCase, repository)
    }

    @Test
    fun `Converting default PLN to UAH, valid amount, returns success with correct calculation`() =
        runTest {
            val result = convertUseCase(
                fromCurrency = CurrencyDefaults.PLN,
                toCurrency = CurrencyDefaults.UAH,
                amount = 300.0
            )

            assertTrue(result.isSuccess)
            val conversion = result.getOrNull()!!
            assertEquals(300.0, conversion.fromAmount, 0.01)
            assertEquals("PLN", conversion.fromCurrency.code)
            assertEquals("UAH", conversion.toCurrency.code)
            assertEquals(
                conversion.fromAmount * conversion.exchangeRate,
                conversion.toAmount,
                0.01
            )
        }

    @Test
    fun `Converting with amount exceeding limit, returns ValidationException`() = runTest {
        val result = convertUseCase(
            fromCurrency = CurrencyDefaults.PLN,
            toCurrency = CurrencyDefaults.EUR,
            amount = 25000.0
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `Converting with zero amount, returns ValidationException`() = runTest {
        val result = convertUseCase(
            fromCurrency = CurrencyDefaults.PLN,
            toCurrency = CurrencyDefaults.UAH,
            amount = 0.0
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }
}