package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.firebase.FirestoreAppointment
import com.example.ui.theme.*

@Composable
fun PractitionerConsultationScreen(
    appointment: FirestoreAppointment?,
    onIssuePrescription: (String, String) -> Unit,
    onBack: () -> Unit,
    isSeniorMode: Boolean
) {
    var medicineName by remember { mutableStateOf("Amoxicilline 500mg") }
    var dosage by remember { mutableStateOf("1 gélule matin et soir") }
    var duration by remember { mutableStateOf("7 jours") }
    var prescriptionNote by remember { mutableStateOf("À prendre à la fin du repas.") }

    var isPrescriptionSigned by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmOffWhite)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour", tint = TextDark)
            }
            Text("Espace de Soin Praticien", fontWeight = FontWeight.Bold, color = SageDeep)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Patient File Header Card with Red Allergies Alert Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Joseph Makula", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                        Text("43 ans • Homme • Groupe O+", fontSize = 13.sp, color = TextMuted)
                    }

                    // Allergies Alert Badge (Red)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = TerracottaLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Terracotta)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Allergie : Pénicilline forte", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = SurfaceCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Antécédents médicaux :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("• Hypertension artérielle modérée (Suivi sous Amlodipine)", fontSize = 12.sp, color = TextDark)
                Text("• Motif RDV du jour : ${appointment?.motif ?: "Consultation de suivi"}", fontSize = 12.sp, color = SageDeep, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Digital Prescription Builder Form
        Text(
            text = "Générateur d'Ordonnance Numérique",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Docta na Tshombo • Watermark Certified e-CPS", fontSize = 11.sp, color = SageMedium, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("Médicament (Recherche prédictive)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Posologie") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Durée") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = prescriptionNote,
                    onValueChange = { prescriptionNote = it },
                    label = { Text("Consignes particulières pour le patient") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Prescription Preview Summary Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = WaterGreen
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Aperçu de l'ordonnance :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SageDeep)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1. $medicineName - $dosage (Durée : $duration)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Note : $prescriptionNote", fontSize = 12.sp, color = TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val fullPrescription = "1. $medicineName ($dosage, pendant $duration)\nNote: $prescriptionNote"
                        onIssuePrescription(fullPrescription, "Dr. Amina Kalala")
                        isPrescriptionSigned = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(6.dp, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPrescriptionSigned) SageMedium else SageDeep
                    )
                ) {
                    Icon(
                        imageVector = if (isPrescriptionSigned) Icons.Outlined.CheckCircle else Icons.Outlined.Draw,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPrescriptionSigned) "Signée e-CPS & Transmise !" else "Signer e-CPS & Envoyer au Patient",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
