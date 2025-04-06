package com.example.hack.screens


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hack.services.sendEmailViaSMTP

@Composable
fun EmailTestScreen() {
    val context = LocalContext.current

    // Состояния для полей ввода
    var recipient by remember { mutableStateOf("mar.gof2012@yandex.ru") }
    var subject by remember { mutableStateOf("Тестовое письмо") }
    var body by remember { mutableStateOf("Это тестовое письмо, отправленное через SMTP.") }

    // Данные для отправки (замените на свои)
    val senderEmail = "gofman09.03@gmail.com" // Ваш email
    val appPassword = "mbvi pdzv efoz ygjl" // Пароль приложения

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Поле для ввода получателя
        OutlinedTextField(
            value = recipient,
            onValueChange = { recipient = it },
            label = { Text("Получатель") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле для ввода темы
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Тема письма") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле для ввода текста письма
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Текст письма") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для отправки письма
        Button(onClick = {
            sendEmailViaSMTP(
                recipient = recipient,
                subject = subject,
                body = body,
                senderEmail = senderEmail,
                appPassword = appPassword,
                pdfFile = TODO()
            )
        }) {
            Text("Отправить письмо")
        }
    }
}