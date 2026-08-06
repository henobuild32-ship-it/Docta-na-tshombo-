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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.DoctorEducation
import com.example.ui.theme.*

/**
 * Practitioner Registration Wizard (4 steps)
 * 1. Identité et Spécialité (Specialty picker & Doctor photo upload)
 * 2. Parcours & Formation (Diplôme, niveau d'étude, universités, formations)
 * 3. Documents Professionnels (Optionnel — faisable plus tard)
 * 4. Vérification et Engagement (RPPS/Adeli timeline verification)
 */

/** Année courante compatible Android 7+ (sans java.time). */
private fun currentYear(): Int =
    try {
        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    } catch (_: Exception) {
        java.util.GregorianCalendar().get(java.util.Calendar.YEAR)
    }
@Composable
fun PractitionerOnboardingWizard(
    onComplete: (
        doctorName: String,
        specialty: String,
        education: DoctorEducation,
        rppsNumber: String,
        photoUri: String?,
        documentUri: String?,
        email: String,
        password: String
    ) -> Unit,
    onBackToLanding: () -> Unit,
    isSeniorMode: Boolean,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var step by remember { mutableIntStateOf(1) }

    // Form states - Empty defaults (no pre-filled fake data)
    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var rppsNumber by remember { mutableStateOf("") }

    // Parcours & Formation
    var degree by remember { mutableStateOf("") }
    var educationLevel by remember { mutableStateOf("") }
    var studyDuration by remember { mutableStateOf("") }
    var universityInput by remember { mutableStateOf("") }
    var universities by remember { mutableStateOf(emptyList<String>()) }
    var trainingInput by remember { mutableStateOf("") }
    var trainings by remember { mutableStateOf(emptyList<String>()) }
    var graduationYear by remember { mutableStateOf("") }

    // Documents (optionnel)
    var documentUri by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            documentUri = uri.toString()
        }
    }

    val specialtiesList = listOf(
        "Médecin Généraliste", "Pédiatre", "Cardiologue",
        "Dermatologue", "Gynécologue-Obstétricien", "Ophtalmologue", "Neurologue"
    )

    val educationLevelsList = listOf(
        "Bac+5 (Master)", "Bac+6", "Bac+7 (Doctorat)", "Bac+8+ (Spécialisation/PhD)"
    )

    val graduationYears = (1970..currentYear()).toList().reversed()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmOffWhite)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { if (step > 1) step-- else onBackToLanding() }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour", tint = TextDark)
            }
            Text("Espace Praticien • Étape $step/4", fontWeight = FontWeight.Bold, color = SageDeep)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (step) {
            1 -> {
                Text(
                    text = "Identité & Spécialité",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Doctor Photo Picker
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
                                contentDescription = "Photo du médecin",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = "Photo professionnelle",
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
                        text = if (photoUri == null) "Photo de profil professionnelle (recommandée)" else "✓ Photo médicale sélectionnée",
                        fontSize = 12.sp,
                        color = SageMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Nom complet avec Titre (ex: Dr. Kabeya)") },
                    placeholder = { Text("Dr. Prénom Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Spécialité médicale", fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    specialtiesList.forEach { item ->
                        val isSelected = specialty == item
                        Surface(
                            onClick = { specialty = item },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) SageDeep else SurfaceCard,
                            border = BorderStroke(1.dp, if (isSelected) SageDeep else SurfaceCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item,
                                    color = if (isSelected) Color.White else TextDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { step = 2 },
                    enabled = doctorName.isNotBlank() && specialty.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Continuer vers le parcours")
                }
            }

            2 -> {
                Text(
                    text = "Parcours & Formation",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Renseignez votre niveau d'étude, vos diplômes et vos formations.", color = TextMuted)

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Diplôme principal") },
                    placeholder = { Text("Ex: Doctorat en Médecine") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Niveau d'étude", fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    educationLevelsList.forEach { level ->
                        val isSelected = educationLevel == level
                        Surface(
                            onClick = { educationLevel = if (isSelected) "" else level },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) SageDeep else SurfaceCard,
                            border = BorderStroke(1.dp, if (isSelected) SageDeep else SurfaceCardBorder)
                        ) {
                            Text(
                                text = level,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = studyDuration,
                    onValueChange = { studyDuration = it },
                    label = { Text("Durée d'étude") },
                    placeholder = { Text("Ex: 7 ans") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = graduationYear,
                    onValueChange = { graduationYear = it },
                    label = { Text("Année d'obtention du diplôme") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Universités (liste à puces)
                Text("Universités / Facultés", fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = universityInput,
                        onValueChange = { universityInput = it },
                        label = { Text("Ex: Université de Kinshasa") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = universityInput.trim()
                            if (trimmed.isNotBlank() && trimmed !in universities) {
                                universities = universities + trimmed
                            }
                            universityInput = ""
                        },
                        enabled = universityInput.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Ajouter")
                    }
                }
                universities.forEach { uni ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WaterGreen.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎓 $uni", fontSize = 13.sp, color = TextDark, modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Retirer",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { universities = universities - uni }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Formations complémentaires
                Text("Formations complémentaires", fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = trainingInput,
                        onValueChange = { trainingInput = it },
                        label = { Text("Ex: Cardiologie interventionnelle") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = trainingInput.trim()
                            if (trimmed.isNotBlank() && trimmed !in trainings) {
                                trainings = trainings + trimmed
                            }
                            trainingInput = ""
                        },
                        enabled = trainingInput.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Ajouter")
                    }
                }
                trainings.forEach { training ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WaterGreen.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📚 $training", fontSize = 13.sp, color = TextDark, modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Retirer",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { trainings = trainings - training }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { step = 3 },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text("Continuer vers les documents")
                }
            }

            3 -> {
                Text(
                    text = "Documents Professionnels",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Téléversez votre carte CPS ou votre Diplôme médical pour accélérer la validation. Cette étape est facultative : vous pourrez ajouter vos documents plus tard depuis votre espace.",
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Dropzone area with shield icon (OPTIONAL — can be skipped)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(WaterGreen)
                        .border(2.dp, if (documentUri != null) SageDeep else SageMedium, RoundedCornerShape(24.dp))
                        .clickable { docPickerLauncher.launch("*/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = "Dropzone",
                            tint = SageDeep,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (documentUri == null) "Appuyez pour scanner/charger votre carte CPS ou diplôme" else "✓ Document sélectionné",
                            fontWeight = FontWeight.Bold,
                            color = SageDeep,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (documentUri != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Touchez pour changer le fichier",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Vous pouvez ignorer cette étape et la compléter plus tard.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { step = 4 },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    Text(if (documentUri != null) "Étape finale de vérification" else "Continuer (sans document)")
                }
            }

            4 -> {
                Text(
                    text = "Vérification et Engagement",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = rppsNumber,
                    onValueChange = { rppsNumber = it },
                    label = { Text("Numéro RPPS / CNOM") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Adresse e-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe sécurisé") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Verification Timeline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Processus de certification :", fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("1. Soumission du dossier RPPS", color = SageDeep)
                        Text("2. Vérification par notre conseil médical (sous 24h)", color = TextMuted)
                        Text("3. Activation de la téléconsultation certifiée", color = TextMuted)
                        if (documentUri == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Note : documents non fournis — vous les ajouterez plus tard depuis votre espace praticien.",
                                fontSize = 12.sp,
                                color = TerracottaDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

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
                    onClick = {
                        val education = DoctorEducation(
                            degree = degree.trim(),
                            educationLevel = educationLevel,
                            studyDuration = studyDuration.trim(),
                            universities = universities,
                            trainings = trainings,
                            graduationYear = graduationYear.trim()
                        )
                        onComplete(
                            doctorName, specialty, education, rppsNumber,
                            photoUri, documentUri, email, password
                        )
                    },
                    enabled = !isLoading && doctorName.isNotBlank() && specialty.isNotBlank() && email.isNotBlank() && password.length >= 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Soumettre pour vérification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
