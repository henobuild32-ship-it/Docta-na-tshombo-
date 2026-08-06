package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.UserRole
import com.example.ui.theme.SageDeep
import com.example.ui.theme.SageLight
import com.example.ui.theme.SageMedium
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmOffWhite
import com.example.ui.theme.WaterGreen
import com.example.ui.theme.WaterGreenLight
import kotlinx.coroutines.launch

private data class PresentationSlide(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val bullets: List<String>
)

private val patientSlides = listOf(
    PresentationSlide(
        emoji = "🏥",
        title = "Bienvenue sur Docta na Tshombo",
        subtitle = "Votre espace santé personnel, simple et sécurisé.",
        bullets = listOf(
            "Trouvez des praticiens vérifiés près de chez vous",
            "Prenez rendez-vous en quelques secondes",
            "Vos données médicales restent privées"
        )
    ),
    PresentationSlide(
        emoji = "🔎",
        title = "Trouvez votre praticien",
        subtitle = "Recherchez par nom, spécialité ou motif de consultation.",
        bullets = listOf(
            "Filtres par spécialité : Pédiatrie, Généraliste…",
            "Profils détaillés : diplômes, parcours, disponibilités",
            "Disponibilités réelles affichées en direct"
        )
    ),
    PresentationSlide(
        emoji = "📅",
        title = "Prenez rendez-vous en 3 étapes",
        subtitle = "En présentiel ou en téléconsultation, choisissez votre créneau.",
        bullets = listOf(
            "Sélectionnez le jour et l'heure qui vous conviennent",
            "Ajoutez un motif et une note pour le médecin",
            "Recevez une confirmation immédiate"
        )
    ),
    PresentationSlide(
        emoji = "💊",
        title = "Suivez vos traitements",
        subtitle = "Rappels de médicaments, ordonnances et messagerie sécurisée.",
        bullets = listOf(
            "Rappels quotidiens de vos prises",
            "Ordonnances numériques téléchargeables",
            "Discutez en direct avec votre praticien"
        )
    )
)

private val practitionerSlides = listOf(
    PresentationSlide(
        emoji = "🩺",
        title = "Bienvenue, Docteur",
        subtitle = "Gérez votre cabinet depuis votre poche.",
        bullets = listOf(
            "Votre agenda et vos consultations centralisés",
            "Vos disponibilités publiées automatiquement",
            "Un profil professionnel valorisant votre parcours"
        )
    ),
    PresentationSlide(
        emoji = "📋",
        title = "Votre agenda & vos consultations",
        subtitle = "Suivez chaque rendez-vous patient par patient.",
        bullets = listOf(
            "Liste des rendez-vous confirmés en temps réel",
            "Statuts : confirmé, en attente, terminé, annulé",
            "Téléconsultation en salon virtuel sécurisé"
        )
    ),
    PresentationSlide(
        emoji = "✍️",
        title = "Ordonnances numériques",
        subtitle = "Prescrivez et signez électroniquement en un geste.",
        bullets = listOf(
            "Génération de PDF signé et daté",
            "Historique complet de vos prescriptions",
            "Envoi direct au patient"
        )
    ),
    PresentationSlide(
        emoji = "💬",
        title = "Messagerie sécurisée",
        subtitle = "Communiquez en toute confidentialité avec vos patients.",
        bullets = listOf(
            "Conversations liées à chaque rendez-vous",
            "Messages chiffrés, stockés en toute sécurité",
            "Notifications à chaque nouveau message"
        )
    )
)

/**
 * Présentation de l'application, affichée à chaque nouvelle personne connectée
 * (patients comme praticiens) selon le rôle choisi. Une fois terminée, le flag
 * `presentation_seen` est enregistré pour cet utilisateur et la présentation
 * reste accessible depuis les paramètres.
 */
@Composable
fun AppPresentationScreen(
    role: UserRole,
    userName: String,
    isSeniorMode: Boolean,
    onComplete: () -> Unit
) {
    val slides = if (role == UserRole.PRATICIEN) practitionerSlides else patientSlides
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage >= slides.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(SageDeep, SageMedium, WarmOffWhite))
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape),
                shape = CircleShape,
                color = WarmOffWhite
            ) {
                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1785434955616),
                        contentDescription = "Logo Docta na Tshombo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(
                text = "Docta na Tshombo",
                fontWeight = FontWeight.Bold,
                fontSize = if (isSeniorMode) 20.sp else 16.sp,
                color = Color.White
            )
            TextButton(onClick = onComplete) {
                Text("Passer", color = WaterGreenLight, fontWeight = FontWeight.Bold)
            }
        }

        // Slides
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Emoji badge
                Surface(
                    modifier = Modifier
                        .size(if (isSeniorMode) 150.dp else 120.dp)
                        .shadow(18.dp, CircleShape),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = slide.emoji,
                            fontSize = if (isSeniorMode) 72.sp else 56.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = slide.title,
                    style = if (isSeniorMode) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineMedium,
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = slide.subtitle,
                    style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Feature bullets
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, SageLight)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        slide.bullets.forEach { bullet ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(WaterGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = SageDeep,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = bullet,
                                    fontSize = if (isSeniorMode) 17.sp else 14.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) SageDeep else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (isLast) {
                        onComplete()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSeniorMode) 64.dp else 56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
            ) {
                Text(
                    text = if (isLast) {
                        if (role == UserRole.PRATICIEN) "Commencer à gérer mon cabinet" else "C'est parti, $userName !"
                    } else {
                        "Continuer"
                    },
                    fontSize = if (isSeniorMode) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!isLast) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null)
                }
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = { onComplete() }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Passer cette présentation", color = SageDeep)
                }
            }
        }
    }
}
