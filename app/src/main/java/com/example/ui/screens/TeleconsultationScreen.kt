package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.firebase.FirestoreAppointment
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TeleconsultationScreen(
    appointment: FirestoreAppointment,
    displayName: String,
    onEndCall: () -> Unit,
    isSeniorMode: Boolean
) {
    var permissionsGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result[Manifest.permission.CAMERA] == true &&
            result[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    if (!permissionsGranted) {
        AlertDialog(
            onDismissRequest = onEndCall,
            title = { Text("Caméra et microphone requis") },
            text = { Text("La téléconsultation WebRTC nécessite la caméra et le microphone.") },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                }) { Text("Autoriser") }
            },
            dismissButton = { TextButton(onClick = onEndCall) { Text("Retour") } }
        )
        return
    }

    val roomId = "docta-${appointment.id.ifBlank { appointment.patientId + appointment.doctorId }}"
        .replace(Regex("[^A-Za-z0-9-]"), "")
    val encodedName = URLEncoder.encode(displayName, StandardCharsets.UTF_8.toString())
    val url = "https://meet.jit.si/$roomId#config.prejoinPageEnabled=false&userInfo.displayName=$encodedName"

    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = onEndCall) { Text("Quitter") }
                Text("Téléconsultation sécurisée", modifier = Modifier.weight(1f))
            }
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest) {
                            request.grant(request.resources)
                        }
                    }
                    loadUrl(url)
                }
            },
            update = { webView -> if (webView.url != url) webView.loadUrl(url) }
        )
    }
}
