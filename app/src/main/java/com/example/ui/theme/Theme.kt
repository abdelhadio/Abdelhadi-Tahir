package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CinemaColorScheme = darkColorScheme(
  primary = CinemaRed,
  onPrimary = Color.White,
  primaryContainer = CinemaRedDark,
  onPrimaryContainer = Color.White,
  secondary = CinemaGold,
  onSecondary = Color.Black,
  secondaryContainer = CinemaGoldDark,
  onSecondaryContainer = Color.White,
  tertiary = ShieldGreen,
  onTertiary = Color.White,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CardBorder,
  surfaceTint = CinemaRed
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CinemaColorScheme,
    typography = Typography,
    content = content
  )
}

