package com.example.liftapp.exercise

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.liftapp.MainActivity
import com.example.liftapp.R
import com.example.liftapp.bottom_nav.HomeFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExerciseCarouselFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExerciseCarouselFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var exerciseRecyclerView: RecyclerView
    private val exercises = listOf(
        Exercise("Push Up", "A great exercise for the chest and arms", R.drawable.ic_lift),
        Exercise("Squat", "A lower body exercise targeting the quads", R.drawable.ic_lift),
        Exercise("Lunge", "Targets the legs and glutes", R.drawable.ic_lift),
        Exercise("Plank", "Core strengthening exercise", R.drawable.ic_lift),
        Exercise("Burpee", "Full body workout", R.drawable.ic_lift)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise_carousel, container, false)

        // Initialize RecyclerView
        exerciseRecyclerView = view.findViewById(R.id.exerciseRecyclerView)

        // Set up the adapter and layout manager
        val exerciseAdapter = ExerciseAdapter(exercises) { exercise ->
            // Notify MainActivity to replace the current fragment with HomeFragment
            (activity as? MainActivity)?.replaceFragment(HomeFragment())
        }

        exerciseRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        exerciseRecyclerView.adapter = exerciseAdapter

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExerciseCarouselFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                ExerciseCarouselFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}