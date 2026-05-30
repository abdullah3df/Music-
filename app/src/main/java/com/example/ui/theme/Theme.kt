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

import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
  themeColor: AppThemeColor = AppThemeColor.PURPLE,
  content: @Composable () -> Unit,
) {
  // Create colorScheme dynamically based on selection
  val colorScheme = darkColorScheme(
      primary = themeColor.primary,
      onPrimary = Color.Black,
      primaryContainer = themeColor.primaryContainer,
      onPrimaryContainer = themeColor.primary,
      secondary = themeColor.primary.copy(alpha = 0.7f),
      background = themeColor.background,
      onBackground = DarkText, // DarkText is Color(0xFFE6E1E5)
      surface = themeColor.surface,
      onSurface = DarkText,
      surfaceVariant = themeColor.surfaceVariant,
      onSurfaceVariant = SecondaryGrey,
      outline = BorderDark
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
