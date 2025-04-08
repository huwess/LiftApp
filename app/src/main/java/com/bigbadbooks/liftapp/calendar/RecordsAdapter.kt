package com.bigbadbooks.liftapp.calendar

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bigbadbooks.liftapp.R
import com.bigbadbooks.liftapp.helper.record.StrengthRecord
import java.text.SimpleDateFormat
import java.util.Locale

class RecordsAdapter(private val records: List<StrengthRecord>) :
    RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    // ViewHolder class: This holds the views for each item
    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val repetitions: TextView = itemView.findViewById(R.id.repetitions)
        private val dumbbellWeight: TextView = itemView.findViewById(R.id.dumbbellWeight)
        private val strengthLevel: TextView = itemView.findViewById(R.id.strengthLevel)
        private val duration: TextView = itemView.findViewById(R.id.duration)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val unit: ImageView = itemView.findViewById(R.id.unit)

        // Bind the data to the view
        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(record: StrengthRecord) {
            repetitions.text = "${record.repetitions} reps"
            dumbbellWeight.text = String.format("%.1f", record.dumbbellWeight)
            strengthLevel.text = record.strengthLevel
            duration.text = "${millisecondsToSeconds(record.duration)}s"
            date.text = stringToDate(record.date)
            time.text = stringToTime(record.time)
            if(record.unit == 0) {
                Log.d("CheckUnit", "${record.unit}")
                unit.setImageResource(R.drawable.weight_kg)
            } else {
                Log.d("CheckUnit", "${record.unit}")
                unit.setBackgroundResource(R.drawable.weight_lb)
            }

        }
    }

    // Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(itemView)
    }

    // Called to bind data to the ViewHolder
    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.bind(record)
    }

    // Returns the size of the list
    override fun getItemCount(): Int {
        return records.size
    }

    fun stringToDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(dateString) ?: return "Invalid Date"
            outputFormat.format(date)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
    private fun millisecondsToSeconds(milliseconds: Long): Long {
        return milliseconds / 1000
    }

    fun stringToTime(timeString: String): String {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // 24-hour format
        val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault()) // 12-hour format with AM/PM

        return try {
            val date = inputFormat.parse(timeString) ?: return "Invalid Time"
            outputFormat.format(date)
        } catch (e: Exception) {
            "Invalid Time"
        }
    }
}
