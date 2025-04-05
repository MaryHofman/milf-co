package com.example.hack

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hack.db.connectToDatabase
import com.example.hack.db.getByUrlAndUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.ResultSet

@Composable
fun Aurorized(appState: MutableState<AppState>,
              context: Context,
              onRegisterClick: (String, String, String) -> Unit,
              onLoginClick: () -> Unit
              ) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Регистрация",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) {
                PasswordVisualTransformation()
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Lock else Icons.Default.Close,
                        contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Подтвердите пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) {
                PasswordVisualTransformation()
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (password == confirmPassword) {
                    onRegisterClick(name, email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            Text("Зарегистрироваться")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLoginClick) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}

@Composable
fun Menu(appState: MutableState<AppState> = mutableStateOf(AppState.MENU),
         urlSelect: MutableState<String> = mutableStateOf(""),
         urls: MutableState<MutableList<String>>
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            items(urls.value.size) { url ->
                Row(Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
                    .border(
                        width = 3.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(10.dp)
                    )
                    .clip(RoundedCornerShape(10.dp))
                ) {
                    Text(
                        urls.value[url], fontSize = 24.sp,
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(4f)
                    )
                    IconButton(
                        onClick = {
                            urlSelect.value = urls.value[url]
                            appState.value = AppState.URL
                                  },
                        content = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Info"
                            )
                        },
                        modifier = Modifier
                            .border(width = 3.dp, color = Color.Black)
                            .weight(1f)
                    )
                }
            }
        }
        IconButton(
            onClick = { dishargeVideo(urls) },
            content = {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = "Add",
                    modifier = Modifier.scale(2f)
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
        )
    }
}

fun dishargeVideo(urls: MutableState<MutableList<String>>) {
    val url = ""
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    coroutineScope.launch {
        // Отправляем видео на сервер
    }
    urls.value.add(url)
}

fun requestBD(url: String): ResultSet? {
    // Получаем по url обработанные данные видео с сервера
    val connect = connectToDatabase(urlDB, "", "")
    return connect?.let { getByUrlAndUser(connect, url, user_id) }
}

@Composable
fun UrlView(url: MutableState<String>) {
    val result = requestBD(url.value)
    Box(Modifier.fillMaxSize()) {
        if (result == null) {
            Text("")
        }

    }
}

enum class AppState {
                    AUTORIZED,
                    MENU,
                    URL
}

@SuppressLint("MutableCollectionMutableState")
@Preview(showSystemUi = true)
@Composable
fun PreviewMenu() {
    val urls = remember{ mutableStateOf(mutableListOf("0", "1")) }
    Menu(urls = urls)
}