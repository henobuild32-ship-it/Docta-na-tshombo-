package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sageDeep = Color(0xFF2C4531)
    val sageLight = Color(0xFFEAF1EC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("À Propos & Informations Légales", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = sageDeep)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(sageLight)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = sageDeep,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Docta na Tshombo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = sageDeep
                    )

                    Text(
                        text = "Version 1.2.0 (Build 42)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = sageDeep)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Créateur & Éditeur", fontSize = 12.sp, color = Color.Gray)
                            Text("Henock Aduma", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = sageDeep)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = sageDeep)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Contact & Assistance", fontSize = 12.sp, color = Color.Gray)
                            Text("henockaduma2@gmail.com", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC96F4A))
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = "Fonctionnalités Principales :",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = sageDeep,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val features = listOf(
                        "✅ Rendez-vous en présentiel & téléconsultation visio direct",
                        "✅ Scan des constantes vitales par torche (BPM & SpO2)",
                        "✅ Messagerie sécurisée en temps réel",
                        "✅ Générateur & partage d'ordonnances numériques",
                        "✅ Scan & validation automatique des médecins (15 sec)"
                    )

                    features.forEach { ft ->
                        Text(
                            text = ft,
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "© 2026 Henock Aduma. Tous droits réservés.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
