package com.example.scanner.home.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.scanner.databinding.FragmentHistoryBinding
import com.example.scanner.sqlite.QrAllHistoryDatabase


class HistoryFragment : Fragment() {
    private lateinit var binding : FragmentHistoryBinding
    private lateinit var dbHelper: QrAllHistoryDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater,container,false)
        dbHelper = QrAllHistoryDatabase(requireActivity())
        val list = dbHelper.getAllQrData().toMutableList()
        return binding.root
    }
}