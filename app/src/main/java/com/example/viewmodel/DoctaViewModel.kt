package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.firebase.*
import com.example.data.models.UserRole
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.reminders.AppNotificationHelper
import com.example.reminders.MedicationReminderScheduler

enum class AppScreen {
    SESSION_LOADING,
    ONBOARDING_LANDING,
    ONBOARDING_PATIENT,
    ONBOARDING_PRO,
    LOGIN,
    PRESENTATION,
    PATIENT_MAIN,
    BOOKING_FLOW,
    TELECONSULTATION,
    PRO_MAIN,
    PRO_CONSULTATION,
    MESSAGING,
    CONVERSATIONS,
    REMINDERS_THERAPEUTIC,
    PATIENT_APPOINTMENTS,
    SETTINGS,
    ABOUT,
    PRESCRIPTION_SHARE
}

class DoctaViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository()
    private val firestoreRepo = FirestoreRepository()
    private val storageRepo = StorageRepository(application)
    private val prescriptionPdfRepo = PrescriptionPdfRepository(application)

    // Local Room DB for offline cache
    private val db = AppDatabase.getDatabase(application)

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.ONBOARDING_LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeUserRole = MutableStateFlow(UserRole.PATIENT)
    val activeUserRole: StateFlow<UserRole> = _activeUserRole.asStateFlow()

    private val _selectedPractitioner = MutableStateFlow<FirestoreDoctor?>(null)
    val selectedPractitioner: StateFlow<FirestoreDoctor?> = _selectedPractitioner.asStateFlow()
    private val _selectedAppointment = MutableStateFlow<FirestoreAppointment?>(null)
    val selectedAppointment: StateFlow<FirestoreAppointment?> = _selectedAppointment.asStateFlow()
    private var sessionJobs = emptyList<kotlinx.coroutines.Job>()
    private var messagingJob: kotlinx.coroutines.Job? = null

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    private val _isPractitionerPresent = MutableStateFlow(true)
    val isPractitionerPresent: StateFlow<Boolean> = _isPractitionerPresent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshingMessage = MutableStateFlow("Actualisation en cours…")
    val refreshingMessage: StateFlow<String> = _refreshingMessage.asStateFlow()

    private val _updateInfo = MutableStateFlow<com.example.data.AppUpdateManager.UpdateInfo?>(null)
    val updateInfo: StateFlow<com.example.data.AppUpdateManager.UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isRestoringSession = MutableStateFlow(true)
    val isRestoringSession: StateFlow<Boolean> = _isRestoringSession.asStateFlow()

    private val prefs: android.content.SharedPreferences by lazy {
        getApplication<Application>().getSharedPreferences("docta_prefs", Context.MODE_PRIVATE)
    }

    private fun isOnboardingCompleted(): Boolean =
        prefs.getBoolean("onboarding_completed", false)

    private fun markOnboardingCompleted() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }

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

    private val _patientAppointments = MutableStateFlow<List<FirestoreAppointment>>(emptyList())
    val patientAppointments: StateFlow<List<FirestoreAppointment>> = _patientAppointments.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<FirestoreMessage>>(emptyList())
    val chatMessages: StateFlow<List<FirestoreMessage>> = _chatMessages.asStateFlow()
    private val _conversations = MutableStateFlow<List<FirestoreConversation>>(emptyList())
    val conversations: StateFlow<List<FirestoreConversation>> = _conversations.asStateFlow()

    private val _medicationReminders = MutableStateFlow<List<FirestoreMedicationReminder>>(emptyList())
    val medicationReminders: StateFlow<List<FirestoreMedicationReminder>> = _medicationReminders.asStateFlow()

    private val _prescriptions = MutableStateFlow<List<FirestorePrescription>>(emptyList())
    val prescriptions: StateFlow<List<FirestorePrescription>> = _prescriptions.asStateFlow()

    // Auth state listener
    init {
        _currentScreen.value = AppScreen.SESSION_LOADING
        viewModelScope.launch {
            authRepo.sessionStatusFlow().collect { status ->
                when (status) {
                    SessionStatus.Initializing -> {
                        _isRestoringSession.value = true
                        _currentScreen.value = AppScreen.SESSION_LOADING
                    }
                    is SessionStatus.Authenticated -> {
                        val uid = status.session.user?.id
                        _isRestoringSession.value = false
                        if (uid != null) {
                            loadUserProfile(uid)
                        } else {
                            finishLogout()
                        }
                    }
                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure -> {
                        _isRestoringSession.value = false
                        finishLogout()
                    }
                }
            }
        }
        checkForAppUpdate()
    }

    /**
     * Vérifie (au démarrage et régulièrement) si une nouvelle version de
     * l'application est disponible. Si oui, expose l'info pour que l'UI
     * propose le téléchargement/installation de la mise à jour.
     */
    fun checkForAppUpdate() {
        viewModelScope.launch {
            val info = com.example.data.AppUpdateManager.checkForUpdate(getApplication())
            if (info != null) {
                _updateInfo.value = info
            }
        }
    }

    fun dismissUpdatePrompt() {
        _updateInfo.value = null
    }

    fun downloadUpdate() {
        val info = _updateInfo.value ?: return
        com.example.data.AppUpdateManager.downloadAndInstall(getApplication(), info)
        _updateInfo.value = null
    }

    /** Rétablit l'écran de destination quand on n'est pas connecté (onboarding vs login). */
    private fun finishLogout() {
        sessionJobs.forEach { it.cancel() }
        sessionJobs = emptyList()
        messagingJob?.cancel()
        messagingJob = null
        _userProfile.value = null
        _selectedAppointment.value = null
        _chatMessages.value = emptyList()
        _currentScreen.value =
            if (isOnboardingCompleted()) AppScreen.LOGIN else AppScreen.ONBOARDING_LANDING
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
                if (_currentScreen.value == AppScreen.SESSION_LOADING ||
                    _currentScreen.value == AppScreen.ONBOARDING_LANDING ||
                    _currentScreen.value == AppScreen.LOGIN) {
                    if (profile.presentationSeen) {
                        navigateToMainScreen()
                    } else {
                        _currentScreen.value = AppScreen.PRESENTATION
                    }
                }
                sessionJobs.forEach { it.cancel() }
                observeUserData(uid)
            }
        }
    }

    private fun observeUserData(uid: String) {
        val jobs = mutableListOf<kotlinx.coroutines.Job>()
        val profile = _userProfile.value ?: return
        if (profile.role == FirestoreUser.ROLE_PATIENT || profile.role == FirestoreUser.ROLE_ADMIN) {
            jobs += viewModelScope.launch {
                firestoreRepo.getPatientAppointments(uid).collect { appointments ->
                    _patientAppointments.value = appointments
                }
            }
            jobs += viewModelScope.launch {
                firestoreRepo.getUpcomingPatientAppointments(uid).collect { appointments ->
                    _upcomingAppointments.value = appointments
                }
            }
        }
        jobs += viewModelScope.launch {
            val profile = _userProfile.value ?: return@launch
            if (profile.role == FirestoreUser.ROLE_DOCTOR) {
                firestoreRepo.getDoctorAppointments(uid).collect { appointments ->
                    _allAppointments.value = appointments
                }
            }
        }
        if (_userProfile.value?.role == FirestoreUser.ROLE_PATIENT || _userProfile.value?.role == FirestoreUser.ROLE_ADMIN) jobs += viewModelScope.launch {
            firestoreRepo.getPatientReminders(uid).collect { reminders ->
                _medicationReminders.value = reminders
            }
        }
        if (_userProfile.value?.role == FirestoreUser.ROLE_PATIENT || _userProfile.value?.role == FirestoreUser.ROLE_ADMIN) jobs += viewModelScope.launch {
            firestoreRepo.getPatientPrescriptions(uid).collect { prescriptions ->
                _prescriptions.value = prescriptions
            }
        }
        jobs += viewModelScope.launch {
            firestoreRepo.getConversations(uid).collect { conversations ->
                _conversations.value = conversations
            }
        }
        sessionJobs = jobs
    }

    // ========== NAVIGATION ==========

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectAppointment(appointment: FirestoreAppointment) {
        _selectedAppointment.value = appointment
        _currentScreen.value = AppScreen.PRO_CONSULTATION
    }

    fun openMessaging() {
        _currentScreen.value = AppScreen.CONVERSATIONS
    }

    fun startConversationWithDoctor(doctor: FirestoreDoctor) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            val profile = _userProfile.value ?: return@launch
            _selectedPractitioner.value = doctor
            val conversationId = firestoreRepo.ensureDirectConversation(
                uid, profile.displayName, doctor.id, doctor.name
            ).getOrElse { error ->
                showPushNotification("Erreur de conversation : ${error.message}")
                return@launch
            }
            observeMessages(conversationId)
            _currentScreen.value = AppScreen.MESSAGING
        }
    }

    fun startConversationForAppointment(appointment: FirestoreAppointment) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            val profile = _userProfile.value ?: return@launch
            _selectedAppointment.value = appointment
            val otherUserId = if (profile.role == FirestoreUser.ROLE_DOCTOR) appointment.patientId else appointment.doctorId
            val otherUserName = if (profile.role == FirestoreUser.ROLE_DOCTOR) appointment.patientName else appointment.doctorName
            val conversationId = firestoreRepo.ensureDirectConversation(
                uid, profile.displayName, otherUserId, otherUserName
            ).getOrElse { error ->
                showPushNotification("Erreur de conversation : ${error.message}")
                return@launch
            }

            observeMessages(conversationId)
            _currentScreen.value = AppScreen.MESSAGING
        }
    }

    fun selectConversation(conversation: FirestoreConversation) {
        val profile = _userProfile.value ?: return
        val otherId = conversation.participantIds.firstOrNull { it != profile.uid }.orEmpty()
        _selectedAppointment.value = (_patientAppointments.value + _allAppointments.value).firstOrNull {
            it.patientId == otherId || it.doctorId == otherId
        }
        observeMessages(conversation.id)
        _currentScreen.value = AppScreen.MESSAGING
    }

    private fun observeMessages(conversationId: String) {
        messagingJob?.cancel()
        messagingJob = viewModelScope.launch {
            firestoreRepo.getMessages(conversationId).collect { messages ->
                _chatMessages.value = messages
            }
        }
    }

    fun navigateToMainScreen() {
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
            if (_userProfile.value?.presentationSeen != true) {
                _currentScreen.value = AppScreen.PRESENTATION
            } else {
                navigateToMainScreen()
            }
        }
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
                    if (authRepo.currentUserId == null) {
                        // Compte créé mais session non établie (confirmation email requise)
                        // => pas de blocage : on reste sur l'écran de connexion avec un message clair.
                        _errorMessage.value = "Compte créé. Vérifiez votre email pour activer votre compte, puis connectez-vous."
                        _isLoading.value = false
                        return@fold
                    }
                    // Upload photo if provided
                    if (photoUri != null) {
                        storageRepo.uploadProfilePhoto(user.id, Uri.parse(photoUri)).fold(
                            onSuccess = { url ->
                                authRepo.updateUserProfile(user.id, mapOf("photoUrl" to url))
                            },
                            onFailure = {}
                        )
                    }
                    // Update OneSignal subscription id
                    FcmHelper.getToken()?.let { token ->
                        authRepo.updateFcmToken(user.id, token)
                    }
                    showPushNotification("Bienvenue $firstName ! Votre compte patient est créé.")
                    AppNotificationHelper.show(
                        getApplication(),
                        "Bienvenue sur Docta na Tshombo",
                        "Bienvenue $firstName ! Votre compte patient est créé. Commencez à explorer l'application."
                    )
                    _activeUserRole.value = UserRole.PATIENT
                    markOnboardingCompleted()
                    _currentScreen.value = AppScreen.PRESENTATION
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Erreur lors de l'inscription"
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
                    if (authRepo.currentUserId == null) {
                        _errorMessage.value = "Compte créé. Vérifiez votre email pour activer votre compte, puis connectez-vous."
                        _isLoading.value = false
                        return@fold
                    }
                    // Create doctor profile
                    val doctor = FirestoreDoctor(
                        userId = user.id,
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
                        storageRepo.uploadProfilePhoto(user.id, Uri.parse(photoUri)).fold(
                            onSuccess = { url ->
                                authRepo.updateUserProfile(user.id, mapOf("photoUrl" to url))
                            },
                            onFailure = {}
                        )
                    }

                    if (documentUri != null) {
                        val fileName = "cps_diplome_${System.currentTimeMillis()}"
                        storageRepo.uploadDoctorDocument(user.id, Uri.parse(documentUri), fileName).fold(
                            onSuccess = { url ->
                                firestoreRepo.updateDoctor(user.id, mapOf("hasDocuments" to true))
                            },
                            onFailure = {}
                        )
                    }

                    FcmHelper.getToken()?.let { token ->
                        authRepo.updateFcmToken(user.id, token)
                    }

                    showPushNotification("Bienvenue $doctorName ! Votre profil praticien est enregistré.")
                    AppNotificationHelper.show(
                        getApplication(),
                        "Bienvenue sur Docta na Tshombo",
                        "Bienvenue $doctorName ! Votre profil praticien est enregistré. Commencez à gérer votre cabinet."
                    )
                    _activeUserRole.value = UserRole.PRATICIEN
                    markOnboardingCompleted()
                    _currentScreen.value = AppScreen.PRESENTATION
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Erreur lors de l'inscription"
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
                    markOnboardingCompleted()
                    showPushNotification("Connexion réussie !")
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Identifiants incorrects"
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.signOut()
            _userProfile.value = null
            // La navigation est gérée par sessionStatusFlow (NotAuthenticated -> finishLogout).
            showPushNotification("Déconnexion réussie")
        }
    }

    // ========== USER ACTIONS ==========

    fun updateUserPhoto(photoUri: String) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            storageRepo.uploadProfilePhoto(uid, Uri.parse(photoUri)).fold(
                onSuccess = { url ->
                    val displayUrl = storageRepo.getSignedUrl(url).getOrElse {
                        showPushNotification("Photo envoyée, mais son aperçu est indisponible")
                        return@fold
                    }
                    authRepo.updateUserProfile(uid, mapOf("photoPath" to url, "photoUrl" to displayUrl))
                    _userProfile.value = _userProfile.value?.copy(photoUrl = displayUrl)
                    showPushNotification("Photo de profil mise à jour avec succès.")
                },
                onFailure = { error ->
                    showPushNotification("Erreur: ${error.message}")
                }
            )
        }
    }

    /** Met à jour les informations du profil (prénom, nom, téléphone). */
    fun updateUserInfo(firstName: String, lastName: String, phone: String) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            _isLoading.value = true
            authRepo.updateUserProfile(
                uid,
                mapOf("firstName" to firstName.trim(), "lastName" to lastName.trim(), "phone" to phone.trim())
            ).fold(
                onSuccess = {
                    _userProfile.value = _userProfile.value?.copy(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        phone = phone.trim()
                    )
                    triggerRefreshFeedback("Actualisation en cours… Mise à jour de votre profil.")
                    showPushNotification("Profil mis à jour avec succès.")
                },
                onFailure = { error ->
                    showPushNotification("Erreur: ${error.message}")
                }
            )
            _isLoading.value = false
        }
    }

    /** Change le mot de passe du compte. */
    fun changePassword(currentPassword: String, newPassword: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (newPassword.length < 6) {
                showPushNotification("Le nouveau mot de passe doit contenir au moins 6 caractères.")
                onDone(false)
                return@launch
            }
            _isLoading.value = true
            authRepo.updatePassword(newPassword).fold(
                onSuccess = {
                    showPushNotification("Mot de passe modifié avec succès.")
                    onDone(true)
                },
                onFailure = { error ->
                    showPushNotification("Erreur: ${error.message}")
                    onDone(false)
                }
            )
            _isLoading.value = false
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
        val appointment = _upcomingAppointments.value.firstOrNull { appt ->
            doctor == null || appt.doctorId == doctor.id
        }
        if (appointment == null) {
            showPushNotification("Aucun rendez-vous actif pour cette téléconsultation")
            return
        }
        _selectedAppointment.value = appointment
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

    fun completePresentation() {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            if (_userProfile.value?.presentationSeen != true) {
                authRepo.updateUserProfile(uid, mapOf("presentationSeen" to true))
                _userProfile.value = _userProfile.value?.copy(presentationSeen = true)
            }
            navigateToMainScreen()
        }
    }

    fun openPresentation() {
        _currentScreen.value = AppScreen.PRESENTATION
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
                    triggerRefreshFeedback("Actualisation en cours… Enregistrement de votre rendez-vous.")
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

    fun updateAppointment(appointmentId: String, time: String, motif: String) {
        viewModelScope.launch {
            firestoreRepo.updateAppointment(
                appointmentId,
                mapOf("time" to time.trim(), "motif" to motif.trim())
            ).fold(
                onSuccess = { showPushNotification("Rendez-vous modifié") },
                onFailure = { showPushNotification("Erreur : ${it.message}") }
            )
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String) {
        viewModelScope.launch {
            firestoreRepo.updateAppointmentStatus(appointmentId, status).fold(
                onSuccess = { showPushNotification("Statut du rendez-vous mis à jour") },
                onFailure = { showPushNotification("Erreur : ${it.message}") }
            )
        }
    }

    fun createDoctorAppointment(patientId: String, patientName: String, date: String, time: String, type: String, motif: String) {
        viewModelScope.launch {
            val doctorId = authRepo.currentUserId ?: return@launch
            val doctorName = _userProfile.value?.displayName.orEmpty()
            val appointment = FirestoreAppointment(
                patientId = patientId,
                patientName = patientName,
                doctorId = doctorId,
                doctorName = doctorName,
                date = date,
                time = time,
                type = type,
                motif = motif,
                status = FirestoreAppointment.STATUS_CONFIRMED
            )
            firestoreRepo.createAppointment(appointment).fold(
                onSuccess = {
                    triggerRefreshFeedback("Actualisation en cours… Création du rendez-vous.")
                    showPushNotification("Rendez-vous créé et transmis au patient")
                },
                onFailure = { showPushNotification("Erreur : ${it.message}") }
            )
        }
    }

    fun issuePrescriptionForPatient(patientId: String, patientName: String, medicine: String, dosage: String, duration: String, notes: String) {
        viewModelScope.launch {
            val doctorId = authRepo.currentUserId ?: return@launch
            if (medicine.isBlank() || dosage.isBlank()) {
                showPushNotification("Médicament et posologie requis")
                return@launch
            }
            val prescription = FirestorePrescription(
                doctorId = doctorId,
                doctorName = _userProfile.value?.displayName.orEmpty(),
                patientId = patientId,
                patientName = patientName,
                medicinesSummary = medicine.trim(),
                dosage = dosage.trim(),
                duration = duration.trim(),
                notes = notes.trim(),
                reference = "ORD-${System.currentTimeMillis()}",
                isSigned = true
            )
            firestoreRepo.createPrescription(prescription).fold(
                onSuccess = { showPushNotification("Ordonnance enregistrée; le PDF sera généré par le serveur") },
                onFailure = { showPushNotification("Erreur : ${it.message}") }
            )
        }
    }

    // ========== MESSAGING ==========

    fun sendChatMessage(text: String) {
        viewModelScope.launch {
            if (text.isBlank()) return@launch
            val uid = authRepo.currentUserId ?: return@launch
            val profile = _userProfile.value ?: return@launch

            val appointment = _selectedAppointment.value ?: run {
                showPushNotification("Sélectionnez d'abord un patient")
                return@launch
            }
            val otherUserId = if (profile.role == FirestoreUser.ROLE_DOCTOR) appointment.patientId else appointment.doctorId
            val otherUserName = if (profile.role == FirestoreUser.ROLE_DOCTOR) appointment.patientName else appointment.doctorName
            val conversationId = firestoreRepo.ensureDirectConversation(
                uid, profile.displayName, otherUserId, otherUserName
            ).getOrElse { error ->
                showPushNotification("Erreur de conversation : ${error.message}")
                return@launch
            }
            observeMessages(conversationId)

            val message = FirestoreMessage(
                conversationId = conversationId,
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
                onSuccess = {
                    MedicationReminderScheduler.schedule(getApplication(), reminder)
                    triggerRefreshFeedback("Actualisation en cours… Programmation de votre rappel.")
                    showPushNotification("Rappel de traitement programmé pour $name ($time)")
                },
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
            val appointment = _selectedAppointment.value ?: run {
                showPushNotification("Aucun patient sélectionné")
                return@launch
            }

            val prescription = FirestorePrescription(
                doctorId = uid,
                doctorName = doctorName,
                patientId = appointment.patientId,
                patientName = appointment.patientName,
                medicinesSummary = medicinesText,
                isSigned = true
            )

            firestoreRepo.createPrescription(prescription).fold(
                onSuccess = { showPushNotification("Ordonnance numérique délivrée et signée !") },
                onFailure = { showPushNotification("Erreur: ${it.message}") }
            )
        }
    }

    fun downloadPrescriptionPdf(prescription: FirestorePrescription) {
        viewModelScope.launch {
            prescriptionPdfRepo.download(prescription).fold(
                onSuccess = { showPushNotification("PDF enregistré dans Téléchargements/Docta : $it") },
                onFailure = { showPushNotification("Téléchargement impossible : ${it.message}") }
            )
        }
    }

    // ========== PRACTITIONER ==========

    fun togglePractitionerPresence() {
        setPractitionerPresence(!_isPractitionerPresent.value)
    }

    fun setPractitionerPresence(isPresent: Boolean) {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            firestoreRepo.updateDoctor(
                uid,
                mapOf("isAvailable" to isPresent, "isOnlineForTeleconsult" to isPresent)
            ).fold(
                onSuccess = {
                    _isPractitionerPresent.value = isPresent
                    showPushNotification(if (isPresent) "Cabinet disponible" else "Cabinet indisponible")
                },
                onFailure = { showPushNotification("Erreur : ${it.message}") }
            )
        }
    }

    // ========== NOTIFICATIONS ==========

    fun showPushNotification(msg: String) {
        _notificationMessage.value = msg
    }

    /**
     * Affiche le bandeau « Actualisation en cours… » pendant 2 à 3 secondes
     * après la création d'une donnée (RDV, rappel…) afin que l'utilisateur
     * voie clairement que les données réelles sont en cours de rafraîchissement.
     */
    fun triggerRefreshFeedback(message: String) {
        _refreshingMessage.value = message
        _isRefreshing.value = true
        viewModelScope.launch {
            delay(2800)
            _isRefreshing.value = false
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
