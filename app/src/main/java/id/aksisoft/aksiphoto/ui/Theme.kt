package id.aksisoft.aksiphoto.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF6C5CE7)
val PrimaryLight = Color(0xFF8B6CF0)
val BlueAccent = Color(0xFF2979FF)
val OrangeAccent = Color(0xFFFF7A3D)
val BgDark = Color(0xFF10141A)
val SurfaceDark = Color(0xFF1A2027)
val TextPrimary = Color(0xFFF2F6FA)
val TextSecondary = Color(0xFF9BB0C1)

private val DarkScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    secondary = BlueAccent,
    onSecondary = Color.White,
    tertiary = OrangeAccent,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF242C36),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF39424E),
)

private val LightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = BlueAccent,
    tertiary = OrangeAccent,
)

@Composable
fun AksiPhotoTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
