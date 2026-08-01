package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.FirestoreAppointment
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Practitioner Dashboard (Pro Mode)
 * - Pro Agenda with timeline grid / list view & color coded appointments
 * - Cabinet & Presence Management toggle ("En consultation" vs "Absent" with sun animation)
 * - Schedule Painter for opening availability slots
 */
@Composable
fun PractitionerHomeScreen(
    doctorName: String = "Dr. Amina Kalala",
    profileImageUri: String? = null,
    onUpdatePhoto: (String) -> Unit = {},
    isPractitionerPresent: Boolean,
    onTogglePresence: () -> Unit,
    appointments: List<FirestoreAppointment>,
    onStartConsultationForPatient: (FirestoreAppointment) -> Unit,
    onNavigateToMessaging: () -> Unit,
    isSeniorMode: Boolean
) {
    var activeFilter by remember { mutableStateOf("Tous") }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onUpdatePhoto(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Pro Header with Liquid Glass surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, GlassBorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, SageDeep, CircleShape)
                            .clickable { photoLauncher.launch("image/*") }
                            .background(WaterGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profileImageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Photo du docteur",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = SageDeep)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (doctorName.startsWith("Dr.")) doctorName else "Dr. $doctorName 👨‍⚕️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cabinet Médical • Certifié iOS 26 Pro",
                            fontSize = 12.sp,
                            color = SageMedium
                        )
                    }
                }

                // Cabinet Presence Toggle (En consultation / Absent)
                Surface(
                    onClick = onTogglePresence,
                    shape = CircleShape,
                    color = if (isPractitionerPresent) SageDeep else Terracotta,
                    modifier = Modifier.shadow(6.dp, CircleShape)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPractitionerPresent) Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPractitionerPresent) "En consultation" else "Absent",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Stats row in Liquid Glass Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(
                modifier = Modifier.weight(1f),
                backgroundColor = SageDeep.copy(alpha = 0.9f),
                borderColor = Color.White.copy(alpha = 0.25f)
            ) {
                Text("Aujourd'hui", fontSize = 11.sp, color = WaterGreenLight, fontWeight = FontWeight.Bold)
                Text("8 Patient(s)", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }

            GlassCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                borderColor = GlassBorderLight
            ) {
                Text("Téléconsultation", fontSize = 11.sp, color = TextMuted)
                Text("5 Prévisites", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Agenda Title & Glass Filters
        Text(
            text = "Agenda Professionnel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tous", "Téléconsultation", "Présentiel").forEach { filter ->
                val isSel = activeFilter == filter
                GlassChip(
                    selected = isSel,
                    onClick = { activeFilter = filter },
                    label = filter,
                    isSeniorMode = isSeniorMode
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Appointments List / Timeline
        if (appointments.isEmpty()) {
            Text("Aucune consultation au programme.", color = TextMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                appointments.forEach { appt ->
                    val isUrgent = appt.motif.contains("Urgence", ignoreCase = true)
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartConsultationForPatient(appt) },
                        backgroundColor = if (isUrgent) TerracottaLight else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        borderColor = if (isUrgent) Terracotta else GlassBorderLight
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏰ ${appt.time}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isUrgent) TerracottaDark else SageDeep
                            )

                            Surface(
                                shape = CircleShape,
                                color = if (appt.type == "TELECONSULTATION") WaterGreen else SageLight.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = appt.type,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageDeep
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Patient : ${appt.patientName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Motif : ${appt.motif}", fontSize = 13.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassPillButton(
                            text = "Ouvrir Dossier & Prescrire",
                            onClick = { onStartConsultationForPatient(appt) },
                            icon = Icons.Outlined.FolderShared,
                            containerColor = iOSPrimaryLight,
                            contentColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Availability Painter Tool
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = WaterGreen.copy(alpha = 0.85f),
            borderColor = SageLight
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.EditCalendar, contentDescription = null, tint = SageDeep)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ouvrir des plages horaires", fontWeight = FontWeight.Bold, color = SageDeep)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Sélectionnez les heures pour 'peindre' vos créneaux en consultation.", fontSize = 12.sp, color = TextDark)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("08:00 - 12:00", "14:00 - 18:00", "Téléconsult 19:00").forEach { slot ->
                    Surface(
                        shape = CircleShape,
                        color = SageDeep
                    ) {
                        Text(slot, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

}
