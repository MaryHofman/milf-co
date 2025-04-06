package com.example.hack.screens

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.hack.services.createPdfFromResponse
import com.example.hack.services.sendEmailViaSMTP


@Composable
fun ChatScreen(navController: NavController) {
    // Состояния компонента
    var text by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("recording") }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var responseBody by remember { mutableStateOf("") }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showEmailDialog by remember { mutableStateOf(false) }
    var recipientEmail by remember { mutableStateOf("") }
    var pdfFile by remember { mutableStateOf<File?>(null) }

    // Медиа-объекты
    val mediaPlayer = remember { MediaPlayer() }
    val recorder = remember { MediaRecorder() }

    // Запрос разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Разрешение на запись не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Функции работы с аудио
    fun startRecording() {
        try {
            val uniqueName = UUID.randomUUID().toString().take(8)
            fileName = "rec_$uniqueName"
            val outputFile = File(context.filesDir, "$fileName.mp3").apply {
                createNewFile()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            audioFile = outputFile
            isRecording = true
            text = "Идёт запись..."
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка записи: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        try {
            recorder.stop()
            recorder.reset()
            isRecording = false
            text = "Запись сохранена: ${audioFile?.name ?: "unknown"}"
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка остановки записи", Toast.LENGTH_SHORT).show()
        }
    }

    fun playRecording(file: File) {
        try {
            if (isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                isPlaying = false
            } else {
                mediaPlayer.apply {
                    setDataSource(file.absolutePath)
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
        }
    }


    fun String.unescapeUnicode(): String {
        val regex = "\\\\u([0-9a-fA-F]{4})".toRegex()
        return regex.replace(this) {
            it.groupValues[1].toInt(16).toChar().toString()
        }
    }
    // Функция загрузки файла
    fun uploadFile(file: File) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val mediaType = "audio/mpeg".toMediaTypeOrNull()
            ?: run {
                text = "Неверный тип медиа"
                return
            }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody(mediaType))
            .build()

        val request = Request.Builder()
            .url("http://150.241.113.6/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                CoroutineScope(Dispatchers.Main).launch {
                    text = "Ошибка сети: ${e.message}"
                    responseBody = "Не удалось подключиться к серверу"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        val responseText = it.body?.string() ?: run {
                            CoroutineScope(Dispatchers.Main).launch {
                                text = "Пустой ответ от сервера"
                            }
                            return
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val json = Json { ignoreUnknownKeys = true }
                                val apiResponse = json.decodeFromString<ApiResponse>(responseText)
                                responseBody = formatApiResponse(apiResponse)

                                CoroutineScope(Dispatchers.Main).launch {
                                    // Сохраняем в PDF
                                    pdfFile = createPdfFromResponse(context, apiResponse.toString())

                                    if (pdfFile != null) {
                                        responseBody = "PDF успешно создан!"

                                    } else {
                                        responseBody = "Ошибка создания PDF"
                                    }
                                }

                                text = "Файл успешно обработан!"
                            } catch (e: Exception) {
                                responseBody = responseText
                                CoroutineScope(Dispatchers.Main).launch {
                                    // Сохраняем в PDF
                                    pdfFile = createPdfFromResponse(context, responseBody)

                                    if (pdfFile != null) {
                                        responseBody = "PDF успешно создан!"

                                    } else {
                                        responseBody = "Ошибка создания PDF"
                                    }
                                }

                                text = "Ответ сервера (необработанный):"
                            }
                        }
                    } catch (e: Exception) {
                        CoroutineScope(Dispatchers.Main).launch {
                            text = "Ошибка обработки ответа"
                            responseBody = "Ошибка: ${e.message}"
                        }
                    }
                }
            }
        })
    }

    // Выбор файла
    val selectFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fileUri ->
            val mimeType = context.contentResolver.getType(fileUri) ?: ""

            when {
                mimeType.startsWith("audio/") -> {
                    selectedAudioUri = fileUri
                    val tempFile = copyFileFromUri(context, fileUri)
                    if (tempFile != null) {
                        audioFile = tempFile
                        fileName = tempFile.nameWithoutExtension
                        text = "Аудиофайл выбран: ${tempFile.name}"
                    } else {
                        text = "Ошибка при обработке файла"
                    }
                }
                else -> Toast.makeText(context, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Очистка ресурсов
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) stopRecording()
            if (isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
            recorder.release()
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (pdfFile != null) {
            Button(
                onClick = { showEmailDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Отправить отчет по почте")
            }
        }

        if (showEmailDialog) {
            AlertDialog(
                onDismissRequest = { showEmailDialog = false },
                title = { Text("Отправить отчет") },
                text = {
                    Column {
                        Text("Введите email получателя:")
                        OutlinedTextField(
                            value = recipientEmail,
                            onValueChange = { recipientEmail = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("example@email.com") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (recipientEmail.isNotBlank() && pdfFile != null) {
                                // Здесь должны быть ваши реальные учетные данные
                                val senderEmail = "gofman09.03@gmail.com" // Замените на реальный email
                                val appPassword = "mbvi pdzv efoz ygjl"  // Пароль приложения Gmail

                                sendEmailViaSMTP(
                                    recipient = recipientEmail,
                                    subject = "Аудио отчет",
                                    body = responseBody,
                                    senderEmail = senderEmail,
                                    appPassword = appPassword,
                                    pdfFile = pdfFile!!
                                )

                                Toast.makeText(context, "Отчет отправляется...", Toast.LENGTH_SHORT).show()
                                showEmailDialog = false
                            } else {
                                Toast.makeText(context, "Введите корректный email", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Отправить")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEmailDialog = false }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
        // Область отображения ответа
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .background(Color.LightGray.copy(alpha = 0.2f))
                .padding(8.dp)
                .border(1.dp, Color.Gray)
        ) {
            SelectionContainer {
                Text(
                    text = responseBody.ifEmpty { "Здесь будет отображён ответ сервера..." },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .verticalScroll(scrollState),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Информационное поле
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Панель управления
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Поле имени файла
            TextField(
                value = fileName,
                onValueChange = { fileName = it.take(20) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Имя файла") }
            )

            // Кнопка отправки
            Button(
                onClick = { audioFile?.let { uploadFile(it) } ?: run { text = "Файл не выбран" } },
                modifier = Modifier.size(56.dp)
            ) {
                Text("↑", fontSize = 14.sp)
            }

            // Кнопка записи
            Button(
                onClick = { if (isRecording) stopRecording() else startRecording() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color.Blue
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.PlayArrow else Icons.Default.Build,
                    contentDescription = if (isRecording) "Stop" else "Record"
                )
            }

            // Кнопка выбора файла
            Button(
                onClick = { selectFileLauncher.launch("audio/*") },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.ArrowForward, "Select file")
            }

            // Кнопка воспроизведения
            Button(
                onClick = { audioFile?.let { playRecording(it) } ?: run { text = "Нет файла для воспроизведения" } },
                enabled = audioFile != null,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play"
                )
            }
        }
    }
}


private fun formatApiResponse(response: ApiResponse): String {
    fun Double.formatTime(): String {
        val minutes = (this / 60).toInt()
        val seconds = (this % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    return buildString {
        appendLine("📄 Полный текст:")
        appendLine(response.full_text)
        appendLine("\n🗣️ Диалог:")

        val allSegments = listOf(
            *response.speakers.speaker0.map {
                it.copy(text = "Спикер 1: ${it.text}")
            }.toTypedArray(),
            *response.speakers.speaker1.map {
                it.copy(text = "Спикер 2: ${it.text}")
            }.toTypedArray()
        ).sortedBy { it.start }

        allSegments.forEach { segment ->
            appendLine("[${segment.start.formatTime()} - ${segment.end.formatTime()}] ${segment.text}")
        }

        appendLine("\nℹ️ Суммаризация:")
        appendLine(
            response.summary
                .removePrefix("[Ошибка суммаризации: ")
                .removeSuffix("]")
        )
    }
}

private fun copyFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("temp_audio", ".mp3", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        tempFile
    } catch (e: Exception) {
        null
    }
}