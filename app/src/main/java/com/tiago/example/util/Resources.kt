package com.tiago.example.util

import com.tiago.example.R
import kotlin.random.Random

object Resources {

    private val avatars = listOf(
        R.drawable.ic_avatar_1,
        R.drawable.ic_avatar_2,
        R.drawable.ic_avatar_3,
        R.drawable.ic_avatar_4,
        R.drawable.ic_avatar_5,
        R.drawable.ic_avatar_6,
        R.drawable.ic_avatar_7,
        R.drawable.ic_avatar_8,
        R.drawable.ic_avatar_9,
        R.drawable.ic_avatar_10
    )

    fun getRandomAvatar(): Int = avatars[Random.nextInt(0, 10)]

    private val itemsLayout = listOf(
        R.layout.adapter_item_left,
        R.layout.adapter_item_right
    )

    fun getRandomItemLayout(): Int = itemsLayout[Random.nextInt(0, 2)]
}
