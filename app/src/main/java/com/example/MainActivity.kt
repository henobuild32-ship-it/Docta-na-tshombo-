package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.FirestoreUser
import com.example.ui.components.PushNotificationBanner
import com.example.ui.components.RefreshingOverlay
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.DoctaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DoctaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val selectedDoctor by viewModel.selectedPractitioner.collectAsStateWithLifecycle()
            val selectedAppointment by viewModel.selectedAppointment.collectAsStateWithLifecycle()
            val notificationMsg by viewModel.notificationMessage.collectAsStateWithLifecycle()
            val isPractitionerPresent by viewModel.isPractitionerPresent.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
            val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
            val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
            val refreshingMessage by viewModel.refreshingMessage.collectAsStateWithLifecycle()
            val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()

            val allPractitioners by viewModel.allPractitioners.collectAsStateWithLifecycle()
            val upcomingAppointments by viewModel.upcomingAppointments.collectAsStateWithLifecycle()
            val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
            val patientAppointments by viewModel.patientAppointments.collectAsStateWithLifecycle()
            val medicationReminders by viewModel.medicationReminders.collectAsStateWithLifecycle()
            val prescriptions by viewModel.prescriptions.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            val conversations by viewModel.conversations.collectAsStateWithLifecycle()

            val isSeniorMode = userProfile?.isSeniorMode ?: false
            val userName = userProfile?.firstName ?: ""

            // Demande la permission de notification (Android 13+) pour que la
            // notification de bienvenue puisse s'afficher à la fin de l'inscription.
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* résultat ignoré : la notification reste disponible si refusée */ }
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            DoctaTheme(isSeniorMode = isSeniorMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            AppScreen.SESSION_LOADING -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(SageDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Color.White)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Chargement de votre session…",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            AppScreen.ONBOARDING_LANDING -> {
                                OnboardingLandingScreen(
                                    onSelectRole = { role ->
                                        if (role == com.example.data.models.UserRole.PATIENT) {
                                            viewModel.navigateTo(AppScreen.ONBOARDING_PATIENT)
                                        } else {
                                            viewModel.navigateTo(AppScreen.ONBOARDING_PRO)
                                        }
                                    },
                                    onLoginClick = { viewModel.navigateTo(AppScreen.LOGIN) },
                                    isSeniorMode = isSeniorMode
                                )
                            }

                            AppScreen.ONBOARDING_PATIENT -> {
                                PatientOnboardingWizard(
                                    onComplete = { fName, lName, email, phone, pwd, photoUri ->
                                        viewModel.registerPatient(fName, lName, email, phone, pwd, photoUri)
                                    },
                                    onBackToLanding = { viewModel.navigateTo(AppScreen.ONBOARDING_LANDING) },
                                    isSeniorMode = isSeniorMode,
                                    isLoading = isLoading,
                                    errorMessage = errorMessage
                                )
                            }

                            AppScreen.ONBOARDING_PRO -> {
                                PractitionerOnboardingWizard(
                                    onComplete = { docName, spec, education, rpps, photoUri, docUri, email, pwd ->
                                        viewModel.registerPractitioner(docName, spec, education, rpps, photoUri, docUri, email, pwd)
                                    },
                                    onBackToLanding = { viewModel.navigateTo(AppScreen.ONBOARDING_LANDING) },
                                    isSeniorMode = isSeniorMode,
                                    isLoading = isLoading,
                                    errorMessage = errorMessage
                                )
                            }

                            AppScreen.LOGIN -> {
                                LoginScreen(
                                    onLoginSuccess = { role -> viewModel.selectRoleAndLogin(role) },
                                    onBackToOnboarding = { viewModel.navigateTo(AppScreen.ONBOARDING_LANDING) },
                                    isSeniorMode = isSeniorMode,
                                    onLoginWithEmail = { email, password ->
                                        viewModel.loginWithEmail(email, password)
                                    },
                                    isLoading = isLoading,
                                    errorMessage = errorMessage
                                )
                            }

                            AppScreen.PRESENTATION -> {
                                AppPresentationScreen(
                                    role = viewModel.activeUserRole.value,
                                    userName = userName,
                                    isSeniorMode = isSeniorMode,
                                    onComplete = { viewModel.completePresentation() }
                                )
                            }

                            AppScreen.PATIENT_MAIN -> {
                                PatientHomeScreen(
                                    userName = userName,
                                    profileImageUri = userProfile?.photoUrl,
                                    onUpdatePhoto = { photoUri -> viewModel.updateUserPhoto(photoUri) },
                                    upcomingAppointments = upcomingAppointments,
                                    allPractitioners = allPractitioners,
                                    isSeniorMode = isSeniorMode,
                                    onToggleSeniorMode = { viewModel.toggleSeniorMode() },
                                    onSelectPractitioner = { doc -> viewModel.startBookingFor(doc) },
                                    onMessagePractitioner = { doc -> viewModel.startConversationWithDoctor(doc) },
                                    onStartTeleconsult = { doc -> viewModel.startTeleconsultationFor(doc) },
                                    onCancelAppointment = { id -> viewModel.cancelAppointment(id) },
                                    onNavigateToReminders = { viewModel.navigateTo(AppScreen.REMINDERS_THERAPEUTIC) },
                                    onNavigateToMessaging = { viewModel.openMessaging() },
                                    onNavigateToAppointmentsList = { viewModel.navigateTo(AppScreen.PATIENT_APPOINTMENTS) },
                                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                                    onOpenPresentation = { viewModel.openPresentation() }
                                )
                            }

                            AppScreen.BOOKING_FLOW -> {
                                val doctor = selectedDoctor ?: allPractitioners.firstOrNull()
                                if (doctor != null) {
                                    BookingFlowScreen(
                                        practitioner = doctor,
                                        onConfirmBooking = { doc, date, time, type, motif, note ->
                                            viewModel.confirmAppointment(doc, date, time, type, motif, note)
                                        },
                                        onBack = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) },
                                        isSeniorMode = isSeniorMode
                                    )
                                }
                            }

                            AppScreen.TELECONSULTATION -> {
                                val appointment = selectedAppointment ?: upcomingAppointments.firstOrNull()
                                if (appointment != null) {
                                    TeleconsultationScreen(
                                        appointment = appointment,
                                        displayName = userProfile?.displayName.orEmpty(),
                                        onEndCall = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) },
                                        isSeniorMode = isSeniorMode
                                    )
                                }
                            }

                            AppScreen.PRO_MAIN -> {
                                PractitionerHomeScreen(
                                    doctorName = userName,
                                    profileImageUri = userProfile?.photoUrl,
                                    specialty = userProfile?.specialty.orEmpty(),
                                    rppsNumber = userProfile?.rppsNumber.orEmpty(),
                                    onUpdatePhoto = { photoUri -> viewModel.updateUserPhoto(photoUri) },
                                    isPractitionerPresent = isPractitionerPresent,
                                    onTogglePresence = { viewModel.togglePractitionerPresence() },
                                    appointments = allAppointments,
                                    onStartConsultationForPatient = { appt ->
                                        viewModel.selectAppointment(appt)
                                    },
                                    onMessagePatient = { appt -> viewModel.startConversationForAppointment(appt) },
                                    onNavigateToMessaging = { viewModel.openMessaging() },
                                    onUpdateAppointment = { id, time, motif -> viewModel.updateAppointment(id, time, motif) },
                                    onUpdateAppointmentStatus = { id, status -> viewModel.updateAppointmentStatus(id, status) },
                                    onSetPresence = { present -> viewModel.setPractitionerPresence(present) },
                                    onCreateAppointment = { patientId, patientName, date, time, type, motif ->
                                        viewModel.createDoctorAppointment(patientId, patientName, date, time, type, motif)
                                    },
                                    onIssuePrescription = { patientId, patientName, med, dosage, duration, notes ->
                                        viewModel.issuePrescriptionForPatient(patientId, patientName, med, dosage, duration, notes)
                                    },
                                    isSeniorMode = isSeniorMode,
                                    onOpenPresentation = { viewModel.openPresentation() },
                                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                                    onLogout = { viewModel.logout() }
                                )
                            }

                            AppScreen.PRO_CONSULTATION -> {
                                PractitionerConsultationScreen(
                                    appointment = selectedAppointment,
                                    doctorName = userName,
                                    onIssuePrescription = { text, docName ->
                                        viewModel.issueDigitalPrescription(text, docName)
                                    },
                                    onBack = { viewModel.navigateTo(AppScreen.PRO_MAIN) },
                                    isSeniorMode = isSeniorMode
                                )
                            }

                            AppScreen.MESSAGING -> {
                                MessagingScreen(
                                    messages = chatMessages,
                                    currentUserId = userProfile?.uid.orEmpty(),
                                    conversationTitle = if (viewModel.activeUserRole.value == com.example.data.models.UserRole.PRATICIEN) selectedAppointment?.patientName.orEmpty() else selectedAppointment?.doctorName.orEmpty(),
                                    onSendMessage = { text -> viewModel.sendChatMessage(text) },
                                    onBack = {
                                        viewModel.navigateTo(if (viewModel.activeUserRole.value == com.example.data.models.UserRole.PRATICIEN) AppScreen.PRO_MAIN else AppScreen.PATIENT_MAIN)
                                    },
                                    isSeniorMode = isSeniorMode
                                )
                            }

                            AppScreen.CONVERSATIONS -> {
                                ConversationListScreen(
                                    conversations = conversations,
                                    appointments = if (viewModel.activeUserRole.value == com.example.data.models.UserRole.PRATICIEN) allAppointments else patientAppointments,
                                    currentUserId = userProfile?.uid.orEmpty(),
                                    onSelect = { viewModel.selectConversation(it) },
                                    onStart = { viewModel.startConversationForAppointment(it) },
                                    onBack = { viewModel.navigateTo(if (viewModel.activeUserRole.value == com.example.data.models.UserRole.PRATICIEN) AppScreen.PRO_MAIN else AppScreen.PATIENT_MAIN) }
                                )
                            }

                            AppScreen.REMINDERS_THERAPEUTIC -> {
                                MedicationReminderScreen(
                                    reminders = medicationReminders,
                                    prescriptions = prescriptions,
                                    onDownloadPrescription = { viewModel.downloadPrescriptionPdf(it) },
                                    onToggleTaken = { id, currentTaken -> viewModel.toggleMedicationTaken(id, currentTaken) },
                                    onAddReminder = { name, dos, freq, time ->
                                        viewModel.addMedicationReminder(name, dos, freq, time)
                                    },
                                    onBack = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) },
                                    isSeniorMode = isSeniorMode
                                )
                            }

                            AppScreen.PATIENT_APPOINTMENTS -> {
                                PatientAppointmentsScreen(
                                    appointments = patientAppointments,
                                    onCancel = { viewModel.cancelAppointment(it) },
                                    onBack = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) }
                                )
                            }

                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    userProfile = userProfile,
                                    onUpdatePhoto = { photoUri -> viewModel.updateUserPhoto(photoUri) },
                                    onUpdateInfo = { firstName, lastName, phone ->
                                        viewModel.updateUserInfo(firstName, lastName, phone)
                                    },
                                    onChangePassword = { current, new, done ->
                                        viewModel.changePassword(current, new, done)
                                    },
                                    onLogout = { viewModel.logout() },
                                    onBack = { viewModel.navigateToMainScreen() },
                                    isSeniorMode = isSeniorMode
                                )
                            }

                            AppScreen.ABOUT -> {
                                AboutScreen(
                                    onBack = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) }
                                )
                            }

                            AppScreen.PRESCRIPTION_SHARE -> {
                                PrescriptionShareScreen(
                                    onBack = { viewModel.navigateTo(AppScreen.PRO_MAIN) }
                                )
                            }
                        }
                    }

                    PushNotificationBanner(
                        message = notificationMsg,
                        onDismiss = { viewModel.clearNotification() },
                        modifier = Modifier.padding(top = 28.dp)
                    )

if (isRefreshing) {
                        RefreshingOverlay(message = refreshingMessage)
                    }

                    updateInfo?.let { info ->
                        AlertDialog(
                            onDismissRequest = { viewModel.dismissUpdatePrompt() },
                            title = { Text("Nouvelle version disponible", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Une mise à jour est disponible (version ${info.versionName}).")
                                    if (info.notes.isNotBlank()) {
                                        Text(info.notes, fontSize = 13.sp, color = Color.Gray)
                                    }
                                    Text(
                                        "Vos données et votre session seront conservées après la mise à jour.",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.downloadUpdate() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                                ) {
                                    Text("Mettre à jour")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.dismissUpdatePrompt() }) {
                                    Text("Plus tard")
                                }
                            },
                            containerColor = WarmOffWhite
                        )
                    }
                }
            }
        }
    }
}
