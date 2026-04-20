package com.sajalweb.pocketsaathi.utils
import java.util.Calendar

fun formatAmount(amount: Double): String {
    return if (amount % 1 == 0.0)
        amount.toInt().toString()
    else
        amount.toString()
}

fun getTodayStartMillis(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}