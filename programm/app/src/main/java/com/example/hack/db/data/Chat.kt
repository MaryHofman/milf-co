package com.example.hack.db.data

data class Chat(
    var id: String = "",
    val userId: String = "",
    val timeCreate: Long = System.currentTimeMillis()
)