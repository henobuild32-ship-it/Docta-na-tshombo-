package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.firebase.*
import com.example.data.models.UserRole
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    ONBOARDING_LANDING,
    ONBOARDING_PATIENT,
    ONBOARDING_PRO,
    LOGIN,
    PATIENT_MAIN,
    BOOKING_FLOW,
    TELECONSULTATION,
    PRO_MAIN,
    PRO_CONSULTATION,
    MESSAGING,
    REMINDERS_THERAPEUTIC,
    ABOUT,
    PRESCRIPTION_SHARE
}

class DoctaViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository()
    private val firestoreRepo = FirestoreRepository()
    private val storageRepo = StorageRepository(application)

    // Local Room DB for offline cache
    private val db = AppDatabase.getDatabase(application)

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.ONBOARDING_LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeUserRole = MutableStateFlow(UserRole.PATIENT)
    val activeUserRole: StateFlow<UserRole> = _activeUserRole.asStateFlow()

    private val _selectedPractitioner = MutableStateFlow<FirestoreDoctor?>(null)
    val selectedPractitioner: StateFlow<FirestoreDoctor?> = _selectedPractitioner.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    private val _isPractitionerPresent = MutableStateFlow(true)
    val isPractitionerPresent: StateFlow<Boolean> = _isPractitionerPresent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Firebase User Profile
    private val _userProfile = MutableStateFlow<FirestoreUser?>(null)
    val userProfile: StateFlow<FirestoreUser?> = _userProfile.asStateFlow()

    // Firestore Data
    val allPractitioners: StateFlow<List<FirestoreDoctor>> = firestoreRepo.getDoctors()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _upcomingAppointments = MutableStateFlow<List<FirestoreAppointment>>(emptyList())
    val upcomingAppointments: StateFlow<List<FirestoreAppointment>> = _upcomingAppointments.asStateFlow()

    private val _allAppointments = MutableStateFlow<List<FirestoreAppointment>>(emptyList())
    val allAppointments: StateFlow<List<FirestoreAppointment>> = _allAppointments.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<FirestoreMessage>>(emptyList())
    val chatMessages: StateFlow<List<FirestoreMessage>> = _chatMessages.asStateFlow()

    private val _medicationReminders = MutableStateFlow<List<FirestoreMedicationReminder>>(emptyList())
    val medicationReminders: StateFlow<List<FirestoreMedicationReminder>> = _medicationReminders.asStateFlow()

    private val _prescriptions = MutableStateFlow<List<FirestorePrescription>>(emptyList())
    val prescriptions: StateFlow<List<FirestorePrescription>> = _prescriptions.asStateFlow()

    // Auth state listener
    init {
        viewModelScope.launch {
            authRepo.authStateFlow().collect { firebaseUser ->
                if (firebaseUser != null) {
                    loadUserProfile(firebaseUser.uid)
                    observeUserData(firebaseUser.uid)
                } else {
                    _userProfile.value = null
                    _currentScreen.value = AppScreen.ONBOARDING_LANDING
                }
            }
        }
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val profile = authRepo.getUserProfile(uid)
            if (profile != null) {
                _userProfile.value = profile
                _activeUserRole.value = when (profile.role) {
                    FirestoreUser.ROLE_DOCTOR -> UserRole.PRATICIEN
                    else -> UserRole.PATIENT
                }
                // Auto-navigate to main screen if already logged in
                if (_currentScreen.value == AppScreen.ONBOARDING_LANDING ||
                    _currentScreen.value == AppScreen.LOGIN) {
                    navigateToMainScreen()
                }
            }
        }
    }

    private fun observeUserData(uid: String) {
        viewModelScope.launch {
            val profile = _userProfile.value ?: return@launch
            if (profile.role == FirestoreUser.ROLE_PATIENT || profile.role == FirestoreUser.ROLE_ADMIN) {
                firestoreRepo.getUpcomingPatientAppointments(uid).collect { appointments ->
                    _upcomingAppointments.value = appointments
                }
            }
        }
        viewModelScope.launch {
            val profile = _userProfile.value ?: return@launch
            if (profile.role == FirestoreUser.ROLE_DOCTOR) {
                firestoreRepo.getDoctorAppointments(uid).collect { appointments ->
                    _allAppointments.value = appointments
                }
            }
        }
        viewModelScope.launch {
            firestoreRepo.getPatientReminders(uid).collect { reminders ->
                _medicationReminders.value = reminders
            }
        }
        viewModelScope.launch {
            firestoreRepo.getPatientPrescriptions(uid).collect { prescriptions ->
                _prescriptions.value = prescriptions
            }
        }
    }

    // ========== NAVIGATION ==========

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    private fun navigateToMainScreen() {
        _currentScreen.value = if (_activeUserRole.value == UserRole.PRATICIEN) {
            AppScreen.PRO_MAIN
        } else {
            AppScreen.PATIENT_MAIN
        }
    }

    // ========== AUTHENTICATION ==========

    fun selectRoleAndLogin(role: UserRole) {
        _activeUserRole.value = role
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            authRepo.updateUserProfile(uid, mapOf("role" to role.name.lowercase()))
            _userProfile.value = _userProfile.value?.copy(role = role.name.lowercase())
        }
        navigateToMainScreen()
    }

    fun registerPatient(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        photoUri: String?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepo.signUpWithEmail(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                role = FirestoreUser.ROLE_PATIENT
            )
            result.fold(
                onSuccess = { user ->
                    // Upload photo if provided
                    if (photoUri != null) {
                        storageRepo.uploadProfilePhoto(user.uid, Uri.parse(photoUri)).fold(
                            onSuccess = { url ->
                                authRepo.updateUserProfile(user.uid, mapOf("photoUrl" to url))
                            },
                            onFailure = {}
                        )
                    }
                    // Update FCM token
                    FcmHelper.getToken()?.let { token ->
                        authRepo.updateFcmToken(user.uid, token)
                    }
                    showPushNotification("Bienvenue $firstName ! Votre compte patient est créé.")
                    _activeUserRole.value = UserRole.PATIENT
                    _currentScreen.value = AppScreen.PATIENT_MAIN
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Erreur lors de l'inscription"
                    showPushNotification("Erreur: ${error.message}")
                }
            )
            _isLoading.value = false
        }
    }

    fun registerPractitioner(
        doctorName: String,
        specialty: String,
        education: DoctorEducation,
        rppsNumber: String,
        photoUri: String?,
        documentUri: String?,
        email: String,
        password: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepo.signUpWithEmail(
                email = email,
                password = password,
                firstName = doctorName,
                lastName = "",
                phone = "",
                role = FirestoreUser.ROLE_DOCTOR
            )
            result.fold(
                onSuccess = { user ->
                    // Create doctor profile in Firestore
                    val doctor = FirestoreDoctor(
                        userId = user.uid,
                        name = doctorName,
                        specialty = specialty,
                        rpps = rppsNumber,
                        degree = education.degree,
                        educationLevel = education.educationLevel,
                        studyDuration = education.studyDuration,
                        universities = education.universities,
                        trainings = education.trainings,
                        graduationYear = education.graduationYear,
                        hasDocuments = documentUri != null,
                        documentsVerified = false
                    )
                    firestoreRepo.createDoctor(doctor)

                    if (photoUri != null) {
                        storageRepo.uploadProfilePhoto(user.uid, Uri.parse(photoUri)).fold(
                            onSuccess = { url ->
                                authRepo.updateUserProfile(user.uid, mapOf("photoUrl" to url))
                            },
                            onFailure = {}
                        )
                    }

                    if (documentUri != null) {
                        val fileName = "cps_diplome_${System.currentTimeMillis()}"
                        storageRepo.uploadDoctorDocument(user.uid, Uri.parse(documentUri), fileName).fold(
                            onSuccess = { url ->
                                firestoreRepo.updateDoctor(doctor.id, mapOf("hasDocuments" to true))
                            },
                            onFailure = {}
                        )
                    }

                    FcmHelper.getToken()?.let { token ->
                        authRepo.updateFcmToken(user.uid, token)
                    }

                    showPushNotification("Bienvenue $doctorName ! Votre profil praticien est enregistré.")
                    _activeUserRole.value = UserRole.PRATICIEN
                    _currentScreen.value = AppScreen.PRO_MAIN
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Erreur lors de l'inscription"
                    showPushNotification("Erreur: ${error.message}")
                }
            )
            _isLoading.value = false
        }
    }

    fun loginWithEmail(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepo.signInWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    showPushNotification("Connexion réussie !")
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Erreur de connexion"
                    showPushNotification("Erreur: ${error.message}")
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.signOut()
            _userProfile.value = null
            _currentScreen.value = AppScreen.ONBOARDING_LANDING
            showPushNotification("Déconnexion réussie")
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            authRepo.resetPassword(email).fold(
                onSuccess = { showPushNotification("Email de réinitialisation envoyé") },
                onFailure = { showPushNotification("Erreur: ${it.message}") }
            )
        }
    }

    // ========== USER ACTIONS ==========

    fun updateUserPhoto(photoUri: String) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            storageRepo.uploadProfilePhoto(uid, Uri.parse(photoUri)).fold(
                onSuccess = { url ->
                    authRepo.updateUserProfile(uid, mapOf("photoUrl" to url))
                    _userProfile.value = _userProfile.value?.copy(photoUrl = url)
                    showPushNotification("Photo de profil mise à jour avec succès.")
                },
                onFailure = { error ->
                    showPushNotification("Erreur: ${error.message}")
                }
            )
        }
    }

    fun startBookingFor(doctor: FirestoreDoctor) {
        _selectedPractitioner.value = doctor
        _currentScreen.value = AppScreen.BOOKING_FLOW
    }

    fun startTeleconsultationFor(doctor: FirestoreDoctor?) {
        if (doctor != null) {
            _selectedPractitioner.value = doctor
        }
        _currentScreen.value = AppScreen.TELECONSULTATION
    }

    fun toggleSeniorMode() {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            val current = _userProfile.value?.isSeniorMode ?: false
            authRepo.updateUserProfile(uid, mapOf("isSeniorMode" to !current))
            _userProfile.value = _userProfile.value?.copy(isSeniorMode = !current)
            showPushNotification(
                if (!current) "Mode Ergonomique / Senior activé"
                else "Mode Standard réactivé"
            )
        }
    }

    // ========== APPOINTMENTS ==========

    fun confirmAppointment(
        doctor: FirestoreDoctor,
        date: String,
        time: String,
        type: String,
        motif: String,
        note: String
    ) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            val profile = _userProfile.value

            val appointment = FirestoreAppointment(
                patientId = uid,
                patientName = profile?.displayName ?: "",
                doctorId = doctor.id,
                doctorName = doctor.name,
                doctorSpecialty = doctor.specialty,
                doctorAvatar = doctor.photoUrl,
                date = "$date, $time",
                time = time,
                type = type,
                motif = motif,
                status = FirestoreAppointment.STATUS_PENDING,
                userNote = note,
                address = if (type == "TELECONSULTATION") "Téléconsultation en ligne sécurisée" else doctor.address
            )

            firestoreRepo.createAppointment(appointment).fold(
                onSuccess = {
                    showPushNotification("Rendez-vous confirmé avec ${doctor.name} le $date à $time")
                },
                onFailure = { error ->
                    showPushNotification("Erreur: ${error.message}")
                }
            )
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            firestoreRepo.updateAppointmentStatus(appointmentId, FirestoreAppointment.STATUS_CANCELLED).fold(
                onSuccess = { showPushNotification("Rendez-vous annulé avec succès") },
                onFailure = { showPushNotification("Erreur: ${it.message}") }
            )
        }
    }

    // ========== MESSAGING ==========

    fun sendChatMessage(text: String) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            val profile = _userProfile.value ?: return@launch

            val message = FirestoreMessage(
                conversationId = "main_chat_$uid",
                senderId = uid,
                senderName = profile.displayName,
                text = text
            )

            firestoreRepo.sendMessage(message).fold(
                onSuccess = {},
                onFailure = { error -> showPushNotification("Erreur envoi: ${error.message}") }
            )
        }
    }

    // ========== MEDICATION REMINDERS ==========

    fun addMedicationReminder(name: String, dosage: String, frequency: String, time: String) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch

            val reminder = FirestoreMedicationReminder(
                patientId = uid,
                medicineName = name,
                dosage = dosage,
                frequency = frequency,
                timeOfDay = time,
                isTakenToday = false,
                startDate = "Aujourd'hui"
            )

            firestoreRepo.createReminder(reminder).fold(
                onSuccess = { showPushNotification("Rappel de traitement programmé pour $name ($time)") },
                onFailure = { showPushNotification("Erreur: ${it.message}") }
            )
        }
    }

    fun toggleMedicationTaken(reminderId: String, currentTakenState: Boolean) {
        viewModelScope.launch {
            firestoreRepo.updateReminderTaken(reminderId, !currentTakenState).fold(
                onSuccess = {
                    if (!currentTakenState) {
                        showPushNotification("Prise de médicament enregistrée !")
                    }
                },
                onFailure = {}
            )
        }
    }

    // ========== PRESCRIPTIONS ==========

    fun issueDigitalPrescription(medicinesText: String, doctorName: String) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch

            val prescription = FirestorePrescription(
                doctorId = uid,
                doctorName = doctorName,
                patientId = _selectedPractitioner.value?.userId ?: "",
                patientName = _userProfile.value?.displayName ?: "",
                medicinesSummary = medicinesText,
                isSigned = true
            )

            firestoreRepo.createPrescription(prescription).fold(
                onSuccess = { showPushNotification("Ordonnance numérique délivrée et signée !") },
                onFailure = { showPushNotification("Erreur: ${it.message}") }
            )
        }
    }

    // ========== PRACTITIONER ==========

    fun togglePractitionerPresence() {
        _isPractitionerPresent.value = !_isPractitionerPresent.value
        val statusText = if (_isPractitionerPresent.value) "En consultation" else "Absent"
        showPushNotification("Cabinet statut : $statusText")
    }

    // ========== NOTIFICATIONS ==========

    fun showPushNotification(msg: String) {
        _notificationMessage.value = msg
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
