package com.example.currency_conventer.presentation.action

import com.example.currency_conventer.domain.model.dataclass.Currency

sealed interface ConversionScreenAction {
    data class OnSendingAmountInputChange(val newText: String): ConversionScreenAction
    data class OnReceivingAmountInputChange(val newText: String): ConversionScreenAction
    data class OnCurrencySelected(val currency: Currency): ConversionScreenAction
    data class SelectCurrencyClicked(val isSendingCurrencySelection: Boolean): ConversionScreenAction
    data object SelectCurrencyDialogDismissed: ConversionScreenAction
    data object SwapClicked: ConversionScreenAction
    data object DismissErrorPanelClicked: ConversionScreenAction
}