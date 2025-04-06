package com.example.hack.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

@Composable
fun NewChatScreen(viewModel: ChatViewModel = viewModel()) {
    var chatId by remember { mutableStateOf("") }

    val chats by viewModel.chats.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            viewModel.addChat("user123")
        }) {
            Text("Добавить чат")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (chatId.isNotEmpty()) {
                viewModel.deleteChat(chatId)
            }
        }) {
            Text("Удалить чат")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Отображаем список чатов
        LazyColumn {
            items(chats) { chat ->
                Text(text = "Chat ID: ${chat.id}, User ID: ${chat.userId}")
            }
        }
    }
}
