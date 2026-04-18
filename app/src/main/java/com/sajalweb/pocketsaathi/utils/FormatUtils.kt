package com.sajalweb.pocketsaathi.utils

fun formatAmount(amount: Double): String {
    return if (amount % 1 == 0.0)
        amount.toInt().toString()
    else
        amount.toString()
}