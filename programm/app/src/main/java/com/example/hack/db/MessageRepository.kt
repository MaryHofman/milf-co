package com.example.hack.db

import com.example.hack.db.data.Chat
import com.example.hack.db.data.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("chats_messages")


    suspend fun addMessage(message: Message): String {
        var document = messagesCollection.document()
        message.id = document.id
        document.set(message).await()
        return document.id
    }


    suspend fun deleteMessage(chatId: String) {
        messagesCollection.document(chatId).delete().await()
    }

    suspend fun getMessagesByChat(chatId: String): List<Message> {
        val snapshot = messagesCollection
            .document(chatId)
            .collection("messages")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Message::class.java)?.apply {
                id = doc.id
            }
        }
    }




}