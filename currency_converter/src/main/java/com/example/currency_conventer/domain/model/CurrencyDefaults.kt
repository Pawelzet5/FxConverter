package com.example.currency_conventer.domain.model

import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_converter.R

object CurrencyDefaults {
    val PLN = Currency(
        code = "PLN",
        nameResId = R.string.currency_pln,
        countryNameResId = R.string.country_poland,
        sendingLimit = 20000.0
    )

    val EUR = Currency(
        code = "EUR",
        nameResId = R.string.currency_eur,
        countryNameResId = R.string.country_germany,
        sendingLimit = 5000.0
    )

    val GBP = Currency(
        code = "GBP",
        nameResId = R.string.currency_gbp,
        countryNameResId = R.string.country_great_britain,
        sendingLimit = 1000.0
    )

    val UAH = Currency(
        code = "UAH",
        nameResId = R.string.currency_uah,
        countryNameResId = R.string.country_ukraine,
        sendingLimit = 50000.0
    )

    fun getBigIconForCurrency(currency: Currency) = when(currency) {
        PLN -> R.drawable.ic_pln_big
        EUR -> R.drawable.ic_eur_big
        GBP -> R.drawable.ic_gbp_big
        UAH -> R.drawable.ic_uah_big
        else -> R.drawable.ic_uah_big
    }

    val SUPPORTED_CURRENCIES = listOf(PLN, EUR, GBP, UAH)
    val DEFAULT_FROM = PLN
    val DEFAULT_TO = UAH
    const val DEFAULT_AMOUNT = 300.0
}