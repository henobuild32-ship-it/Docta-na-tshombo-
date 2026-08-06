package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.theme.*
import kotlin.math.sin

/**
 * DNA Molecule / Organic Pulse Loading Animation
 * "Les chargements sont indiqués par une animation de molécule ADN stylisée qui pulse calmement"
 */
@Composable
fun MoleculeLoadingAnimation(
    modifier: Modifier = Modifier,
    color: Color = SageDeep
) {
    val infiniteTransition = rememberInfiniteTransition(label = "molecule")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .size(80.dp, 40.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val nodeCount = 6
        val step = width / (nodeCount - 1)

        for (i in 0 until nodeCount) {
            val x = i * step
            val angle = phase + i * 0.8f
            val yOffset = sin(angle.toDouble()).toFloat() * (height / 3f)

            val y1 = centerY + yOffset
            val y2 = centerY - yOffset

            // Connecting rung line
            drawLine(
                color = color.copy(alpha = 0.4f),
                start = Offset(x, y1),
                end = Offset(x, y2),
                strokeWidth = 2.5f
            )

            // Top strand node
            drawCircle(
                color = color,
                radius = 5.dp.toPx(),
                center = Offset(x, y1)
            )

            // Bottom strand node (Terracotta accent node)
            drawCircle(
                color = if (i % 2 == 0) Terracotta else SageMedium,
                radius = 5.dp.toPx(),
                center = Offset(x, y2)
            )
        }
    }
}

/**
 * Floating Push Notification Banner (Simulating native iOS / Android push notification)
 */
@Composable
fun PushNotificationBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = SageDeep,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Notification",
                            tint = TerracottaLight
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Docta na Tshombo • Suivi Santé",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaterGreenLight
                        )
                        Text(
                            text = message,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Fermer",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bandeau de superposition « Actualisation en cours… » affiché pendant la
 * synchronisation des données réelles après une action de création.
 */
@Composable
fun RefreshingOverlay(message: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = SageDeep,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = WaterGreenLight,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Modifier Extensions for iOS 26 Liquid Glass Glassmorphism
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.75f),
    borderColor: Color = GlassBorderLight,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    blurRadius: Dp = 12.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.08f),
        spotColor = Color.Black.copy(alpha = 0.12f)
    )
    .blur(blurRadius)
    .clip(shape)
    .background(backgroundColor)
    .border(BorderStroke(borderWidth, borderColor), shape)

fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.85f),
    borderColor: Color = GlassBorderLight
): Modifier = this
    .shadow(elevation = 6.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.1f))
    .clip(shape)
    .background(backgroundColor)
    .border(BorderStroke(1.dp, borderColor), shape)

fun Modifier.glassSheet(
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.92f),
    borderColor: Color = GlassBorderLight
): Modifier = this
    .shadow(elevation = 16.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.15f))
    .clip(shape)
    .background(backgroundColor)
    .border(BorderStroke(1.2.dp, borderColor), shape)

/**
 * Reusable iOS 26 Liquid Glass Card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    borderColor: Color = GlassBorderLight,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .then(clickModifier),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Reusable iOS 26 Liquid Glass Pill / Capsule Action Button
 */
@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    containerColor: Color = iOSPrimaryLight,
    contentColor: Color = Color.White,
    isSeniorMode: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(if (isSeniorMode) 60.dp else 50.dp)
            .shadow(10.dp, CircleShape, spotColor = containerColor.copy(alpha = 0.4f)),
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(if (isSeniorMode) 22.dp else 18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (isSeniorMode) 18.sp else 15.sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

/**
 * Reusable iOS 26 Glass Filter Chip
 */
@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isSeniorMode: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) SageDeep else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        border = BorderStroke(
            1.dp,
            if (selected) SageDeep else GlassBorderLight
        ),
        shadowElevation = if (selected) 4.dp else 1.dp,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = if (isSeniorMode) 18.dp else 14.dp,
                vertical = if (isSeniorMode) 10.dp else 8.dp
            ),
            fontSize = if (isSeniorMode) 16.sp else 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * ✨ Element Signature "Liquid Glass" — Health Hero Card (Wow Factor ~40% Screen Height)
 * Note: Les données de santé (healthScore, spO2, etc.) doivent venir d'une source externe
 * ou être laissées vides si non disponibles. Les valeurs par défaut sont neutres.
 */
@Composable
fun LiquidGlassHealthHeroCard(
    userName: String,
    healthScore: Int? = null, // null = non disponible
    spO2: String? = null, // null = non disponible
    heartRate: String? = null, // null = non disponible
    bp: String? = null, // null = non disponible
    onStartTeleconsult: () -> Unit,
    onBookAppointment: () -> Unit,
    isSeniorMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val pulseAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_ring"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = SageDeep.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SageDeep,
                            SageMedium,
                            iOSSecondaryLight.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(iOSSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Constantes Vitales RDC • En Direct",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Score de Santé",
                            fontSize = if (isSeniorMode) 16.sp else 13.sp,
                            color = WaterGreenLight,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = healthScore?.toString() ?: "--",
                                fontSize = if (isSeniorMode) 42.sp else 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (healthScore != null) " /100 • Excellent" else " /100",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WaterGreenLight,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                    }

                    // Interactive Animated Progress Ring
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 8.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            val center = Offset(size.width / 2, size.height / 2)

                            // Track Ring
                            drawCircle(
                                color = Color.White.copy(alpha = 0.15f),
                                radius = radius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )

                            // Animated Progress Sweep Ring
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(iOSSuccess, WaterGreen, Color.White, iOSSuccess)
                                ),
                                startAngle = pulseAngle,
                                sweepAngle = ((healthScore ?: 0) / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = TerracottaLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = healthScore?.let { "$it%" } ?: "--%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Vitals Row (SpO2, Pulse, Blood Pressure) in Glass Capsules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VitalCapsuleItem(
                        label = "Pouls",
                        value = heartRate ?: "-- bpm",
                        icon = Icons.Outlined.MonitorHeart,
                        modifier = Modifier.weight(1f)
                    )
                    VitalCapsuleItem(
                        label = "SpO2",
                        value = spO2 ?: "--%",
                        icon = Icons.Outlined.Air,
                        modifier = Modifier.weight(1f)
                    )
                    VitalCapsuleItem(
                        label = "Tension",
                        value = bp ?: "--/--",
                        icon = Icons.Outlined.Thermostat,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Quick Action Pill Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassPillButton(
                        text = "Téléconsultation Instantanée",
                        onClick = onStartTeleconsult,
                        icon = Icons.Outlined.VideoCall,
                        containerColor = Color.White,
                        contentColor = SageDeep,
                        isSeniorMode = isSeniorMode,
                        modifier = Modifier.weight(1f)
                    )
                    GlassPillButton(
                        text = "Rendez-vous",
                        onClick = onBookAppointment,
                        icon = Icons.Outlined.CalendarMonth,
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White,
                        isSeniorMode = isSeniorMode,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VitalCapsuleItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = WaterGreenLight
            )
        }
    }
}

/**
 * Glassmorphic Header with Greeting & Senior Mode Button
 */
@Composable
fun OrganicHeader(
    userName: String,
    greetingEmoji: String = "🌤️",
    isSeniorMode: Boolean,
    onToggleSeniorMode: () -> Unit,
    onAvatarClick: () -> Unit,
    profileImageUri: String? = null,
    onOpenPresentation: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
        border = BorderStroke(0.5.dp, GlassBorderLight),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bonjour ",
                        style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$userName $greetingEmoji",
                        style = if (isSeniorMode) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Espace de santé sécurisé • iOS 26",
                    fontSize = if (isSeniorMode) 16.sp else 12.sp,
                    color = SageMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // App presentation / discovery button
                if (onOpenPresentation != null) {
                    Surface(
                        onClick = onOpenPresentation,
                        shape = CircleShape,
                        color = WaterGreen,
                        border = BorderStroke(1.dp, SageDeep.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Présentation de l'application",
                            tint = SageDeep,
                            modifier = Modifier.padding(9.dp).size(20.dp)
                        )
                    }
                }

                // Settings / Profile button
                if (onOpenSettings != null) {
                    Surface(
                        onClick = onOpenSettings,
                        shape = CircleShape,
                        color = WaterGreen,
                        border = BorderStroke(1.dp, SageDeep.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Paramètres",
                            tint = SageDeep,
                            modifier = Modifier.padding(9.dp).size(20.dp)
                        )
                    }
                }

                // Senior accessibility mode toggle pill
                Surface(
                    onClick = onToggleSeniorMode,
                    shape = CircleShape,
                    color = if (isSeniorMode) Terracotta else SageDeep.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isSeniorMode) TerracottaDark else SageDeep.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSeniorMode) Icons.Outlined.AccessibilityNew else Icons.Outlined.TextFormat,
                            contentDescription = "Mode Senior",
                            tint = if (isSeniorMode) Color.White else SageDeep,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSeniorMode) "Senior ON" else "Senior",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSeniorMode) Color.White else SageDeep
                        )
                    }
                }

                // User Avatar with connected indicator ring
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.dp, SageDeep, CircleShape)
                        .clickable { onAvatarClick() }
                        .background(WaterGreen),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Photo de profil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (userName.isNotBlank()) userName.take(1).uppercase() else "U",
                            fontWeight = FontWeight.Bold,
                            color = SageDeep,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

