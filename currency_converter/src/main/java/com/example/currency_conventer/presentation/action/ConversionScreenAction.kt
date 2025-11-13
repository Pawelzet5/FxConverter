package com.example.currency_conventer.presentation.action

import com.example.currency_conventer.domain.model.dataclass.Currency

sealed interface ConversionScreenAction {
    data class OnSendingAmountInputChange(val newText: String): ConversionScreenAction
    data class OnReceivingAmountInputChange(val newText: String): ConversionScreenAction
    data class SendingCurrencySelected(val currency: Currency): ConversionScreenAction
    data class ReceivingCurrencySelected(val currency: Currency): ConversionScreenAction
    data object SwapClicked: ConversionScreenAction
    data object HideErrorPanelClicked: ConversionScreenAction
}