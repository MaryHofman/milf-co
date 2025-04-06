package com.example.hack.screens

import androidx.lifecycle.ViewModel


import androidx.lifecycle.viewModelScope
import com.example.hack.db.FirestoreChatRepository
import com.example.hack.db.data.Chat
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = FirestoreChatRepository()

    // Состояние для хранения списка чатов
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    // Метод для загрузки всех чатов
    fun loadChats() {
        viewModelScope.launch {
            try {
                val loadedChats = repository.getAllChats()
                _chats.value = loadedChats
            } catch (e: Exception) {
                println("Ошибка при загрузке чатов: ${e.message}")
            }
        }
    }

    // Методы для добавления и удаления чатов
    fun addChat(userId: String) {
        viewModelScope.launch {
            val newChat = Chat(userId = userId, timeCreate = System.currentTimeMillis())
            repository.addChat(newChat)
            loadChats() // Обновляем список чатов после добавления
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
            loadChats() // Обновляем список чатов после удаления
        }
    }
}