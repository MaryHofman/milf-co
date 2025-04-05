package com.example.hack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hack.db.connectToDatabase
import com.example.hack.db.executeQuery
//import com.example.hack.db.DBConfig
//import com.example.hack.db.DBRepositiry
//import com.example.hack.db.DatabaseFactory
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
//            val config = DBConfig(
//                url = "db",
//                user = "admin",
//                passwd = "0000"
//            )
//
//            val dataSource = DatabaseFactory.createDataSource(config)
//            val repository = DBRepositiry(dataSource)
//
//            runBlocking {
//                val trans = repository.getByUrlAndUserId("", 0)
//                trans.forEach { row ->
//                    println("${row.fileUrl} ${row.userId} ${row.chatId} ${row.summary} ${row.createdAt} ${row.transcript}")
//                }
//            }

            val connection = connectToDatabase("", "admin", "0000")
            connection?.let { executeQuery(connection) }
        }
    }
}