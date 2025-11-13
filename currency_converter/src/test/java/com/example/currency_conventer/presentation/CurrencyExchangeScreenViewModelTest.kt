package com.example.currency_conventer.presentation

import app.cash.turbine.test
import com.example.currency_conventer.domain.common.Result
import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.CurrencyConversion
import com.example.currency_conventer.domain.repository.FxRatesRepository
import com.example.currency_conventer.domain.usecase.ValidateAmountUseCase
import com.example.currency_conventer.presentation.action.CurrencyExchangeScreenAction
import com.example.currency_conventer.presentation.state.CurrencyInputType
import com.example.currency_conventer.presentation.state.ErrorPanelState
import com.example.currency_conventer.presentation.viewmodel.CurrencyExchangeScreenViewModel
import com.example.currency_converter.R
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyExchangeScreenViewModelTest {
    private lateinit var viewModel: CurrencyExchangeScreenViewModel
    private lateinit var fxRatesRepository: FxRatesRepository
    private val validateAmountUseCase = ValidateAmountUseCase()
    private val testDispatcher = StandardTestDispatcher()

    private val plnCurrency = CurrencyDefaults.PLN
    private val uahCurrency = CurrencyDefaults.UAH

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fxRatesRepository = mockk()
        viewModel = CurrencyExchangeScreenViewModel(fxRatesRepository, validateAmountUseCase)

        val defaultConversion = CurrencyConversion(
            plnCurrency,
            uahCurrency,
            11.52,
            CurrencyDefaults.DEFAULT_AMOUNT * 11.52
        )
        coEvery {
            fxRatesRepository.getCurrencyConversion(plnCurrency, uahCurrency, 100.0)
        } returns Result.Success(defaultConversion)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Sending amount input change, valid within limit, converts successfully`() = runTest {
        viewModel.screenState.test {
            // Given
            val conversion = CurrencyConversion(plnCurrency, uahCurrency, 0.235, 23.5)
            coEvery {
                fxRatesRepository.getCurrencyConversion(plnCurrency, uahCurrency, 100.0)
            } returns Result.Success(conversion)

            // When
            viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
            advanceTimeBy(301)

            coVerify {
                fxRatesRepository.getCurrencyConversion(
                    viewModel.screenState.value.sendingCurrency,
                    viewModel.screenState.value.receivingCurrency,
                    viewModel.screenState.value.sendingAmount.toDouble()
                )
            }

            // Then
            val state = expectMostRecentItem()
            assertEquals("100", state.sendingAmount)
            assertEquals("23.5", state.receivingAmount)
            assertNotNull(state.exchangeRatio)
            assertEquals(0.235, state.exchangeRatio!!, 0.001)
            assertNull(state.sendingLimitExceededMessage)
        }
    }

    @Test
    fun `Sending amount input change, exceeds limit, shows warning and converts`() = runTest {
        // Given
        val conversion = CurrencyConversion(plnCurrency, uahCurrency, 0.235, 5875.0)
        coEvery {
            fxRatesRepository.getCurrencyConversion(plnCurrency, uahCurrency, 25000.0)
        } returns Result.Success(conversion)

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("25000"))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals("25000", state.sendingAmount)
            assertEquals("5875.0", state.receivingAmount)
            assertEquals("Maximum sending amount: 20000.0 PLN", state.sendingLimitExceededMessage)
            assertNotNull(state.exchangeRatio)
        }
    }

    @Test
    fun `Sending amount input change, invalid format, blocks conversion`() =
        runTest {
            // When
            viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("abc"))
            advanceUntilIdle()

            // Then
            viewModel.screenState.test {
                val state = expectMostRecentItem()
                assertEquals("abc", state.sendingAmount)
                assertNull(state.sendingLimitExceededMessage)
                assertNull(state.exchangeRatio)
            }
        }

    @Test
    fun `Sending amount input change, empty string, clears receiving amount`() = runTest {
        // Given
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange(""))
        advanceUntilIdle()

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals("", state.sendingAmount)
            assertEquals("", state.receivingAmount)
        }
    }

    @Test
    fun `Sending amount input change, network error, shows error panel`() = runTest {
        // Given
        coEvery {
            fxRatesRepository.getCurrencyConversion(plnCurrency, uahCurrency, 100.0)
        } returns Result.Error(IOException(), "Network unavailable")

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals(
                ErrorPanelState(
                    titleResId = R.string.error_no_network,
                    messageResId = R.string.error_no_network_message,
                    isVisible = true
                ),
                state.errorPanelState
            )
        }
    }

    @Test
    fun `Dismiss error panel, error panel hidden`() = runTest {
        // Given
        coEvery {
            fxRatesRepository.getCurrencyConversion(plnCurrency, uahCurrency, 100.0)
        } returns Result.Error(IOException(), "Network unavailable")
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)

        // WHEN
        viewModel.onAction(CurrencyExchangeScreenAction.DismissErrorPanelClicked)

        // Then
        viewModel.screenState.test {
            assertFalse(expectMostRecentItem().errorPanelState?.isVisible!!)
        }
    }

    @Test
    fun `Rapid sending amount input changes, only last request processed`() = runTest {
        // Given
        val conversion = CurrencyConversion(plnCurrency, uahCurrency, 0.235, 23.5)
        coEvery {
            fxRatesRepository.getCurrencyConversion(any(), any(), any())
        } returns Result.Success(conversion)

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("1"))
        advanceTimeBy(100)
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("10"))
        advanceTimeBy(100)
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals("100", state.sendingAmount)
            assertEquals("23.5", state.receivingAmount)
        }
    }

    @Test
    fun `Sending amount input change, previous input invalid, clears error`() = runTest {
        // Given
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("abc"))
        advanceUntilIdle()
        val conversion = CurrencyConversion(plnCurrency, uahCurrency, 0.235, 23.5)
        coEvery {
            fxRatesRepository.getCurrencyConversion(plnCurrency, uahCurrency, 100.0)
        } returns Result.Success(conversion)

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertNull(state.sendingLimitExceededMessage)
            assertEquals("23.5", state.receivingAmount)
        }
    }

    @Test
    fun `Receiving amount input change, valid input, calculates sending amount`() = runTest {
        // Given
        val conversion = CurrencyConversion(uahCurrency, plnCurrency, 4.25, 425.0)
        coEvery {
            fxRatesRepository.getCurrencyConversion(uahCurrency, plnCurrency, 100.0)
        } returns Result.Success(conversion)

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnReceivingAmountInputChange("100"))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals("100", state.receivingAmount)
            assertEquals("425.0", state.sendingAmount)
            assertNull(state.sendingLimitExceededMessage)
        }

        coVerify {
            fxRatesRepository.getCurrencyConversion(
                viewModel.screenState.value.receivingCurrency,
                viewModel.screenState.value.sendingCurrency,
                100.0
            )
        }
    }

    @Test
    fun `Receiving amount input change, calculated sending exceeds limit, shows warning`() = runTest {
        // Given
        val conversion = CurrencyConversion(uahCurrency, plnCurrency, 4.25, 21250.0)
        coEvery {
            fxRatesRepository.getCurrencyConversion(uahCurrency, plnCurrency, 5000.0)
        } returns Result.Success(conversion)

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnReceivingAmountInputChange("5000"))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals("5000", state.receivingAmount)
            assertEquals("21250.0", state.sendingAmount)
            assertEquals("Maximum sending amount: 20000.0 PLN", state.sendingLimitExceededMessage)
        }

        coVerify {
            fxRatesRepository.getCurrencyConversion(
                viewModel.screenState.value.receivingCurrency,
                viewModel.screenState.value.sendingCurrency,
                5000.0
            )
        }
    }

    @Test
    fun `Receiving amount input change, invalid format, does not calculate`() = runTest {
        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnReceivingAmountInputChange("abc"))
        advanceUntilIdle()

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals("abc", state.receivingAmount)
            assertEquals(CurrencyDefaults.DEFAULT_AMOUNT.toString(), state.sendingAmount)
        }
    }

    @Test
    fun `Select currency click with SENDING type, opens currency selection dialog`() = runTest {
        // When
        viewModel.onAction(CurrencyExchangeScreenAction.SelectCurrencyClicked(currencyInputType = CurrencyInputType.SENDING))

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals(CurrencyInputType.SENDING, state.currencySelectionDialogState.currencyInputType)
            assertTrue(state.currencySelectionDialogState.isCurrencySelectionDialogOpen)
        }
    }

    @Test
    fun `Select currency click with RECEIVING type, opens currency selection dialog`() = runTest {
        // When
        viewModel.onAction(CurrencyExchangeScreenAction.SelectCurrencyClicked(currencyInputType = CurrencyInputType.RECEIVING))

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals(CurrencyInputType.RECEIVING, state.currencySelectionDialogState.currencyInputType)
            assertTrue(state.currencySelectionDialogState.isCurrencySelectionDialogOpen)
        }
    }

    @Test
    fun `Select SENDING type currency, triggers conversion`() = runTest {
        // Given
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)
        val eurCurrency = CurrencyDefaults.EUR
        val conversion = CurrencyConversion(eurCurrency, uahCurrency, 42.50, 4250.0)
        coEvery {
            fxRatesRepository.getCurrencyConversion(eurCurrency, uahCurrency, 100.0)
        } returns Result.Success(conversion)
        viewModel.onAction(CurrencyExchangeScreenAction.SelectCurrencyClicked(currencyInputType = CurrencyInputType.SENDING))

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnCurrencySelected(eurCurrency, CurrencyInputType.SENDING))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals(eurCurrency, state.sendingCurrency)
            assertNotNull(state.exchangeRatio)
            assertEquals(42.5, state.exchangeRatio!!, 0.001)
        }
    }

    @Test
    fun `Select RECEVEIVING type currency, triggers conversion`() = runTest {
        // Given
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)
        val gbpCurrency = CurrencyDefaults.GBP
        val conversion = CurrencyConversion(plnCurrency, gbpCurrency, 0.19, 19.0)
        coEvery {
            fxRatesRepository.getCurrencyConversion(plnCurrency, gbpCurrency, 100.0)
        } returns Result.Success(conversion)
        viewModel.onAction(CurrencyExchangeScreenAction.SelectCurrencyClicked(currencyInputType = CurrencyInputType.RECEIVING))

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.OnCurrencySelected(gbpCurrency, CurrencyInputType.RECEIVING))
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals(gbpCurrency, state.receivingCurrency)
        }
    }

    @Test
    fun `Swap currencies, swaps both currencies and amounts`() = runTest {
        // Given
        val plnToUahConversion = CurrencyConversion(plnCurrency, uahCurrency, 0.235, 23.5)
        val uahToPlnConversion = CurrencyConversion(uahCurrency, plnCurrency, 4.2553, 425.53)
        coEvery {
            fxRatesRepository.getCurrencyConversion(plnCurrency, uahCurrency, any())
        } returns Result.Success(plnToUahConversion)
        coEvery {
            fxRatesRepository.getCurrencyConversion(uahCurrency, plnCurrency, any())
        } returns Result.Success(uahToPlnConversion)
        viewModel.onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange("100"))
        advanceTimeBy(301)

        // When
        viewModel.onAction(CurrencyExchangeScreenAction.SwapClicked)
        advanceTimeBy(301)

        // Then
        viewModel.screenState.test {
            val state = expectMostRecentItem()
            assertEquals(uahCurrency, state.sendingCurrency)
            assertEquals(plnCurrency, state.receivingCurrency)
            assertEquals("100", state.sendingAmount)
            assertEquals("425.53", state.receivingAmount)
        }
    }
}
