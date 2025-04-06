package com.example.hack.db

import com.example.hack.db.data.Chat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val chatsCollection = firestore.collection("chats")


    suspend fun getAllChats(): List<Chat> {
        return try {
            val snapshot = chatsCollection.get().await()
            snapshot.toObjects(Chat::class.java)
        } catch (e: Exception) {
            println("Ошибка при получении чатов: ${e.message}")
            emptyList()
        }
    }

    suspend fun addChat(chat: Chat): String {
        var document = chatsCollection.document()
        chat.id = document.id
        document.set(chat).await()
        return document.id
    }


    suspend fun deleteChat(chatId: String) {
        chatsCollection.document(chatId).delete().await()
    }
}