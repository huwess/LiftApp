package com.example.liftapp.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.liftapp.R

class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val onItemClick: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount(): Int {
        return exercises.size
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.exerciseImage)
        private val nameTextView: TextView = itemView.findViewById(R.id.exerciseName)

        fun bind(exercise: Exercise) {
            imageView.setImageResource(exercise.imageResId)
            nameTextView.text = exercise.name

            itemView.setOnClickListener {
                onItemClick(exercise)
            }
        }
    }
}
