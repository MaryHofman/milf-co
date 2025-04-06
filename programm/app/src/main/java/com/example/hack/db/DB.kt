package com.example.hack.db

//import com.fasterxml.jackson.annotation.JsonProperty
//import java.time.Instant
//
//// структура для хранения данных из БД
//data class DB(
//    @JsonProperty("file_url") val fileUrl: String,
//    @JsonProperty("transcript") val transcript: String,
//    @JsonProperty("summary") val summary: String,
//    @JsonProperty("speakers") val speakers: Map<String, Array<String>>,
//    @JsonProperty("created_at") val createdAt: Instant,
//    @JsonProperty("chat_id") val chatId: Long,
//    @JsonProperty("user_id") val userId: Long
//)
//
//// Дата класс чтобы хранить конфигурацию БД
//data class DBConfig (
//    val url: String,
//    val user: String,
//    val passwd: String,
//    val poolSize: Int = 5
//)
