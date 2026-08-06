package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.FirestoreUser
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    userProfile: FirestoreUser?,
    onUpdatePhoto: (String) -> Unit,
    onUpdateInfo: (String, String, String) -> Unit,
    onChangePassword: (String, String, (Boolean) -> Unit) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    isSeniorMode: Boolean = false
) {
    val profile = userProfile
    var editMode by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var firstName by remember(profile?.uid) { mutableStateOf(profile?.firstName.orEmpty()) }
    var lastName by remember(profile?.uid) { mutableStateOf(profile?.lastName.orEmpty()) }
    var phone by remember(profile?.uid) { mutableStateOf(profile?.phone.orEmpty()) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onUpdatePhoto(uri.toString())
    }

    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour") }
                Text("Paramètres", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = WarmOffWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Carte profil ----
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                borderColor = GlassBorderLight
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(WaterGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile?.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile?.photoUrl,
                                contentDescription = "Photo de profil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                profile?.displayName?.take(2).orEmpty().ifBlank { "🙂" },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageDeep
                            )
                        }
                    }
                    TextButton(onClick = { photoLauncher.launch("image/*") }) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Changer la photo", color = SageDeep, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(profile?.displayName ?: "Utilisateur", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        if (profile?.role == FirestoreUser.ROLE_DOCTOR) "Praticien" else "Patient",
                        fontSize = 13.sp,
                        color = SageDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(profile?.email.orEmpty(), fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center)
                    Text(profile?.phone?.ifBlank { "Téléphone non renseigné" } ?: "Téléphone non renseigné", fontSize = 13.sp, color = TextMuted)
                }
            }

            // ---- Informations personnelles ----
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                borderColor = GlassBorderLight
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Informations personnelles", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        TextButton(onClick = { editMode = !editMode }) {
                            Text(if (editMode) "Annuler" else "Modifier", color = SageDeep, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (editMode) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("Prénom") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Téléphone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Button(
                            onClick = {
                                onUpdateInfo(firstName, lastName, phone)
                                editMode = false
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                        ) {
                            Text("Enregistrer les modifications", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Prénom : ${profile?.firstName?.ifBlank { "Non renseigné" } ?: "Non renseigné"}", fontSize = 14.sp)
                        Text("Nom : ${profile?.lastName?.ifBlank { "Non renseigné" } ?: "Non renseigné"}", fontSize = 14.sp)
                        Text("Téléphone : ${profile?.phone?.ifBlank { "Non renseigné" } ?: "Non renseigné"}", fontSize = 14.sp)
                    }
                }
            }

            // ---- Sécurité ----
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                borderColor = GlassBorderLight
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Sécurité", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedButton(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SageDeep)
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = SageDeep)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Changer le mot de passe", color = SageDeep, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // ---- Déconnexion ----
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
            ) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Se déconnecter", fontWeight = FontWeight.Bold)
            }

            Text(
                "Version ${com.example.BuildConfig.VERSION_NAME}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onChangePassword = { current, new, done ->
                onChangePassword(current, new, done)
            },
            onDismiss = { showPasswordDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Se déconnecter ?", fontWeight = FontWeight.Bold) },
            text = { Text("Vous retournerez à l'écran d'accueil. Votre session restera enregistrée sur cet appareil.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Se déconnecter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") }
            },
            containerColor = WarmOffWhite
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onChangePassword: (String, String, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Changer le mot de passe", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mot de passe actuel") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nouveau mot de passe") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmer le nouveau mot de passe") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = Terracotta, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPassword.isBlank() -> error = "Veuillez saisir votre mot de passe actuel."
                        newPassword.length < 6 -> error = "Le nouveau mot de passe doit contenir au moins 6 caractères."
                        newPassword != confirmPassword -> error = "Les nouveaux mots de passe ne correspondent pas."
                        else -> onChangePassword(currentPassword, newPassword) { success ->
                            if (success) onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
            ) {
                Text("Modifier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
        containerColor = WarmOffWhite
    )
}
