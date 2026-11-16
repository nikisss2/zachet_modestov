package com.example.zachet_modestov

object WeatherRepository {
    val weatherList = mutableListOf<WeatherItem>()

    init {
        weatherList.add(
            WeatherItem(
                1,
                "2026-11-14",
                "День",
                "+4°C",
                "Москва",
                "Облачно с прояснениями",
                "Умеренный ветер"
            )
        )
        weatherList.add(
            WeatherItem(
                2,
                "2026-11-15",
                "Утро",
                "+2°C",
                "Москва",
                "Ясно",
                "Утренний заморозок"
            )
        )
        weatherList.add(
            WeatherItem(
                3,
                "2026-11-16",
                "День",
                "+5°C",
                "Москва",
                "Небольшой дождь",
                "Взять зонт"
            )
        )
        weatherList.add(
            WeatherItem(
                4,
                "2026-11-17",
                "Вечер",
                "+3°C",
                "Москва",
                "Пасмурно",
                "Влажность 85%"
            )
        )
    }

    fun getById(id: Int): WeatherItem? {
        return weatherList.find { it.id == id }
    }

    fun update(item: WeatherItem) {
        val index = weatherList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            weatherList[index] = item
        }
    }

    fun add(item: WeatherItem) {
        weatherList.add(0, item)
    }
}
