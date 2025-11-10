package com.example.scanner.home.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.scanner.R
import com.example.scanner.databinding.FragmentCreateBinding

class CreateFragment : Fragment() {
    private lateinit var binding : FragmentCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateBinding.inflate(layoutInflater,container,false)

        return binding.root
    }
}