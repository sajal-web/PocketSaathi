package com.sajalweb.pocketsaathi.data.model

data class ParsedExpense(
    val amount: Double?,
    val category: Category,
    val description: String,
    val confidence: Float // 0.0 - 1.0
)