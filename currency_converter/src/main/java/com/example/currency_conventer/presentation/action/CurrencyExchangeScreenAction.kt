package com.example.currency_conventer.presentation.action

import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.presentation.state.CurrencyInputType

sealed interface CurrencyExchangeScreenAction {
    data class OnSendingAmountInputChange(val newText: String): CurrencyExchangeScreenAction
    data class OnReceivingAmountInputChange(val newText: String): CurrencyExchangeScreenAction
    data class OnCurrencySelected(val currency: Currency, val currencyInputType: CurrencyInputType): CurrencyExchangeScreenAction
    data class SelectCurrencyClicked(val currencyInputType: CurrencyInputType): CurrencyExchangeScreenAction
    data object SelectCurrencyDialogDismissed: CurrencyExchangeScreenAction
    data object SwapClicked: CurrencyExchangeScreenAction
    data object DismissErrorPanelClicked: CurrencyExchangeScreenAction
}