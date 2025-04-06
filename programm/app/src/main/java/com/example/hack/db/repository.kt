package com.example.hack.db

//import com.fasterxml.jackson.databind.ObjectMapper
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import javax.sql.DataSource
//
//class DBRepositiry(private val dataSourse: DataSource) {
//    private val objMapper = ObjectMapper()
//
//    suspend fun getByUrlAndUserId(url: String, userId: Long): List<DB> = withContext(Dispatchers.IO) {
//        dataSourse.connection.use { connection ->
//            connection.prepareStatement(
//                """
//                SELECT json_agg(t) as json_data
//                FROM (
//                    SELECT
//                        file_url,
//                        transcript,
//                        summary,
//                        speakers,
//                        created_at,
//                        chat_id,
//                        user_id
//                    FROM transcriptions
//                    WHERE file_url = ? AND user_id = ?
//                ) t
//                """
//            ).use { statement ->
//                statement.setLong(1, userId)
//                statement.executeQuery().use { resultSet ->
//                    if (resultSet.next()) {
//                        val json = resultSet.getString("json_data") ?: "[]"
//                        objMapper.readValue<List<DB>>(json)
//                    } else {
//                        emptyList()
//                    }
//                }
//            }
//        }
//    }
//}