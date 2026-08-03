package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.firebase.FirestoreAppointment
import com.example.ui.theme.*

@Composable
fun PatientAppointmentsScreen(
    appointments: List<FirestoreAppointment>,
    onCancel: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour") }
                Text("Mes rendez-vous", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = WarmOffWhite
    ) { padding ->
        if (appointments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Aucun rendez-vous enregistré.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appointments, key = { it.id }) { appt ->
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SurfaceCard)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(appt.doctorName, fontWeight = FontWeight.Bold)
                            Text("${appt.date} • ${appt.type}", color = SageDeep)
                            Text(appt.motif)
                            Text("Statut : ${appt.status}", color = TextMuted)
                            if (appt.status == FirestoreAppointment.STATUS_PENDING || appt.status == FirestoreAppointment.STATUS_CONFIRMED) {
                                TextButton(onClick = { onCancel(appt.id) }) { Text("Annuler", color = Terracotta) }
                            }
                        }
                    }
                }
            }
        }
    }
}
