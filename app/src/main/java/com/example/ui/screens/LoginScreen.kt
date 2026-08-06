package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onBackToOnboarding: () -> Unit,
    isSeniorMode: Boolean,
    onLoginWithEmail: ((String, String) -> Unit)? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var selectedRole by remember { mutableStateOf(UserRole.PATIENT) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val displayError = errorMessage ?: localError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SageDeep)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
                .background(SageDeep),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(12.dp, CircleShape),
                    shape = CircleShape,
                    color = WarmOffWhite
                ) {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_1785434955616),
                            contentDescription = "Logo Docta na Tshombo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Docta na Tshombo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Connexion sécurisée", fontSize = 13.sp, color = WaterGreenLight)
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.62f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = WarmOffWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = WaterGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Surface(
                            onClick = { selectedRole = UserRole.PATIENT },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedRole == UserRole.PATIENT) SageDeep else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Patient",
                                    color = if (selectedRole == UserRole.PATIENT) Color.White else SageDeep,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isSeniorMode) 18.sp else 14.sp
                                )
                            }
                        }
                        Surface(
                            onClick = { selectedRole = UserRole.PRATICIEN },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedRole == UserRole.PRATICIEN) SageDeep else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Praticien",
                                    color = if (selectedRole == UserRole.PRATICIEN) Color.White else SageDeep,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isSeniorMode) 18.sp else 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it; localError = null },
                    label = { Text("Email") },
                    placeholder = { Text("ex: joseph@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = SageMedium) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageDeep,
                        unfocusedBorderColor = SurfaceCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; localError = null },
                    label = { Text("Mot de passe") },
                    placeholder = { Text("Entrez votre mot de passe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = SageMedium) },
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

                if (displayError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = displayError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (identifier.isBlank() || password.isBlank()) {
                            localError = "Veuillez remplir votre email et mot de passe."
                        } else {
                            onLoginWithEmail?.invoke(identifier, password)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSeniorMode) 64.dp else 56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Se connecter", fontSize = if (isSeniorMode) 20.sp else 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBackToOnboarding) {
                    Text("Pas encore de compte ? Rejoignez-nous", color = SageMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Créé par Henock Aduma",
                    fontSize = 11.sp,
                    color = SageMedium.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
