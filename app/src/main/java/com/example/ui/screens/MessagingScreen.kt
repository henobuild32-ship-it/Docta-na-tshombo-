package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirestoreMessage
import com.example.ui.theme.*

@Composable
fun MessagingScreen(
    messages: List<FirestoreMessage>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    isSeniorMode: Boolean
) {
    var textInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WarmOffWhite,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WaterGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Dr A", fontWeight = FontWeight.Bold, color = SageDeep)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Dr. Amina Kalala", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Outlined.Verified, contentDescription = "Vérifié", tint = SageDeep, modifier = Modifier.size(16.dp))
                        }
                        Text("Messagerie chiffrée de santé", fontSize = 11.sp, color = SageMedium)
                    }
                }
            }
        },
        containerColor = WarmOffWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId != "doctor"
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (!isMe) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (!isMe) WaterGreen else SageDeep,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isMe) SageDeep else WaterGreenLight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = if (isSeniorMode) 18.sp else 14.sp,
                                    color = if (!isMe) TextDark else Color.White
                                )

                                if (msg.attachmentName.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (!isMe) SageDeep.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(msg.attachmentName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.createdAt?.toDate()?.time?.toString() ?: "À l'instant",
                                    fontSize = 9.sp,
                                    color = if (!isMe) TextMuted else WaterGreenLight,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Écrire un message sécurisé...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageDeep,
                        unfocusedBorderColor = SurfaceCardBorder
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    },
                    containerColor = SageDeep,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Outlined.Send, contentDescription = "Envoyer")
                }
            }
        }
    }
}
