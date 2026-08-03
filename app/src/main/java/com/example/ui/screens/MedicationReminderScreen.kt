package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirestoreMedicationReminder
import com.example.data.firebase.FirestorePrescription
import com.example.data.firebase.toDisplayDate
import com.example.ui.theme.*

@Composable
fun MedicationReminderScreen(
    reminders: List<FirestoreMedicationReminder>,
    prescriptions: List<FirestorePrescription>,
    onDownloadPrescription: (FirestorePrescription) -> Unit,
    onToggleTaken: (String, Boolean) -> Unit,
    onAddReminder: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    isSeniorMode: Boolean
) {
    var showAddModal by remember { mutableStateOf(false) }

    var medName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("1 comprimé") }
    var frequency by remember { mutableStateOf("Chaque matin") }
    var timeOfDay by remember { mutableStateOf("08:00") }

    val takenCount = reminders.count { it.isTakenToday }
    val totalCount = reminders.size
    val compliancePercent = if (totalCount > 0) (takenCount * 100 / totalCount) else 100

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WarmOffWhite,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suivi Traitement & Rappels",
                            style = if (isSeniorMode) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddModal = true },
                        shape = CircleShape,
                        containerColor = SageDeep,
                        contentColor = Color.White,
                        modifier = Modifier.size(if (isSeniorMode) 54.dp else 44.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Ajouter")
                    }
                }
            }
        },
        containerColor = WarmOffWhite
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compliance Card Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SageDeep)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Suivi Thérapeutique Docta", color = WaterGreenLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Aujourd'hui : $takenCount sur $totalCount pris", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(WaterGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$compliancePercent%", fontWeight = FontWeight.Bold, color = SageDeep, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { compliancePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = WaterGreen,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Reminders Section Title
            item {
                Text(
                    text = "Rappels de Médicaments (Push iOS & Android)",
                    style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            // Reminders Checklist
            items(reminders) { rem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rem.isTakenToday) WaterGreen else SurfaceCard
                    ),
                    border = BorderStroke(1.dp, if (rem.isTakenToday) SageMedium else SurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSeniorMode) 52.dp else 44.dp)
                                    .clip(CircleShape)
                                    .background(if (rem.isTakenToday) SageDeep else WaterGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Medication,
                                    contentDescription = null,
                                    tint = if (rem.isTakenToday) Color.White else SageDeep
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = rem.medicineName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isSeniorMode) 20.sp else 16.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "${rem.dosage} • ${rem.frequency} à ${rem.timeOfDay}",
                                    fontSize = if (isSeniorMode) 15.sp else 12.sp,
                                    color = TextMuted
                                )
                                if (rem.notes.isNotEmpty()) {
                                    Text(
                                        text = "💡 ${rem.notes}",
                                        fontSize = 11.sp,
                                        color = SageDeep,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onToggleTaken(rem.id, rem.isTakenToday) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rem.isTakenToday) SageDeep else Terracotta
                            ),
                            modifier = Modifier.height(if (isSeniorMode) 56.dp else 44.dp)
                        ) {
                            Text(
                                text = if (rem.isTakenToday) "Pris ✓" else "Marquer Pris",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSeniorMode) 16.sp else 12.sp
                            )
                        }
                    }
                }
            }

            // Prescriptions History Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Vos Ordonnances Numériques Certifiées",
                    style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            items(prescriptions) { pres ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Délivrée par : ${pres.doctorName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WaterGreen
                            ) {
                                Text("Signée e-CPS ✓", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = SageDeep, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pres.medicinesSummary, fontSize = 13.sp, color = TextDark)
                        Text(pres.createdAt?.toDisplayDate() ?: "", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onDownloadPrescription(pres) },
                            enabled = pres.pdfPath.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                        ) {
                            Text(if (pres.pdfPath.isBlank()) "PDF en génération…" else "Télécharger le PDF")
                        }
                    }
                }
            }
        }
    }

    // Modal Add Reminder
    if (showAddModal) {
        AlertDialog(
            onDismissRequest = { showAddModal = false },
            title = { Text("Nouveau Rappel de Médicament", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Nom du médicament") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Posologie (ex: 1 gélule)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = timeOfDay,
                        onValueChange = { timeOfDay = it },
                        label = { Text("Heure du rappel (ex: 08:00)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            onAddReminder(medName, dosage, frequency, timeOfDay)
                            showAddModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Programmer le rappel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModal = false }) {
                    Text("Annuler")
                }
            },
            containerColor = WarmOffWhite
        )
    }
}
