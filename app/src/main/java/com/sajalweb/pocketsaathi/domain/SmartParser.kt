package com.sajalweb.pocketsaathi.domain

import com.sajalweb.pocketsaathi.data.model.Category
import com.sajalweb.pocketsaathi.data.model.ParsedExpense
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartParser @Inject constructor() {

    // Patterns: "paid 250 for food", "spent 500 on cab", "250 food", "food 250"
    private val amountPatterns = listOf(
        Regex("""(?:paid|spent|pay|spend|rs\.?|₹|inr)?\s*(\d+(?:\.\d{1,2})?)\s*(?:rs\.?|₹)?""", RegexOption.IGNORE_CASE),
        Regex("""(\d+(?:\.\d{1,2})?)\s*(?:rupees?|bucks?)?""", RegexOption.IGNORE_CASE)
    )

    private val skipWords = setOf("paid", "spent", "pay", "spend", "for", "on", "at", "the", "a", "an", "rs", "inr")

    fun parse(input: String): ParsedExpense {
        val cleaned = input.trim().lowercase()
        val amount = extractAmount(cleaned)
        val category = detectCategory(cleaned)
        val description = buildDescription(input, amount)
        val confidence = if (amount != null) 0.9f else 0.3f

        return ParsedExpense(amount, category, description, confidence)
    }

    private fun extractAmount(text: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(text) ?: continue
            val value = match.groupValues[1].toDoubleOrNull()
            if (value != null && value > 0) return value
        }
        return null
    }

    private fun detectCategory(text: String): Category {
        val words = text.split(Regex("\\s+|[,.]"))
        val filtered = words.filter { it !in skipWords && it.length > 2 }

        for (category in Category.values()) {
            if (category == Category.OTHERS) continue
            for (keyword in category.keywords) {
                if (filtered.any { it.contains(keyword) || keyword.contains(it) }) {
                    return category
                }
            }
        }
        // Fuzzy fallback: search whole text
        for (category in Category.values()) {
            if (category == Category.OTHERS) continue
            if (category.keywords.any { text.contains(it) }) return category
        }
        return Category.OTHERS
    }

    private fun buildDescription(original: String, amount: Double?): String {
        val cleaned = original
            .replace(Regex("""(?:paid|spent|pay|spend)\s+""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""(?:rs\.?|₹|inr)\s*\d+(?:\.\d{1,2})?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\d+(?:\.\d{1,2})?\s*(?:rs\.?|₹|rupees?)?"""), "")
            .replace(Regex("""(?:for|on|at)\s+""", RegexOption.IGNORE_CASE), "")
            .trim().trimStart('-', ',').trim()
            .replaceFirstChar { it.uppercase() }

        return cleaned.ifEmpty { "Expense" }
    }
}