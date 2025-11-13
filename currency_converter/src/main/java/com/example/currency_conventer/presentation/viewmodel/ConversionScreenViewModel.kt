package com.example.currency_conventer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currency_conventer.domain.common.onError
import com.example.currency_conventer.domain.common.onSuccess
import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.domain.model.dataclass.CurrencyConversion
import com.example.currency_conventer.domain.model.dataclass.result.ValidationResult
import com.example.currency_conventer.domain.repository.FxRatesRepository
import com.example.currency_conventer.domain.usecase.ValidateAmountUseCase
import com.example.currency_conventer.presentation.action.ConversionScreenAction
import com.example.currency_conventer.presentation.state.ConversionScreenState
import com.example.currency_conventer.presentation.state.ErrorPanelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ConversionScreenViewModel @Inject constructor(
    private val fxRatesRepository: FxRatesRepository,
    private val validateAmountUseCase: ValidateAmountUseCase
) : ViewModel() {
    private val _screenState = MutableStateFlow(ConversionScreenState.initialValue())
    val screenState = _screenState.asStateFlow()

    private var conversionJob: Job? = null
    private val debounceTime = 300L

    fun onAction(action: ConversionScreenAction) {
        when (action) {
            is ConversionScreenAction.OnSendingAmountInputChange ->
                handleSendingAmountInputChange(action.newText)

            is ConversionScreenAction.OnReceivingAmountInputChange ->
                handleReceivingAmountInputChange(action.newText)

            is ConversionScreenAction.SendingCurrencySelected ->
                handleSendingCurrencySelected(action.currency)

            is ConversionScreenAction.ReceivingCurrencySelected ->
                handleReceivingCurrencySelected(action.currency)

            ConversionScreenAction.SwapClicked -> handleSwapClicked()

            ConversionScreenAction.HideErrorPanelClicked -> _screenState.update {
                it.copy(errorPanelState = it.errorPanelState?.copy(isVisible = false))
            }
        }
    }

    private fun handleSendingAmountInputChange(newText: String) {
        _screenState.update { it.copy(sendingAmount = newText) }
        if (newText.isEmpty())
            _screenState.update { it.copy(receivingAmount = "") }
        else
            clearAndRecalculate()
    }

    private fun handleReceivingAmountInputChange(newText: String) {
        _screenState.update { it.copy(receivingAmount = newText) }

        val amountInputValidationResult = validateAmountUseCase.amountInputValidation(newText)
        if (amountInputValidationResult is ValidationResult.Valid)
            recalculateSendingAmount(newText.toDouble())
    }

    private fun recalculateSendingAmount(newReceivingAmount: Double) {
        fetchExchangeDetails(
            from = screenState.value.receivingCurrency,
            to = screenState.value.sendingCurrency,
            amount = newReceivingAmount,
            onSuccess = { conversion ->
                val calculatedSending = conversion.convertedAmount.toString()

                val limitValidation = validateAmountUseCase.currencyLimitValidation(
                    calculatedSending,
                    screenState.value.sendingCurrency
                )

                _screenState.update {
                    it.copy(
                        sendingAmount = calculatedSending,
                        sendingLimitExceededMessage = if (limitValidation is ValidationResult.Warning)
                            limitValidation.warningMessage else null
                    )
                }
            }
        )
    }

    private fun handleSendingCurrencySelected(currency: Currency) {
        _screenState.update {
            it.copy(
                sendingCurrency = currency,
                ratioText = null
            )
        }
        clearAndRecalculate()
    }

    private fun handleReceivingCurrencySelected(currency: Currency) {
        _screenState.update {
            it.copy(
                receivingCurrency = currency,
                ratioText = null
            )
        }
        clearAndRecalculate()
    }

    private fun handleSwapClicked() {
        _screenState.update {
            it.copy(
                sendingCurrency = it.receivingCurrency,
                receivingCurrency = it.sendingCurrency
            )
        }
        clearAndRecalculate()
    }

    private fun clearAndRecalculate() {
        _screenState.update {
            it.copy(
                sendingLimitExceededMessage = null,
                errorPanelState = null
            )
        }
        validateAndConvertCurrency()
    }

    private fun validateAndConvertCurrency() {
        val validationResult = validateAmountUseCase.fullValidation(
            screenState.value.sendingAmount,
            screenState.value.sendingCurrency
        )

        val onSuccess: ((CurrencyConversion) -> Unit) = { conversionResult ->
            _screenState.update {
                it.copy(
                    receivingAmount = conversionResult.convertedAmount.toString(),
                    ratioText = prepareRatioText(conversionResult),
                    errorPanelState = null
                )
            }
        }

        when (validationResult) {
            is ValidationResult.Invalid -> {
                _screenState.update {
                    it.copy(sendingLimitExceededMessage = validationResult.errorMessage)
                }
            }

            ValidationResult.Valid -> {
                _screenState.update {
                    it.copy(sendingLimitExceededMessage = null)
                }
                fetchExchangeDetails(
                    screenState.value.sendingCurrency,
                    screenState.value.receivingCurrency,
                    screenState.value.sendingAmount.toDouble(),
                    onSuccess
                )
            }

            is ValidationResult.Warning -> {
                _screenState.update {
                    it.copy(sendingLimitExceededMessage = validationResult.warningMessage)
                }
                fetchExchangeDetails(
                    screenState.value.sendingCurrency,
                    screenState.value.receivingCurrency,
                    screenState.value.sendingAmount.toDouble(),
                    onSuccess
                )
            }
        }
    }

    private fun fetchExchangeDetails(
        from: Currency,
        to: Currency,
        amount: Double,
        onSuccess: (CurrencyConversion) -> Unit
    ) {
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            delay(debounceTime)
            fxRatesRepository.getCurrencyConversion(from, to, amount)
                .onSuccess { result ->
                    onSuccess(result)
                }
                .onError { exception, message ->
                    if (exception is IOException) {
                        _screenState.update {
                            it.copy(
                                errorPanelState = ErrorPanelState(
                                    title = "No Network",
                                    message = "Check your internet connection",
                                    isVisible = true
                                )
                            )
                        }
                    } else {
                        _screenState.update {
                            it.copy(
                                errorPanelState = ErrorPanelState(
                                    title = "Unexpected error",
                                    message = "Please try again later",
                                    isVisible = true
                                )
                            )
                        }
                    }
                }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun prepareRatioText(currencyConversion: CurrencyConversion): String {
        val roundedRate = String.format("%.2f", currencyConversion.rate)
        return "1 ${currencyConversion.from.code} = $roundedRate ${currencyConversion.to.code}"
    }

}