package com.sajalweb.pocketsaathi.data.model

enum class Category(val label: String, val emoji: String, val keywords: List<String>) {
    FOOD("Food", "🍔", listOf("food","eat","lunch","dinner","breakfast","coffee","cafe","restaurant","zomato","swiggy","tea","snack")),
    TRAVEL("Travel", "🚗", listOf("travel","cab","auto","bus","uber","ola","metro","petrol","fuel","ticket","flight","train")),
    SHOPPING("Shopping", "🛍️", listOf("shopping","clothes","amazon","flipkart","buy","purchase","mall","shirt","shoes","dress")),
    RENT("Rent", "🏠", listOf("rent","house","pg","accommodation","flat","maintenance","electricity","water","wifi","internet")),
    ENTERTAINMENT("Entertainment", "🎬", listOf("movie","netflix","spotify","game","gym","sport","concert","show","party")),
    HEALTH("Health", "💊", listOf("medicine","doctor","hospital","pharmacy","medical","health","clinic","lab","test")),
    OTHERS("Others", "📦", listOf())
}