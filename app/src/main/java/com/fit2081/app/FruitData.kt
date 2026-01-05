package com.fit2081.app

data class FruitData(
    val name: String,
    val family: String,
    val order: String,
    val genus: String,
    val nutritions: FruitNutrition
)

data class FruitNutrition(
    val calories: Int,
    val fat: Double,
    val sugar: Double,
    val carbohydrates: Double,
    val protein: Double
)