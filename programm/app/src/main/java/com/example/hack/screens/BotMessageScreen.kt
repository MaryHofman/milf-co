package com.example.hack.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class JsonData(
    val full_text: String,
    val source: String,
    val speakers: Map<String, List<MessageDetails>>,
    val summary: String
)

data class MessageDetails(
    val start: Double,
    val end: Double,
    val text: String
)

@Composable
fun DisplaySummaryOrFullText(jsonData: JsonData) {

    var isFullView by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        if (isFullView) {
            FullTextView(jsonData)
        } else {
            Text(
                text = jsonData.summary,
                modifier = Modifier.padding(16.dp)
            )
        }

        Button(
            onClick = { isFullView = !isFullView },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = if (isFullView) "Скрыть" else "Развернуть")
        }
    }
}

@Composable
fun FullTextView(jsonData: JsonData) {
    Text(
        text = "Полный текст",
        modifier = Modifier.padding(16.dp)
    )

    Text(
        text = jsonData.full_text,
        modifier = Modifier.padding(16.dp)
    )

    jsonData.speakers.forEach { (speaker, messages) ->
        SpeakerSection(speaker, messages)
    }
}

@Composable
fun SpeakerSection(speaker: String, messages: List<MessageDetails>) {
    Text(
        text = "$speaker:",

        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
    )

    LazyColumn(modifier = Modifier.padding(start = 16.dp)) {
        items(messages) { message ->
            Text(
                text = message.text,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
