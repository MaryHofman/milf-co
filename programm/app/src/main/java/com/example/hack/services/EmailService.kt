package com.example.hack.services

import android.util.Log
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

import java.io.File
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

fun sendEmailViaSMTP(
    recipient: String,
    subject: String,
    body: String,
    senderEmail: String,
    appPassword: String,
    pdfFile: File
) {
    Thread {
        try {
            Log.d("EmailSender", "Подготовка к отправке письма с вложением...")

            // Настройка свойств SMTP
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.ssl.trust", "smtp.gmail.com")
                put("mail.smtp.ssl.protocols", "TLSv1.2")
                // Для больших файлов
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.socketFactory.port", "587")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.socketFactory.fallback", "false")
            }

            // Создание сессии
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, appPassword)
                }
            })

            // Создание MIME сообщения
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
                this.subject = subject
            }

            // Создание multipart контента
            val multipart = MimeMultipart()

            // Текст письма
            val textPart = MimeBodyPart().apply {
                setText(body, "utf-8", "html")
            }
            multipart.addBodyPart(textPart)

            // Вложение PDF
            val attachmentPart = MimeBodyPart().apply {
                val source = FileDataSource(pdfFile)
                dataHandler = DataHandler(source)
                fileName = pdfFile.name
                setHeader("Content-Type", "application/pdf")
            }
            multipart.addBodyPart(attachmentPart)

            // Установка контента сообщения
            message.setContent(multipart)

            // Отправка письма
            Transport.send(message)
            Log.d("EmailSender", "Письмо с вложением успешно отправлено!")

        } catch (e: Exception) {
            Log.e("EmailSender", "Ошибка при отправке письма с вложением", e)
        }
    }.start()
}