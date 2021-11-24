package com.tiago.example.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tiago.example.R
import com.tiago.example.util.Resources
import kotlin.random.Random

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind() = with(itemView) {
        val strings = context.resources.getStringArray(R.array.random_phrases)
        val randomString = strings[Random.nextInt(0, 23)]

        findViewById<ImageView>(R.id.image_view).setImageResource(Resources.getRandomAvatar())
        findViewById<TextView>(R.id.text_view).text = randomString
    }
}
