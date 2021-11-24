package com.tiago.example.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.tiago.example.component.ISwipeRefreshCustomListener
import com.tiago.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ISwipeRefreshCustomListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.swipeRefresh.setListener(this)
        binding.recyclerView.adapter = ItemAdapter()
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, 1))

        setContentView(binding.root)
    }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed(
            { refreshed() },
            3000
        )
    }

    private fun refreshed() = with(binding) {
        recyclerView.adapter = ItemAdapter()
        swipeRefresh.setRefreshing(false)
    }
}
