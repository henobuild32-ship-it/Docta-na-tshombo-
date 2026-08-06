package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.R
import com.example.data.firebase.FirestoreAppointment
import com.example.data.firebase.FirestoreDoctor
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PatientHomeScreen(
    userName: String,
    profileImageUri: String? = null,
    onUpdatePhoto: (String) -> Unit = {},
    upcomingAppointments: List<FirestoreAppointment>,
    allPractitioners: List<FirestoreDoctor>,
    isSeniorMode: Boolean,
    onToggleSeniorMode: () -> Unit,
    onSelectPractitioner: (FirestoreDoctor) -> Unit,
    onMessagePractitioner: (FirestoreDoctor) -> Unit,
    onStartTeleconsult: (FirestoreDoctor) -> Unit,
    onCancelAppointment: (String) -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToMessaging: () -> Unit,
    onNavigateToAppointmentsList: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    onOpenPresentation: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("Tous") }
    val filterChips = listOf("Tous", "Téléconsultation", "Présentiel", "Pédiatrie", "Généraliste")

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onUpdatePhoto(uri.toString())
        }
    }

    // Filtered practitioners list
    val filteredPractitioners = remember(searchQuery, selectedFilterChip, allPractitioners) {
        allPractitioners.filter { doc ->
            val matchesQuery = doc.name.contains(searchQuery, ignoreCase = true) ||
                    doc.specialty.contains(searchQuery, ignoreCase = true)
            val matchesChip = when (selectedFilterChip) {
                "Téléconsultation" -> doc.isOnlineForTeleconsult
                "Pédiatrie" -> doc.specialty.contains("Pédiatre", ignoreCase = true)
                "Généraliste" -> doc.specialty.contains("Généraliste", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesChip
        }
    }

    var expandedDoctorId by remember { mutableStateOf<String?>(null) }
    var showCancelDialogForId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            OrganicHeader(
                userName = userName,
                greetingEmoji = "🌤️",
                isSeniorMode = isSeniorMode,
                onToggleSeniorMode = onToggleSeniorMode,
                profileImageUri = profileImageUri,
                onAvatarClick = { photoLauncher.launch("image/*") },
                onOpenPresentation = onOpenPresentation,
                onOpenSettings = onOpenSettings
            )
        },
        bottomBar = {
            // Floating Liquid Glass Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.2f)),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Item 1: Home
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { /* Déjà sur l'accueil */ }
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(iOSPrimaryLight)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Home, contentDescription = "Accueil", tint = Color.White)
                            }
                            Text("Accueil", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = iOSPrimaryLight)
                        }

                        // Item 2: Mes RDV
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onNavigateToAppointmentsList() }
                        ) {
                            Icon(Icons.Outlined.EventNote, contentDescription = "RDV", tint = TextMuted)
                            Text("Mes RDV", fontSize = 11.sp, color = TextMuted)
                        }

                        // Item 3: Suivi Traitement
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onNavigateToReminders() }
                        ) {
                            Icon(Icons.Outlined.Medication, contentDescription = "Soin", tint = TextMuted)
                            Text("Traitements", fontSize = 11.sp, color = TextMuted)
                        }

                        // Item 4: Messagerie
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onNavigateToMessaging() }
                        ) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Chat", tint = TextMuted)
                            Text("Messages", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ✨ Element Signature Liquid Glass Health Hero Card (~40% Screen Height Wow Factor)
            LiquidGlassHealthHeroCard(
                userName = userName,
                healthScore = 96,
                spO2 = "98%",
                heartRate = "74 bpm",
                bp = "12/8",
                onStartTeleconsult = {
                    if (allPractitioners.isNotEmpty()) {
                        onStartTeleconsult(allPractitioners.first())
                    }
                },
                onBookAppointment = onNavigateToAppointmentsList,
                isSeniorMode = isSeniorMode
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Universal Glass Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, GlassBorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = "Chercher", tint = iOSPrimaryLight)
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Chercher un praticien, motif, spécialité...",
                                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                color = TextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Effacer", tint = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glass Filter Chips Horizontal Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterChips) { chip ->
                    val isSelected = selectedFilterChip == chip
                    GlassChip(
                        selected = isSelected,
                        onClick = { selectedFilterChip = chip },
                        label = chip,
                        isSeniorMode = isSeniorMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Section "Vos prochains rendez-vous"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vos prochains rendez-vous",
                    style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                TextButton(onClick = onNavigateToAppointmentsList) {
                    Text("Tout voir", color = SageDeep, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (upcomingAppointments.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = WaterGreen
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.EventAvailable, contentDescription = null, tint = SageDeep)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Aucun rendez-vous à venir. Prenez rendez-vous en quelques clics !",
                            fontSize = 13.sp,
                            color = TextDark
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(upcomingAppointments) { appt ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .combinedClickable(
                                    onClick = { onNavigateToAppointmentsList() },
                                    onLongClick = { showCancelDialogForId = appt.id }
                                ),
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
                                    // Pill Tag: Video (Blue) vs In-Person (Green)
                                    val isVideo = appt.type == "TELECONSULTATION"
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isVideo) Color(0xFFE3F2FD) else WaterGreen
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isVideo) Icons.Outlined.Videocam else Icons.Outlined.Business,
                                                contentDescription = null,
                                                tint = if (isVideo) Color(0xFF1565C0) else SageDeep,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isVideo) "Vidéo" else "Présentiel",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isVideo) Color(0xFF1565C0) else SageDeep
                                            )
                                        }
                                    }

                                    Text(
                                        text = appt.time,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SageDeep
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(WaterGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(appt.doctorName.take(4), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SageDeep)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(appt.doctorName, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(appt.doctorSpecialty, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "📅 ${appt.date} • ${appt.motif}",
                                    fontSize = 12.sp,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (appt.type == "TELECONSULTATION") {
                                    Button(
                                        onClick = {
                                            val doc = allPractitioners.find { it.id == appt.doctorId }
                                            onStartTeleconsult(doc ?: allPractitioners.first())
                                        },
                                        modifier = Modifier.fillMaxWidth().height(38.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                                    ) {
                                        Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Rejoindre la téléconsultation", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Section "Découvrir la téléconsultation" (Banner)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (allPractitioners.isNotEmpty()) onStartTeleconsult(allPractitioners.first())
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SageDeep)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WaterGreen
                        ) {
                            Text("Consultation à distance", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = SageDeep, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Découvrir la téléconsultation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Espace de soin virtuel chiffré et ordonnance numérique instantanée.", color = WaterGreenLight, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Image(
                        painter = painterResource(id = R.drawable.img_teleconsult_banner_1785434984773),
                        contentDescription = "Téléconsultation",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Practitioner Accordion List
            Text(
                text = "Médecins disponibles (${filteredPractitioners.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredPractitioners.forEach { doc ->
                    val isExpanded = expandedDoctorId == doc.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(WaterGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(doc.name.take(4), fontWeight = FontWeight.Bold, color = SageDeep)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            doc.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            doc.specialty,
                                            fontSize = 12.sp,
                                            color = TextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (doc.educationSummary.isNotBlank()) {
                                            Text(
                                                doc.educationSummary,
                                                fontSize = 11.sp,
                                                color = SageMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                                            Text(" ${doc.rating} (${doc.reviewCount} avis)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "📍 ${doc.address}",
                                fontSize = 12.sp,
                                color = TextDark,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { onSelectPractitioner(doc) },
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                                ) {
                                    Text("Prendre rendez-vous", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = { onMessagePractitioner(doc) },
                                    modifier = Modifier.height(42.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, SageMedium)
                                ) {
                                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = SageDeep)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Message", fontSize = 12.sp, color = SageDeep)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(onClick = { expandedDoctorId = if (isExpanded) null else doc.id }) {
                                    Text(
                                        if (isExpanded) "Masquer les créneaux ▲" else "Voir les créneaux ▼",
                                        fontSize = 12.sp,
                                        color = SageDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Accordion Content (Slots)
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp)
                                ) {
                                    Divider(color = SurfaceCardBorder)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Prochains créneaux disponibles :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val slots = doc.availableSlots.mapNotNull { slot ->
                                            slot.split(" ").getOrNull(1)
                                        }.take(4)
                                        if (slots.isEmpty()) {
                                            Text(
                                                text = "Aucun créneau publié pour le moment",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        } else {
                                            slots.forEach { time ->
                                                Surface(
                                                    onClick = { onSelectPractitioner(doc) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = WaterGreen,
                                                    border = BorderStroke(1.dp, SageLight)
                                                ) {
                                                    Text(
                                                        text = time,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SageDeep
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cancel appointment confirmation modal
    if (showCancelDialogForId != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialogForId = null },
            title = { Text("Annuler le rendez-vous ?", fontWeight = FontWeight.Bold) },
            text = { Text("Êtes-vous sûr de vouloir annuler ce rendez-vous ? L'action est immédiate.") },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelAppointment(showCancelDialogForId!!)
                        showCancelDialogForId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Oui, Annuler")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialogForId = null }) {
                    Text("Garder le RDV")
                }
            },
            containerColor = WarmOffWhite
        )
    }
}
