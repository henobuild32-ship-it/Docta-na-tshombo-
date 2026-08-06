package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.R
import com.example.data.models.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Screen 1: Non-Connected Landing Page
 * "Fond en dégradé vertical très lent du vert sauge (haut) vers le blanc cassé (bas).
 * Au centre, le logo de Docta na Tshombo : un cœur stylisé dont la pointe se transforme en une feuille"
 */
@Composable
fun OnboardingLandingScreen(
    onSelectRole: (UserRole) -> Unit,
    onLoginClick: () -> Unit,
    isSeniorMode: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SageDeep,
                        SageMedium,
                        WarmOffWhite
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Center Logo & Slogan
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(16.dp, CircleShape),
                    shape = CircleShape,
                    color = WarmOffWhite
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_1785434955616),
                            contentDescription = "Logo Docta na Tshombo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Docta na Tshombo",
                    style = if (isSeniorMode) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Votre bien-être, notre rythme",
                    style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    color = WaterGreenLight,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Molecule loading decoration
                MoleculeLoadingAnimation(color = WaterGreen)
            }

            // Two Liquid Glass pill buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button 1: Patient
                GlassPillButton(
                    text = "Je suis Patient",
                    onClick = { onSelectRole(UserRole.PATIENT) },
                    icon = Icons.Outlined.Person,
                    containerColor = SageDeep,
                    contentColor = Color.White,
                    isSeniorMode = isSeniorMode,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                // Button 2: Practitioner
                GlassPillButton(
                    text = "Je suis Professionnel de Santé",
                    onClick = { onSelectRole(UserRole.PRATICIEN) },
                    icon = Icons.Outlined.MedicalServices,
                    containerColor = Color.White.copy(alpha = 0.25f),
                    contentColor = Color.White,
                    isSeniorMode = isSeniorMode,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                // Existing account link
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = "J'ai déjà un compte • Se connecter",
                        color = WaterGreenLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSeniorMode) 18.sp else 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // App Signature
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "✨ Application conçue & développée par Henock Aduma",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Patient Registration Wizard (3 steps)
 * Step 1: Identité
 * Step 2: Contact & Sécurité (avec indicateur de force du mot de passe)
 * Step 3: Finalisation (with interactive policy toggles)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientOnboardingWizard(
    onComplete: (String, String, String, String, String, String?) -> Unit,
    onBackToLanding: () -> Unit,
    isSeniorMode: Boolean,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var step by remember { mutableIntStateOf(1) }

    // Form states - Empty defaults (no hardcoded fake user data)
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    var acceptTerms by remember { mutableStateOf(true) }
    var acceptPrivacy by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmOffWhite)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Navigation & Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { if (step > 1) step-- else onBackToLanding() }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour", tint = TextDark)
            }

            Text(
                text = "Étape $step sur 3",
                fontWeight = FontWeight.Bold,
                color = SageMedium,
                fontSize = if (isSeniorMode) 18.sp else 14.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress indicators (3 circles connected by a line)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1..3) {
                Box(
                    modifier = Modifier
                        .size(if (step == i) 32.dp else 24.dp)
                        .clip(CircleShape)
                        .background(if (i <= step) SageDeep else SurfaceCardBorder),
                    contentAlignment = Alignment.Center
                ) {
                    if (i < step) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            text = "$i",
                            color = if (i <= step) Color.White else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                if (i < 3) {
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp),
                        color = if (i < step) SageDeep else SurfaceCardBorder
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (step) {
            1 -> {
                // Step 1: Identité
                Text(
                    text = "Créons votre espace santé",
                    style = if (isSeniorMode) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Avatar picker box
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(WaterGreen)
                            .border(2.5.dp, SageDeep, CircleShape)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Photo de profil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = "Ajouter photo",
                                    tint = SageDeep,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Photo", fontSize = 11.sp, color = SageDeep, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (photoUri == null) "Ajouter votre photo de profil (optionnel)" else "✓ Photo de profil sélectionnée",
                        fontSize = 12.sp,
                        color = SageMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Form fields with rounded squircle containers
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Prénom") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageDeep,
                        unfocusedBorderColor = SurfaceCardBorder
                    ),
                    trailingIcon = {
                        if (firstName.isNotBlank()) Icon(Icons.Outlined.Check, contentDescription = null, tint = SageDeep)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nom de famille") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageDeep,
                        unfocusedBorderColor = SurfaceCardBorder
                    ),
                    trailingIcon = {
                        if (lastName.isNotBlank()) Icon(Icons.Outlined.Check, contentDescription = null, tint = SageDeep)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Date de naissance — sélecteur jour/mois/année (JJ/MM/AAAA)
                Box {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date de naissance (JJ/MM/AAAA)") },
                        placeholder = { Text("Jour / Mois / Année") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = SageMedium)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageDeep,
                            unfocusedBorderColor = SurfaceCardBorder
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showBirthDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { step = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSeniorMode) 60.dp else 52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Continuer", fontSize = if (isSeniorMode) 20.sp else 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            2 -> {
                // Step 2: Contact & Sécurité
                Text(
                    text = "Contact & Sécurité",
                    style = if (isSeniorMode) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Adresse e-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageDeep,
                        unfocusedBorderColor = SurfaceCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone mobile") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageDeep,
                        unfocusedBorderColor = SurfaceCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password field with pebbles strength indicator
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe sécurisé") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Afficher mot de passe"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageDeep,
                        unfocusedBorderColor = SurfaceCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password strength indicator: line of 3 rounded pebbles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Force :", fontSize = 12.sp, color = TextMuted)
                    val pebblesCount = when {
                        password.length > 10 -> 3
                        password.length > 6 -> 2
                        password.isNotEmpty() -> 1
                        else -> 0
                    }
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        i <= pebblesCount && pebblesCount == 3 -> SageDeep
                                        i <= pebblesCount && pebblesCount == 2 -> SageMedium
                                        i <= pebblesCount && pebblesCount == 1 -> Terracotta
                                        else -> SurfaceCardBorder
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { step = 3 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSeniorMode) 60.dp else 52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Étape suivante", fontSize = if (isSeniorMode) 20.sp else 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            3 -> {
                // Step 3: Finalisation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.img_teleconsult_banner_1785434984773),
                        contentDescription = "Illustration santé",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Vous y êtes presque, $firstName.",
                        style = if (isSeniorMode) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms Card 1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Conditions Générales", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("J'accepte les règles d'utilisation de Docta na Tshombo.", fontSize = 12.sp, color = TextMuted)
                            }
                            Switch(
                                checked = acceptTerms,
                                onCheckedChange = { acceptTerms = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SageDeep)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Terms Card 2
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Protection des Données", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Vos données médicales sont chiffrées de bout en bout.", fontSize = 12.sp, color = TextMuted)
                            }
                            Switch(
                                checked = acceptPrivacy,
                                onCheckedChange = { acceptPrivacy = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SageDeep)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = { onComplete(firstName, lastName, email, phone, password, photoUri) },
                        enabled = !isLoading && acceptTerms && acceptPrivacy && password.length >= 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isSeniorMode) 64.dp else 56.dp)
                            .shadow(8.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageDeep,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Ouvrir mon Docta na Tshombo",
                                fontSize = if (isSeniorMode) 20.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Date de naissance — sélecteur calendrier jour / mois / année
    if (showBirthDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showBirthDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        val day = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                        val month = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                        val year = cal.get(java.util.Calendar.YEAR).toString()
                        birthDate = "$day/$month/$year"
                    }
                    showBirthDatePicker = false
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}
