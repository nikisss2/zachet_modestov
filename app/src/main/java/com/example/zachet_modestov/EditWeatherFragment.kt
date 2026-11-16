package com.example.zachet_modestov

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment

class EditWeatherFragment : Fragment() {

    private lateinit var etEditDate: AppCompatEditText
    private lateinit var etEditTimeOfDay: AppCompatEditText
    private lateinit var etEditTemp: AppCompatEditText
    private lateinit var etEditCity: AppCompatEditText
    private lateinit var etEditCondition: AppCompatEditText
    private lateinit var etEditComment: AppCompatEditText
    private lateinit var btnSave: AppCompatButton
    private lateinit var btnBack: AppCompatButton

    private var itemId: Int = -1

    companion object {
        private const val ARG_ITEM_ID = "arg_item_id"

        fun newInstance(itemId: Int): EditWeatherFragment {
            val fragment = EditWeatherFragment()
            val args = Bundle()
            args.putInt(ARG_ITEM_ID, itemId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getInt(ARG_ITEM_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etEditDate = view.findViewById(R.id.etEditDate)
        etEditTimeOfDay = view.findViewById(R.id.etEditTimeOfDay)
        etEditTemp = view.findViewById(R.id.etEditTemp)
        etEditCity = view.findViewById(R.id.etEditCity)
        etEditCondition = view.findViewById(R.id.etEditCondition)
        etEditComment = view.findViewById(R.id.etEditComment)
        btnSave = view.findViewById(R.id.btnSave)
        btnBack = view.findViewById(R.id.btnBack)

        val item = WeatherRepository.getById(itemId)
        if (item != null) {
            etEditDate.setText(item.date)
            etEditTimeOfDay.setText(item.timeOfDay)
            etEditTemp.setText(item.temp)
            etEditCity.setText(item.city)
            etEditCondition.setText(item.condition)
            etEditComment.setText(item.comment)
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            val newDate = etEditDate.text.toString().trim()
            val newTimeOfDay = etEditTimeOfDay.text.toString().trim()
            val newTemp = etEditTemp.text.toString().trim()
            val newCity = etEditCity.text.toString().trim()
            val newCondition = etEditCondition.text.toString().trim()
            val newComment = etEditComment.text.toString().trim()

            if (newDate.isEmpty() || newTemp.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.empty_fields_warning), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (item != null) {
                item.date = newDate
                item.timeOfDay = if (newTimeOfDay.isNotEmpty()) newTimeOfDay else "День"
                item.temp = newTemp
                item.city = if (newCity.isNotEmpty()) newCity else "Москва"
                item.condition = if (newCondition.isNotEmpty()) newCondition else "Ясно"
                item.comment = newComment

                WeatherRepository.update(item)

                val prefs = requireActivity().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
                if (prefs.getInt("last_id", -1) == item.id) {
                    prefs.edit()
                        .putString("last_date", item.date)
                        .putString("last_time_of_day", item.timeOfDay)
                        .putString("last_temp", item.temp)
                        .putString("last_city", item.city)
                        .putString("last_condition", item.condition)
                        .putString("last_comment", item.comment)
                        .apply()
                }

                Toast.makeText(requireContext(), getString(R.string.success_saved), Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }
}
