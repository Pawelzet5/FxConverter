package com.example.currency_conventer.presentation.state

import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.Currency

data class CurrencyExchangeScreenState(
    val availableCurrencies: List<Currency>,
    val sendingCurrency: Currency,
    val sendingAmount: String,
    val receivingCurrency: Currency,
    val receivingAmount: String,
    val ratioText: String?,
    val sendingLimitExceededMessage: String?,
    val errorPanelState: ErrorPanelState? = null,
    val currencySelectionDialogState: CurrencySelectionDialogState = CurrencySelectionDialogState()
) {
    companion object {
        fun initialValue() = CurrencyExchangeScreenState(
            availableCurrencies = CurrencyDefaults.SUPPORTED_CURRENCIES,
            sendingCurrency = CurrencyDefaults.DEFAULT_FROM,
            sendingAmount = CurrencyDefaults.DEFAULT_AMOUNT.toString(),
            receivingCurrency = CurrencyDefaults.DEFAULT_TO,
            receivingAmount = "",
            ratioText = null,
            sendingLimitExceededMessage = null,
            errorPanelState = null
        )
    }
}
