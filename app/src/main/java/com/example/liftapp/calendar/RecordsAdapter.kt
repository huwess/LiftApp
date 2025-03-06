package com.example.liftapp.calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.liftapp.R
import com.example.liftapp.helper.record.StrengthRecord

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

        // Bind the data to the view
        @SuppressLint("SetTextI18n")
        fun bind(record: StrengthRecord) {
            repetitions.text = "Reps: ${record.repetitions}"
            dumbbellWeight.text = "Weight: ${record.dumbbellWeight}kg"
            strengthLevel.text = "Level: ${record.strengthLevel}"
            duration.text = "Duration: ${record.duration}"
            date.text = "Date: ${record.date}"
            time.text = "Time: ${record.time}"
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
}
