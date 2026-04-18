package com.sajalweb.pocketsaathi.data.model

enum class Category(val label: String, val emoji: String, val keywords: List<String>) {

    FOOD(
        "Food", "🍔",
        listOf("food","eat","lunch","dinner","breakfast","coffee","cafe","restaurant","zomato","swiggy","tea","snack","pizza","burger","biryani","roll","meal","tiffin","vegetable")
    ),

    TRAVEL(
        "Travel", "🚗",
        listOf("travel","cab","auto","bus","uber","ola","metro","petrol","fuel","diesel","ticket","flight","train","rapido","bike","taxi")
    ),

    SHOPPING(
        "Shopping", "🛍️",
        listOf("shopping","clothes","amazon","flipkart","buy","purchase","mall","shirt","shoes","dress","jeans","watch","bag","order")
    ),

    RENT(
        "Rent & Bills", "🏠",
        listOf("rent","house","pg","accommodation","flat","maintenance","electricity","water","wifi","internet","bill","recharge","dth","gas")
    ),

    ENTERTAINMENT(
        "Entertainment", "🎬",
        listOf("movie","netflix","spotify","game","gym","sport","concert","show","party","pub","bar","fun","outing")
    ),

    HEALTH(
        "Health", "💊",
        listOf("medicine","doctor","hospital","pharmacy","medical","health","clinic","lab","test","checkup","tablet")
    ),

    EDUCATION(
        "Education", "📚",
        listOf("book","course","udemy","class","college","school","exam","fees","tuition","study","learning")
    ),

    PERSONAL(
        "Personal Care", "🧴",
        listOf("salon","haircut","spa","grooming","cosmetics","cream","soap","shampoo","makeup","parlor")
    ),

    SUBSCRIPTION(
        "Subscriptions", "📺",
        listOf("subscription","netflix","prime","hotstar","spotify","youtube","membership","plan","renewal")
    ),

    GIFTS(
        "Gifts & Donations", "🎁",
        listOf("gift","present","donation","charity","help","festival","birthday","wedding")
    ),

    INVESTMENT(
        "Investment", "📈",
        listOf("sip","mutual fund","stock","investment","trading","zerodha","groww","crypto","fd","rd")
    ),

    SAVINGS(
        "Savings", "💰",
        listOf("saving","deposit","bank","transfer","emergency fund","piggy","wallet")
    ),

    OTHERS(
        "Others", "📦",
        listOf()
    )
}