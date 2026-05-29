package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ElegantDarkColorScheme = darkColorScheme(
    primary = LightPurple,
    onPrimary = DeepPurple,
    primaryContainer = DeepPurple,
    onPrimaryContainer = SoftPurple,
    secondary = PurpleGrey80,
    background = DarkBg,
    onBackground = DarkText,
    surface = SurfaceDark,
    onSurface = DarkText,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = SecondaryGrey,
    outline = BorderDark
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  // Enforce Elegant Dark styling
  val colorScheme = ElegantDarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
