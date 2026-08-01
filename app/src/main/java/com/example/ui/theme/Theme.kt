package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Hiérarchie de surfaces iOS 26 Liquid Glass en 4 niveaux
 */
data class LiquidGlassSurfaces(
    val sunken: Color,      // Niveau 1: Creux / Sunken
    val background: Color,  // Niveau 2: Toile de fond
    val surface: Color,     // Niveau 3: Cartes standards
    val elevated: Color,    // Niveau 4: Cartes surélevées / Sheets / Modals
    val glassBorder: Color, // Bordure translucide
    val glassSheen: Color   // Éclat réflectif
)

val LightLiquidGlassSurfaces = LiquidGlassSurfaces(
    sunken = iOSSurfaceSunkenLight,
    background = iOSBackgroundLight,
    surface = iOSSurfaceLight,
    elevated = iOSSurfaceElevatedLight,
    glassBorder = GlassBorderLight,
    glassSheen = GlassSheenLight
)

val DarkLiquidGlassSurfaces = LiquidGlassSurfaces(
    sunken = iOSSurfaceSunkenDark,
    background = iOSBackgroundDark,
    surface = iOSSurfaceDark,
    elevated = iOSSurfaceElevatedDark,
    glassBorder = GlassBorderDark,
    glassSheen = GlassSheenDark
)

val LocalLiquidGlassSurfaces = staticCompositionLocalOf { LightLiquidGlassSurfaces }

val MaterialTheme.liquidGlassSurfaces: LiquidGlassSurfaces
    @Composable
    @ReadOnlyComposable
    get() = LocalLiquidGlassSurfaces.current

private val iOS26LightColorScheme = lightColorScheme(
    primary = iOSPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = WaterGreen,
    onPrimaryContainer = TextDark,
    secondary = iOSSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = WaterGreenLight,
    onSecondaryContainer = TextDark,
    tertiary = iOSAccentLight,
    onTertiary = Color.White,
    tertiaryContainer = TerracottaLight,
    onTertiaryContainer = TerracottaDark,
    background = iOSBackgroundLight,
    onBackground = TextDark,
    surface = iOSSurfaceLight,
    onSurface = TextDark,
    surfaceVariant = WaterGreen,
    onSurfaceVariant = TextMuted,
    outline = SurfaceCardBorder,
    error = iOSError,
    onError = Color.White
)

private val iOS26DarkColorScheme = darkColorScheme(
    primary = iOSPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = SageDeep,
    onPrimaryContainer = WaterGreen,
    secondary = iOSSecondaryDark,
    onSecondary = Color.White,
    tertiary = iOSAccentDark,
    background = iOSBackgroundDark,          // Quasi-noir #111111
    onBackground = Color.White,
    surface = iOSSurfaceDark,               // #1C1C1E
    onSurface = Color.White,
    surfaceVariant = iOSSurfaceElevatedDark,// #2C2C2E
    onSurfaceVariant = SageLight,
    outline = Color(0x30FFFFFF),
    error = iOSError,
    onError = Color.White
)

@Composable
fun DoctaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isSeniorMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) iOS26DarkColorScheme else iOS26LightColorScheme
    val glassSurfaces = if (darkTheme) DarkLiquidGlassSurfaces else LightLiquidGlassSurfaces
    val typography = if (isSeniorMode) SeniorTypography else StandardTypography

    CompositionLocalProvider(
        LocalLiquidGlassSurfaces provides glassSurfaces
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}


