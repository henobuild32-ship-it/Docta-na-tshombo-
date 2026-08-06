package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirestoreAppointment
import com.example.data.firebase.FirestoreConversation

@Composable
fun ConversationListScreen(
    conversations: List<FirestoreConversation>,
    appointments: List<FirestoreAppointment>,
    currentUserId: String,
    onSelect: (FirestoreConversation) -> Unit,
    onStart: (FirestoreAppointment) -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Retour") }; Text("Conversations", style = MaterialTheme.typography.headlineSmall) }
        if (conversations.isEmpty() && appointments.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Aucun message", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Vos conversations avec les médecins et patients apparaîtront ici.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(conversations, key = { it.id }) { conversation ->
                    val otherIndex = conversation.participantIds.indexOfFirst { it != currentUserId }
                    ListItem(
                        headlineContent = { Text(conversation.participantNames.getOrNull(otherIndex) ?: "Conversation") },
                        supportingContent = { Text(conversation.lastMessage.ifBlank { "Aucun message" }) },
                        modifier = Modifier.clickable { onSelect(conversation) }
                    )
                }
                if (conversations.isEmpty()) {
                    items(appointments.distinctBy { it.patientId + it.doctorId }, key = { it.id }) { appointment ->
                        ListItem(
                            headlineContent = { Text(if (appointment.patientId == currentUserId) appointment.doctorName else appointment.patientName) },
                            supportingContent = { Text("Démarrer une conversation") },
                            modifier = Modifier.clickable { onStart(appointment) }
                        )
                    }
                }
            }
        }
    }
}
