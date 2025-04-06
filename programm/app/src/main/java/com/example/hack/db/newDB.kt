package com.example.hack.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

fun connectToDatabase(url: String, user: String, password: String): Connection? {
    return try {
        DriverManager.getConnection(url, user, password)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getByUrlAndUser(connection: Connection, file_url: String, user_id: Int): ResultSet? {
    val statement: Statement = connection.createStatement()
    val resultSet = statement.executeQuery(
        """
            SELECT json_agg(t) as json_data
            FROM (
                SELECT 
                    file_url,
                    transcript,
                    summary,
                    speakers,
                    created_at,
                    chat_id,
                    user_id
                FROM transcriptions
                WHERE file_url = $file_url AND user_id = $user_id
            ) t
    """
    )
    return resultSet
}