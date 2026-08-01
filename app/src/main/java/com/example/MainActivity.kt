package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.FirestoreUser
import com.example.ui.components.PushNotificationBanner
import com.example.ui.screens.*
import com.example.ui.theme.DoctaTheme
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
            val notificationMsg by viewModel.notificationMessage.collectAsStateWithLifecycle()
            val isPractitionerPresent by viewModel.isPractitionerPresent.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
            val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

            val allPractitioners by viewModel.allPractitioners.collectAsStateWithLifecycle()
            val upcomingAppointments by viewModel.upcomingAppointments.collectAsStateWithLifecycle()
            val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
            val medicationReminders by viewModel.medicationReminders.collectAsStateWithLifecycle()
            val prescriptions by viewModel.prescriptions.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

            val isSeniorMode = userProfile?.isSeniorMode ?: false
            val userName = userProfile?.firstName ?: ""

            DoctaTheme(isSeniorMode = isSeniorMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
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
                                    isSeniorMode = isSeniorMode
                                )
                            }

                            AppScreen.ONBOARDING_PRO -> {
                                PractitionerOnboardingWizard(
                                    onComplete = { docName, spec, education, rpps, photoUri, docUri, email, pwd ->
                                        viewModel.registerPractitioner(docName, spec, education, rpps, photoUri, docUri, email, pwd)
                                    },
                                    onBackToLanding = { viewModel.navigateTo(AppScreen.ONBOARDING_LANDING) },
                                    isSeniorMode = isSeniorMode
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
                                    onStartTeleconsult = { doc -> viewModel.startTeleconsultationFor(doc) },
                                    onCancelAppointment = { id -> viewModel.cancelAppointment(id) },
                                    onNavigateToReminders = { viewModel.navigateTo(AppScreen.REMINDERS_THERAPEUTIC) },
                                    onNavigateToMessaging = { viewModel.navigateTo(AppScreen.MESSAGING) },
                                    onNavigateToAppointmentsList = { viewModel.navigateTo(AppScreen.REMINDERS_THERAPEUTIC) }
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
                                val doctor = selectedDoctor ?: allPractitioners.firstOrNull()
                                if (doctor != null) {
                                    TeleconsultationScreen(
                                        practitioner = doctor,
                                        onEndCall = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) },
                                        isSeniorMode = isSeniorMode
                                    )
                                }
                            }

                            AppScreen.PRO_MAIN -> {
                                PractitionerHomeScreen(
                                    doctorName = userName,
                                    profileImageUri = userProfile?.photoUrl,
                                    onUpdatePhoto = { photoUri -> viewModel.updateUserPhoto(photoUri) },
                                    isPractitionerPresent = isPractitionerPresent,
                                    onTogglePresence = { viewModel.togglePractitionerPresence() },
                                    appointments = allAppointments,
                                    onStartConsultationForPatient = { appt ->
                                        viewModel.navigateTo(AppScreen.PRO_CONSULTATION)
                                    },
                                    onNavigateToMessaging = { viewModel.navigateTo(AppScreen.MESSAGING) },
                                    isSeniorMode = isSeniorMode,
                                    onLogout = { viewModel.logout() }
                                )
                            }

                            AppScreen.PRO_CONSULTATION -> {
                                val currentAppt = upcomingAppointments.firstOrNull()
                                PractitionerConsultationScreen(
                                    appointment = currentAppt,
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
                                    onSendMessage = { text -> viewModel.sendChatMessage(text) },
                                    onBack = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) },
                                    isSeniorMode = isSeniorMode
                                )
                            }

                            AppScreen.REMINDERS_THERAPEUTIC -> {
                                MedicationReminderScreen(
                                    reminders = medicationReminders,
                                    prescriptions = prescriptions,
                                    onToggleTaken = { id, currentTaken -> viewModel.toggleMedicationTaken(id, currentTaken) },
                                    onAddReminder = { name, dos, freq, time ->
                                        viewModel.addMedicationReminder(name, dos, freq, time)
                                    },
                                    onBack = { viewModel.navigateTo(AppScreen.PATIENT_MAIN) },
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
                }
            }
        }
    }
}
