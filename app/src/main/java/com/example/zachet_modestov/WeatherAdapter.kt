package com.example.zachet_modestov

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter(
    private val items: List<WeatherItem>,
    private val onEditClick: (WeatherItem) -> Unit
) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: AppCompatTextView = itemView.findViewById(R.id.tvDate)
        val tvTimeOfDay: AppCompatTextView = itemView.findViewById(R.id.tvTimeOfDay)
        val tvTemp: AppCompatTextView = itemView.findViewById(R.id.tvTemp)
        val tvCityAndCondition: AppCompatTextView = itemView.findViewById(R.id.tvCityAndCondition)
        val tvComment: AppCompatTextView = itemView.findViewById(R.id.tvComment)
        val btnEdit: AppCompatButton = itemView.findViewById(R.id.btnEditItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = item.date
        holder.tvTimeOfDay.text = "Время суток: " + item.timeOfDay
        holder.tvTemp.text = item.temp
        holder.tvCityAndCondition.text = item.city + ", " + item.condition

        if (item.comment.isNotEmpty()) {
            holder.tvComment.visibility = View.VISIBLE
            holder.tvComment.text = "Заметка: " + item.comment
        } else {
            holder.tvComment.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(item)
        }

        holder.itemView.setOnClickListener {
            onEditClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
