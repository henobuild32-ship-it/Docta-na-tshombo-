package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.firebase.FirestoreDoctor
import com.example.ui.components.MoleculeLoadingAnimation
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun TeleconsultationScreen(
    practitioner: FirestoreDoctor,
    onEndCall: () -> Unit,
    isSeniorMode: Boolean
) {
    var inWaitingRoom by remember { mutableStateOf(true) }

    var isMicOn by remember { mutableStateOf(true) }
    var isCameraOn by remember { mutableStateOf(true) }
    var activePanel by remember { mutableStateOf<String?>(null) } // "CHAT" or "DOCS"

    // Draggable Picture-in-Picture patient self-view offset
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var chatMessageInput by remember { mutableStateOf("") }
    val chatMessagesList = remember {
        mutableStateListOf(
            "Dr. Amina Kalala" to "Bonjour Joseph, je démarre la téléconsultation.",
            "Joseph Makula" to "Bonjour Docteur, je suis bien là."
        )
    }

    if (inWaitingRoom) {
        // Salle d'Attente Virtuelle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmOffWhite)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_waiting_room_1785434998112),
                    contentDescription = "Salle d'attente",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Salle d'Attente Virtuelle",
                    style = if (isSeniorMode) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${practitioner.name} va vous rejoindre.\nPrenez place, vous êtes dans un espace sécurisé.",
                    style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                MoleculeLoadingAnimation(color = SageDeep)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { inWaitingRoom = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(if (isSeniorMode) 60.dp else 52.dp)
                        .shadow(8.dp, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Entrer dans la consultation", fontSize = if (isSeniorMode) 18.sp else 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onEndCall) {
                    Text("Quitter la salle d'attente", color = Terracotta)
                }
            }
        }
    } else {
        // Active Consultation Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF142018))
        ) {
            // Main Doctor Stream (Full screen video)
            Image(
                painter = painterResource(id = R.drawable.img_doctor_hero_1785434970139),
                contentDescription = "Doctor Video Stream",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SageDeep.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("En direct • ${practitioner.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                IconButton(onClick = { activePanel = if (activePanel == "CHAT") null else "CHAT" }) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Chat, contentDescription = "Chat", tint = Color.White)
                    }
                }
            }

            // Draggable Floating Patient PIP (Self-View)
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .padding(20.dp)
                    .align(Alignment.BottomStart)
                    .size(110.dp, 150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, WaterGreen, RoundedCornerShape(20.dp))
                    .background(Color.DarkGray)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCameraOn) {
                    Text("Vous (Vidéo)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Outlined.VideocamOff, contentDescription = null, tint = Color.White)
                }
            }

            // Glassmorphic Control Bar (Mic, Cam, End Call Terracotta)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = WarmOffWhite.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mic toggle
                    IconButton(
                        onClick = { isMicOn = !isMicOn },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isMicOn) WaterGreen else TerracottaLight)
                    ) {
                        Icon(
                            imageVector = if (isMicOn) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                            contentDescription = "Microphone",
                            tint = if (isMicOn) SageDeep else Terracotta
                        )
                    }

                    // Cam toggle
                    IconButton(
                        onClick = { isCameraOn = !isCameraOn },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isCameraOn) WaterGreen else TerracottaLight)
                    ) {
                        Icon(
                            imageVector = if (isCameraOn) Icons.Outlined.Videocam else Icons.Outlined.VideocamOff,
                            contentDescription = "Caméra",
                            tint = if (isCameraOn) SageDeep else Terracotta
                        )
                    }

                    // Document Sharing
                    IconButton(
                        onClick = { activePanel = if (activePanel == "DOCS") null else "DOCS" },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(WaterGreen)
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = "Documents", tint = SageDeep)
                    }

                    // End Call Button (Terracotta)
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Terracotta)
                    ) {
                        Icon(Icons.Outlined.CallEnd, contentDescription = "Raccrocher", tint = Color.White)
                    }
                }
            }

            // Side Panel for Chat or Documents
            AnimatedVisibility(
                visible = activePanel != null,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WarmOffWhite,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (activePanel == "CHAT") "Chat sécurisé" else "Documents partagés",
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            IconButton(onClick = { activePanel = null }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Fermer")
                            }
                        }

                        Divider(color = SurfaceCardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (activePanel == "CHAT") {
                            Column(modifier = Modifier.weight(1f)) {
                                chatMessagesList.forEach { (sender, msg) ->
                                    val isMe = sender == "Joseph Makula"
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isMe) SageDeep else WaterGreen,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = msg,
                                                modifier = Modifier.padding(10.dp),
                                                color = if (isMe) Color.White else TextDark,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = chatMessageInput,
                                    onValueChange = { chatMessageInput = it },
                                    placeholder = { Text("Message...") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                IconButton(onClick = {
                                    if (chatMessageInput.isNotBlank()) {
                                        chatMessagesList.add("Joseph Makula" to chatMessageInput)
                                        chatMessageInput = ""
                                    }
                                }) {
                                    Icon(Icons.Outlined.Send, contentDescription = "Envoyer", tint = SageDeep)
                                }
                            }
                        } else {
                            // Documents panel
                            Text("Documents médicaux reçus pendant l'appel :", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WaterGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = SageDeep)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Ordonnance_Numerique.pdf", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Reçue à l'instant • Signée e-CPS", fontSize = 10.sp, color = SageMedium)
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
