package com.example.zachet_modestov

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherListFragment : Fragment() {

    private lateinit var etCity: AppCompatEditText
    private lateinit var btnFetchWeather: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private lateinit var tvLastDate: AppCompatTextView
    private lateinit var tvLastTimeOfDay: AppCompatTextView
    private lateinit var tvLastTemp: AppCompatTextView
    private lateinit var tvLastDetails: AppCompatTextView
    private lateinit var btnEditLast: AppCompatButton

    private lateinit var rvWeather: RecyclerView
    private lateinit var adapter: WeatherAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etCity = view.findViewById(R.id.etCity)
        btnFetchWeather = view.findViewById(R.id.btnFetchWeather)
        progressBar = view.findViewById(R.id.progressBar)

        tvLastDate = view.findViewById(R.id.tvLastDate)
        tvLastTimeOfDay = view.findViewById(R.id.tvLastTimeOfDay)
        tvLastTemp = view.findViewById(R.id.tvLastTemp)
        tvLastDetails = view.findViewById(R.id.tvLastDetails)
        btnEditLast = view.findViewById(R.id.btnEditLast)

        rvWeather = view.findViewById(R.id.rvWeather)
        rvWeather.layoutManager = LinearLayoutManager(requireContext())
        adapter = WeatherAdapter(WeatherRepository.weatherList) { item ->
            openEditFragment(item)
        }
        rvWeather.adapter = adapter

        btnFetchWeather.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWeatherFromApi(city)
            } else {
                Toast.makeText(requireContext(), getString(R.string.hint_city), Toast.LENGTH_SHORT).show()
            }
        }

        loadLastSavedFromPreferences()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        loadLastSavedFromPreferences()
    }

    private fun loadLastSavedFromPreferences() {
        val prefs = requireActivity().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val id = prefs.getInt("last_id", -1)
        val date = prefs.getString("last_date", null)
        val timeOfDay = prefs.getString("last_time_of_day", null)
        val temp = prefs.getString("last_temp", null)
        val city = prefs.getString("last_city", null)
        val condition = prefs.getString("last_condition", null)
        val comment = prefs.getString("last_comment", "") ?: ""

        if (date != null && temp != null && timeOfDay != null) {
            tvLastDate.text = "Дата: " + date
            tvLastTimeOfDay.text = "Время суток: " + timeOfDay
            tvLastTemp.text = temp
            tvLastDetails.text = (city ?: "Москва") + ", " + (condition ?: "Ясно")
            btnEditLast.visibility = View.VISIBLE
            btnEditLast.setOnClickListener {
                val item = WeatherRepository.getById(id) ?: WeatherItem(
                    id,
                    date,
                    timeOfDay,
                    temp,
                    city ?: "Москва",
                    condition ?: "Ясно",
                    comment
                )
                openEditFragment(item)
            }
        } else {
            tvLastDate.text = getString(R.string.no_data)
            tvLastTimeOfDay.text = ""
            tvLastTemp.text = ""
            tvLastDetails.text = ""
            btnEditLast.visibility = View.GONE
        }
    }

    private fun saveLastToPreferences(item: WeatherItem) {
        val prefs = requireActivity().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("last_id", item.id)
            .putString("last_date", item.date)
            .putString("last_time_of_day", item.timeOfDay)
            .putString("last_temp", item.temp)
            .putString("last_city", item.city)
            .putString("last_condition", item.condition)
            .putString("last_comment", item.comment)
            .apply()
    }

    private fun fetchWeatherFromApi(city: String) {
        progressBar.visibility = View.VISIBLE
        val queue = Volley.newRequestQueue(requireContext())
        val encodedCity = Uri.encode(city)
        val url = "https://api.weatherapi.com/v1/current.json?key=c515a63fa54e41c4b1a133408240410&q=" + encodedCity + "&lang=ru"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                progressBar.visibility = View.GONE
                parseAndSaveWeather(response)
            },
            {
                progressBar.visibility = View.GONE
                handleNetworkFallback(city)
            }
        )

        queue.add(stringRequest)
    }

    private fun parseAndSaveWeather(response: String) {
        try {
            val obj = JSONObject(response)
            val location = obj.getJSONObject("location")
            val cityName = location.optString("name", "Город")
            val localtime = location.optString("localtime", "")

            val dateStr = if (localtime.contains(" ")) {
                localtime.split(" ")[0]
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }

            val current = obj.getJSONObject("current")
            val tempC = current.optDouble("temp_c", 0.0)
            val isDay = current.optInt("is_day", 1)
            val timeOfDay = if (isDay == 1) "День" else "Ночь"

            val conditionObj = current.optJSONObject("condition")
            val condition = conditionObj?.optString("text", "Без осадков") ?: "Ясно"

            val tempFormatted = (if (tempC > 0) "+" else "") + tempC.toInt() + "°C"
            val newId = (System.currentTimeMillis() % 100000).toInt()

            val newItem = WeatherItem(
                newId,
                dateStr,
                timeOfDay,
                tempFormatted,
                cityName,
                condition,
                "Запрошено из WeatherAPI"
            )

            WeatherRepository.add(newItem)
            saveLastToPreferences(newItem)

            adapter.notifyDataSetChanged()
            loadLastSavedFromPreferences()
            Toast.makeText(requireContext(), "Погода для " + cityName + " загружена", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            handleNetworkFallback(etCity.text.toString().trim())
        }
    }

    private fun handleNetworkFallback(city: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newId = (System.currentTimeMillis() % 100000).toInt()
        val fallbackItem = WeatherItem(
            newId,
            dateStr,
            "День",
            "+12°C",
            if (city.isNotEmpty()) city else "Москва",
            "Переменная облачность",
            "Локальная запись"
        )

        WeatherRepository.add(fallbackItem)
        saveLastToPreferences(fallbackItem)

        adapter.notifyDataSetChanged()
        loadLastSavedFromPreferences()
        Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show()
    }

    private fun openEditFragment(item: WeatherItem) {
        val fragment = EditWeatherFragment.newInstance(item.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
