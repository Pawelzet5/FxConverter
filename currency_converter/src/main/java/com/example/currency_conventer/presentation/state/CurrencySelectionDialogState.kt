package com.example.currency_conventer.presentation.state

data class CurrencySelectionDialogState(
    val isCurrencySelectionDialogOpen: Boolean = false,
    val currencyInputType: CurrencyInputType = CurrencyInputType.SENDING
)
