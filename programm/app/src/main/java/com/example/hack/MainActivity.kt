package com.example.hack

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.hack.db.connectToDatabase
import com.example.hack.db.getByUrlAndUser
//import com.example.hack.db.DBConfig
//import com.example.hack.db.DBRepositiry
//import com.example.hack.db.DatabaseFactory

const val urlDB = ""
const val user_id = 0

class MainActivity : ComponentActivity() {
    @SuppressLint("MutableCollectionMutableState")
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
            connection?.let { getByUrlAndUser(connection, "", 0) }

            val appState = remember { mutableStateOf(AppState.MENU) }
            val urlSelect = remember { mutableStateOf("") }
            val urls = remember { mutableStateOf(mutableListOf("")) }

            val sharedPreferences = getSharedPreferences("Autorized", MODE_PRIVATE)
            if (!sharedPreferences.getBoolean("is_autorized", false))
                appState.value = AppState.AUTORIZED

            App(appState, applicationContext, urlSelect, urls)
        }
    }
}

@Composable
fun App(appState: MutableState<AppState>,
        context: Context,
        urlSelect: MutableState<String>,
        urls: MutableState<MutableList<String>>) {
    when (appState.value) {
        AppState.AUTORIZED -> Aurorized(appState, context, {_, _, _ -> Unit}, {})
        AppState.MENU -> Menu(appState, urlSelect, urls)
        AppState.URL -> UrlView(urlSelect)
    }
}