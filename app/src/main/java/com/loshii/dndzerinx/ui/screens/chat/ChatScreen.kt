package com.loshii.dndzerinx.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class ChatMessage(
    val id: String = "",
    val author: String = "",
    val content: String = "",
    val diceRoll: String? = null,
    val isIC: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatScreen(onBack: () -> Unit) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var input by remember { mutableStateOf("") }
    var isIC by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Sala de Campaña", style = MaterialTheme.typography.headlineMedium)

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(messages) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.isIC)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            msg.author,
                            color = if (msg.isIC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(msg.content, style = MaterialTheme.typography.bodyMedium)
                        msg.diceRoll?.let {
                            Text("🎲 $it", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                label = { Text("Mensaje") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (input.isNotBlank()) {
                    val roll = if (input.contains("/roll")) {
                        "d20 = ${Random.nextInt(1, 21)}"
                    } else null
                    messages = messages + ChatMessage(
                        author = if (isIC) "Personaje" else "Jugador",
                        content = input,
                        diceRoll = roll,
                        isIC = isIC
                    )
                    input = ""
                }
            }) { Text("Enviar") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { isIC = !isIC }) { Text(if (isIC) "IC" else "OOC") }
            Button(onClick = onBack) { Text("Volver") }
        }
    }
}
