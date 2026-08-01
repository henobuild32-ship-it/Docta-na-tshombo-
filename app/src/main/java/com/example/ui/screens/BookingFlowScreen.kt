package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.firebase.FirestoreDoctor
import com.example.ui.theme.*

@Composable
fun BookingFlowScreen(
    practitioner: FirestoreDoctor,
    onConfirmBooking: (FirestoreDoctor, String, String, String, String, String) -> Unit,
    onBack: () -> Unit,
    isSeniorMode: Boolean
) {
    var step by remember { mutableIntStateOf(1) }

    // Motifs list
    val motifs = listOf(
        "Consultation Générale" to "Avis médical complet, diagnostic et orientation",
        "Téléconsultation Suivi" to "Renouvellement d'ordonnance et suivi de traitement",
        "Urgence Légère" to "Fièvre, toux, infection mineure",
        "Certificat Médical" to "Certificat d'aptitude sportive ou professionnelle"
    )
    var selectedMotif by remember { mutableStateOf(motifs.first().first) }

    // Days timeline
    val days = listOf(
        "Jeu 30" to true,
        "Ven 31" to true,
        "Sam 01" to false,
        "Dim 02" to false,
        "Lun 03" to true,
        "Mar 04" to true
    )
    var selectedDay by remember { mutableStateOf("Jeu 30") }

    val times = listOf("09:00", "10:30", "14:00", "15:30", "17:00")
    var selectedTime by remember { mutableStateOf("14:00") }

    var appointmentType by remember { mutableStateOf("TELECONSULTATION") } // TELECONSULTATION or PRESENTIEL
    var userNote by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmOffWhite)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { if (step > 1) step-- else onBack() }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour", tint = TextDark)
            }
            Text("Prise de Rendez-vous • $step/3", fontWeight = FontWeight.Bold, color = SageDeep)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Doctor Card Info Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(WaterGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(practitioner.name.take(4), fontWeight = FontWeight.Bold, color = SageDeep)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(practitioner.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(practitioner.specialty, fontSize = 13.sp, color = TextMuted)
                    if (practitioner.educationSummary.isNotBlank()) {
                        Text(
                            practitioner.educationSummary,
                            fontSize = 11.sp,
                            color = SageMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                        Text(" ${practitioner.rating} • ${practitioner.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SageDeep)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (step) {
            1 -> {
                // Step 1: Selection du motif
                Text(
                    text = "1. Choisissez le motif de consultation",
                    style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    motifs.forEach { (title, desc) ->
                        val isSelected = selectedMotif == title
                        Surface(
                            onClick = { selectedMotif = title },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) SageDeep else SurfaceCard,
                            border = BorderStroke(1.5.dp, if (isSelected) SageDeep else SurfaceCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isSeniorMode) 18.sp else 15.sp,
                                        color = if (isSelected) Color.White else TextDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 12.sp,
                                        color = if (isSelected) WaterGreenLight else TextMuted
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Type of appointment (Présentiel vs Teleconsult)
                Text("Type de consultation :", fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isVideo = appointmentType == "TELECONSULTATION"
                    Surface(
                        onClick = { appointmentType = "TELECONSULTATION" },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isVideo) SageDeep else SurfaceCard,
                        border = BorderStroke(1.dp, SageDeep),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.Videocam, contentDescription = null, tint = if (isVideo) Color.White else SageDeep)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Téléconsultation", color = if (isVideo) Color.White else SageDeep, fontWeight = FontWeight.Bold)
                        }
                    }

                    val isPres = appointmentType == "PRESENTIEL"
                    Surface(
                        onClick = { appointmentType = "PRESENTIEL" },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isPres) SageDeep else SurfaceCard,
                        border = BorderStroke(1.dp, SageDeep),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.Business, contentDescription = null, tint = if (isPres) Color.White else SageDeep)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Présentiel", color = if (isPres) Color.White else SageDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Suivant : Choisir l'heure", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            2 -> {
                // Step 2: Living Calendar Timeline
                Text(
                    text = "2. Sélectionnez la date & l'heure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Days Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(days) { (dayStr, isAvailable) ->
                        val isSelected = selectedDay == dayStr
                        Surface(
                            onClick = { if (isAvailable) selectedDay = dayStr },
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                isSelected -> SageDeep
                                isAvailable -> WaterGreen
                                else -> SurfaceCardBorder
                            },
                            modifier = Modifier.size(68.dp, 80.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayStr.split(" ")[0],
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                                Text(
                                    text = dayStr.split(" ")[1],
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isAvailable) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) TerracottaLight else SageDeep)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Heures disponibles pour le $selectedDay :", fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    times.forEach { timeStr ->
                        val isSelected = selectedTime == timeStr
                        Surface(
                            onClick = { selectedTime = timeStr },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) SageDeep else SurfaceCard,
                            border = BorderStroke(1.dp, if (isSelected) SageDeep else SurfaceCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = timeStr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isSelected) Color.White else TextDark
                                )
                                if (isSelected) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { step = 3 },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Récapitulatif du rendez-vous", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            3 -> {
                // Step 3: Summary Card with Map Preview & Note
                Text(
                    text = "3. Récapitulatif et Confirmation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Premium Business Card Summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.5.dp, SageLight)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Rendez-vous Docta na Tshombo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SageMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(selectedMotif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📅 $selectedDay à $selectedTime ($appointmentType)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SageDeep)

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = SurfaceCardBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Praticien : ${practitioner.name}", fontWeight = FontWeight.Bold)
                        Text("Lieu : ${if (appointmentType == "TELECONSULTATION") "Salon virtuel sécurisé Docta" else practitioner.address}", fontSize = 13.sp, color = TextMuted)
                        Text("Honoraires : ${practitioner.price}", fontSize = 13.sp, color = SageDeep, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userNote,
                    onValueChange = { userNote = it },
                    label = { Text("Note ou symptôme pour le médecin (Optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onConfirmBooking(
                            practitioner,
                            selectedDay,
                            selectedTime,
                            appointmentType,
                            selectedMotif,
                            userNote
                        )
                        showSuccessDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Confirmer le rendez-vous", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Success Confirmation Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBack()
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(WaterGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SageDeep, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rendez-vous Confirmé !", fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            },
            text = {
                Text(
                    "Votre rendez-vous avec ${practitioner.name} pour '$selectedMotif' est enregistré. Vous recevrez un rappel par notification push.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Ajouter à mon calendrier")
                }
            },
            containerColor = WarmOffWhite
        )
    }
}
