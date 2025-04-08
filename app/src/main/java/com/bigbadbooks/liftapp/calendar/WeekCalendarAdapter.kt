package com.bigbadbooks.liftapp.calendar

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bigbadbooks.liftapp.R
import java.time.LocalDate

class WeekCalendarAdapter(
    private val days: List<LocalDate>,
    private val currentDate: LocalDate = LocalDate.now()
) : RecyclerView.Adapter<WeekCalendarAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days.getOrNull(position) ?: run {
            Log.e("ADAPTER", "Invalid position: $position")
            return
        }
        holder.textView.text = date.dayOfMonth.toString()

        // Highlight today's date
        if (date == currentDate) {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.dark_gray))
            holder.textView.setBackgroundResource(R.drawable.circle_2)
        } else {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
            holder.textView.background = null
        }
    }

    override fun getItemCount() = days.size
}