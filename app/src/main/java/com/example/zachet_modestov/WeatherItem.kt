package com.example.zachet_modestov

import java.io.Serializable

data class WeatherItem(
    val id: Int,
    var date: String,
    var timeOfDay: String,
    var temp: String,
    var city: String,
    var condition: String,
    var comment: String = ""
) : Serializable
