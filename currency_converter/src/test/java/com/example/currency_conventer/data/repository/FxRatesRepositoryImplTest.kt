package com.example.currency_conventer.data.repository

import com.example.currency_conventer.data.api.FxRatesService
import com.example.currency_conventer.data.model.FxRateResponseDto
import com.example.currency_conventer.domain.common.Result
import com.example.currency_conventer.domain.model.CurrencyDefaults
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FxRatesRepositoryImplTest {

    private lateinit var repository: FxRatesRepositoryImpl
    private lateinit var fxRatesService: FxRatesService
    private val testDispatcher = StandardTestDispatcher()

    private val eurCurrency = CurrencyDefaults.EUR
    private val uahCurrency = CurrencyDefaults.UAH
    private val testAmount = 100.0

    @Before
    fun setup() {
        fxRatesService = mockk()
        repository = FxRatesRepositoryImpl(
            fxRatesService = fxRatesService,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `Get currency conversion, api call successful, returns success with correct data`() =
        runTest(testDispatcher) {
            // Given
            val mockResponse = FxRateResponseDto(
                rate = 1.15694,
                convertedAmount = 115.69
            )
            coEvery {
                fxRatesService.getExchangeInfo("EUR", "UAH", testAmount)
            } returns mockResponse

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = testAmount
            )

            // Then
            assertTrue(result is Result.Success)
            val exchangeRate = (result as Result.Success).data
            assertEquals(eurCurrency, exchangeRate.from)
            assertEquals(uahCurrency, exchangeRate.to)
            assertEquals(1.15694, exchangeRate.rate, 0.00001)
            assertEquals(115.69, exchangeRate.convertedAmount, 0.01)

            coVerify(exactly = 1) {
                fxRatesService.getExchangeInfo("EUR", "UAH", testAmount)
            }
        }

    @Test
    fun `Get currency conversion, different currency pair, returns success with correct rate`() =
        runTest(testDispatcher) {
            // Given
            val plnCurrency = CurrencyDefaults.PLN
            val mockResponse = FxRateResponseDto(
                rate = 4.25,
                convertedAmount = 42.5
            )
            coEvery {
                fxRatesService.getExchangeInfo("EUR", "PLN", 10.0)
            } returns mockResponse

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = plnCurrency,
                amount = 10.0
            )

            // Then
            assertTrue(result is Result.Success)
            val exchangeRate = (result as Result.Success).data
            assertEquals(4.25, exchangeRate.rate, 0.00001)
            assertEquals(42.5, exchangeRate.convertedAmount, 0.01)
        }

    @Test
    fun `Get currency conversion, decimal amount, returns success with correct conversion`() =
        runTest(testDispatcher) {
            // Given
            val decimalAmount = 123.45
            val mockResponse = FxRateResponseDto(
                rate = 1.15694,
                convertedAmount = 142.82
            )
            coEvery {
                fxRatesService.getExchangeInfo("EUR", "UAH", decimalAmount)
            } returns mockResponse

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = decimalAmount
            )

            // Then
            assertTrue(result is Result.Success)
            assertEquals(142.82, (result as Result.Success).data.convertedAmount, 0.01)
        }

    @Test
    fun `Get currency conversion, IO exception thrown, returns error with network message`() =
        runTest(testDispatcher) {
            // Given
            coEvery {
                fxRatesService.getExchangeInfo(any(), any(), any())
            } throws IOException("Network unavailable")

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = testAmount
            )

            // Then
            assertTrue(result is Result.Error)
            val error = result as Result.Error
            assertTrue(error.exception is IOException)
            assertEquals("Network error: Unable to fetch exchange rate", error.message)
        }

    @Test
    fun `Get currency conversion, connection timeout, returns error with IO exception`() =
        runTest(testDispatcher) {
            // Given
            coEvery {
                fxRatesService.getExchangeInfo(any(), any(), any())
            } throws IOException("timeout")

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = testAmount
            )

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is IOException)
        }

    @Test
    fun `Get currency conversion, http exception 404, returns error with server message`() =
        runTest(testDispatcher) {
            // Given
            val mockErrorResponse = Response.error<FxRateResponseDto>(
                404,
                "Not found".toResponseBody(null)
            )
            coEvery {
                fxRatesService.getExchangeInfo(any(), any(), any())
            } throws HttpException(mockErrorResponse)

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = testAmount
            )

            // Then
            assertTrue(result is Result.Error)
            val error = result as Result.Error
            assertTrue(error.exception is HttpException)
            assertEquals("Server error: 404", error.message)
        }

    @Test
    fun `Get currency conversion, http exception 500, returns error with server code`() =
        runTest(testDispatcher) {
            // Given
            val mockErrorResponse = Response.error<FxRateResponseDto>(
                500,
                "Internal server error".toResponseBody(null)
            )
            coEvery {
                fxRatesService.getExchangeInfo(any(), any(), any())
            } throws HttpException(mockErrorResponse)

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = testAmount
            )

            // Then
            assertTrue(result is Result.Error)
            val error = result as Result.Error
            assertEquals("Server error: 500", error.message)
        }

    @Test
    fun `Get currency conversion, unexpected exception, returns error with exception message`() =
        runTest(testDispatcher) {
            // Given
            val unexpectedException = IllegalStateException("Unexpected state")
            coEvery {
                fxRatesService.getExchangeInfo(any(), any(), any())
            } throws unexpectedException

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = testAmount
            )

            // Then
            assertTrue(result is Result.Error)
            val error = result as Result.Error
            assertTrue(error.exception is IllegalStateException)
            assertEquals("Unexpected error: Unexpected state", error.message)
        }

    @Test
    fun `Get supported currencies, when called, returns non-empty list`() {
        // When
        val currencies = repository.getSupportedCurrencies()

        // Then
        assertNotNull(currencies)
        assertTrue(currencies.isNotEmpty())
    }

    @Test
    fun `Get supported currencies, when called, includes EUR and UAH`() {
        // When
        val currencies = repository.getSupportedCurrencies()

        // Then
        assertTrue(currencies.any { it.code == "EUR" })
        assertTrue(currencies.any { it.code == "UAH" })
    }

    @Test
    fun `Get currency by code, code exists, returns correct currency`() {
        // When
        val euroCurrency = repository.getCurrencyByCode("EUR")

        // Then
        assertNotNull(euroCurrency)
        assertEquals("EUR", euroCurrency?.code)
        assertEquals("Euro", euroCurrency?.name)
    }

    @Test
    fun `Get currency conversion, zero amount, returns success with zero conversion`() =
        runTest(testDispatcher) {
            // Given
            val mockResponse = FxRateResponseDto(
                rate = 1.15694,
                convertedAmount = 0.0
            )
            coEvery {
                fxRatesService.getExchangeInfo("EUR", "UAH", 0.0)
            } returns mockResponse

            // When
            val result = repository.getCurrencyConversion(
                from = eurCurrency,
                to = uahCurrency,
                amount = 0.0
            )

            // Then
            assertTrue(result is Result.Success)
            assertEquals(0.0, (result as Result.Success).data.convertedAmount, 0.01)
        }
}
