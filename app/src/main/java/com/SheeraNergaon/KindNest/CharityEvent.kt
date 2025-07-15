package com.SheeraNergaon.KindNest

import java.io.Serializable

data class CharityEvent(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val createdBy: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
) : Serializable
