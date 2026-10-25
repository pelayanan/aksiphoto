package id.aksisoft.aksiphoto.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import id.aksisoft.aksiphoto.R
import id.aksisoft.aksiphoto.ui.BlueAccent
import id.aksisoft.aksiphoto.ui.Primary
import id.aksisoft.aksiphoto.ui.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(nav: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF10141A), Color(0xFF231A4E)))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_image),
                contentDescription = "Logo AksiPhoto",
                modifier = Modifier.size(140.dp),
            )
            Text(
                text = "AksiPhoto",
                color = TextPrimary,
                fontSize = 34.sp,
                lineHeight = 44.sp,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = "Aksiin fotomu! · by Aksisoft Media",
                color = Color(0xFF9BB0C1),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            CircularProgressIndicator(
                color = Primary,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .size(30.dp),
            )
        }
    }
    LaunchedEffect(Unit) {
        delay(1400)
        val prefs = nav.context.getSharedPreferences("aksiphoto", 0)
        val seen = prefs.getBoolean("onboarding_done", false)
        val dest = if (seen) "home" else "onboarding"
        nav.navigate(dest) {
            popUpTo("splash") { inclusive = true }
        }
    }
}
