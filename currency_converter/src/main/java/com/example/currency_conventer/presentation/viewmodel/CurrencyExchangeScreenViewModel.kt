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
import com.example.currency_conventer.presentation.action.CurrencyExchangeScreenAction
import com.example.currency_conventer.presentation.state.*
import com.example.currency_converter.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CurrencyExchangeScreenViewModel @Inject constructor(
    private val fxRatesRepository: FxRatesRepository,
    private val validateAmountUseCase: ValidateAmountUseCase
) : ViewModel() {

    private val _screenState = MutableStateFlow(CurrencyExchangeScreenState.initialValue())
    val screenState = _screenState.asStateFlow()
    private var conversionJob: Job? = null
    private val debounceTime = 300L

    fun onAction(action: CurrencyExchangeScreenAction) {
        when (action) {
            is CurrencyExchangeScreenAction.OnSendingAmountInputChange ->
                handleSendingAmountInputChange(action.newText)

            is CurrencyExchangeScreenAction.OnReceivingAmountInputChange ->
                handleReceivingAmountInputChange(action.newText)

            is CurrencyExchangeScreenAction.OnCurrencySelected ->
                handleCurrencySelected(action.currency)

            CurrencyExchangeScreenAction.SwapClicked -> handleSwapClicked()

            CurrencyExchangeScreenAction.DismissErrorPanelClicked -> _screenState.update {
                it.copy(errorPanelState = it.errorPanelState?.copy(isVisible = false))
            }

            is CurrencyExchangeScreenAction.SelectCurrencyClicked -> _screenState.update {
                it.copy(
                    currencySelectionDialogState = CurrencySelectionDialogState(
                        isCurrencySelectionDialogOpen = true,
                        isSendingCurrencySelection = action.isSendingCurrencySelection
                    )
                )
            }

            CurrencyExchangeScreenAction.SelectCurrencyDialogDismissed -> _screenState.update {
                it.copy(
                    currencySelectionDialogState = CurrencySelectionDialogState(
                        isCurrencySelectionDialogOpen = false
                    )
                )
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
            })
    }

    private fun handleCurrencySelected(currency: Currency) {
        val isSendingCurrencySelected =
            screenState.value.currencySelectionDialogState.isSendingCurrencySelection
        val sendingCurrency =
            if (isSendingCurrencySelected) currency else screenState.value.sendingCurrency
        val receivingCurrency =
            if (isSendingCurrencySelected) screenState.value.receivingCurrency else currency

        _screenState.update {
            it.copy(
                sendingCurrency = sendingCurrency,
                receivingCurrency = receivingCurrency,
                exchangeRatio = null
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
                    exchangeRatio = conversionResult.rate,
                    errorPanelState = null
                )
            }
        }

        when (validationResult) {
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

            else -> Unit
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
                                    titleResId = R.string.error_no_network,
                                    messageResId = R.string.error_no_network_message,
                                    isVisible = true
                                )
                            )
                        }
                    } else {
                        _screenState.update {
                            it.copy(
                                errorPanelState = ErrorPanelState(
                                    titleResId = R.string.error_unexpected,
                                    messageResId = R.string.error_unexpected_message,
                                    isVisible = true
                                )
                            )
                        }
                    }
                }
        }
    }
}