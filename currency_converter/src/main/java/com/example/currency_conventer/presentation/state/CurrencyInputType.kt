package com.example.currency_conventer.presentation.state

import com.example.currency_converter.R

enum class CurrencyInputType {
    SENDING {
        override val labelTextResId = R.string.sending_from
    },
    RECEIVING {
        override val labelTextResId = R.string.sending_to
    };

    abstract val labelTextResId: Int
}