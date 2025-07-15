package com.SheeraNergaon.KindNest.utilities

object Constants {
    fun getUserLevel(points: Int): String = when {
        points < 1000 -> "Seed 🌱"
        points in 1000..2499 -> "Flower 🌷"
        else -> "Tree 🌳"
    }

    fun getUnlockedFeatures(level: String): List<String> = when (level) {
        "Seed" -> listOf("Daily Deeds", "Earn Points")
        "Flower" -> listOf("Daily Deeds", "Earn Points", "Inspiring Quote")
        "Tree" -> listOf("Daily Deeds", "Earn Points", "Inspiring Quote", "Organize Charity Events")
        else -> emptyList()
    }
}
