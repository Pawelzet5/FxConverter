package com.example.currency_conventer.domain.model

import com.example.currency_conventer.domain.model.dataclass.Currency

object CurrencyDefaults {
    val PLN = Currency(
        code = "PLN",
        name = "Polish Zloty",
        countryName = "Poland",
        sendingLimit = 20000.0
    )

    val EUR = Currency(
        code = "EUR",
        name = "Euro",
        countryName = "Germany",
        sendingLimit = 5000.0
    )

    val GBP = Currency(
        code = "GBP",
        name = "British Pound",
        countryName = "Great Britain",
        sendingLimit = 1000.0
    )

    val UAH = Currency(
        code = "UAH",
        name = "Ukrainian Hryvnia",
        countryName = "Ukraine",
        sendingLimit = 50000.0
    )

    val SUPPORTED_CURRENCIES = listOf(PLN, EUR, GBP, UAH)
    val DEFAULT_FROM = PLN
    val DEFAULT_TO = UAH
    const val DEFAULT_AMOUNT = 300.0
}