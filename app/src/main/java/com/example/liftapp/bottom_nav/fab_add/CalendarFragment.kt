package com.example.liftapp.bottom_nav.fab_add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.liftapp.R
import com.example.liftapp.databinding.FragmentCalendarBinding


class CalendarFragment : Fragment() {

    private var _fragmentCalendarBinding: FragmentCalendarBinding? = null
    private val binding get() = _fragmentCalendarBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCalendarBinding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        private const val TAG = "All Records"
    }
}