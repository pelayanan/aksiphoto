package id.aksisoft.aksiphoto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.FilterCenterFocus
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import id.aksisoft.aksiphoto.ui.BlueAccent
import id.aksisoft.aksiphoto.ui.Primary
import id.aksisoft.aksiphoto.ui.TextSecondary
import kotlinx.coroutines.launch

private data class Slide(val icon: ImageVector, val title: String, val desc: String)

private val slides = listOf(
    Slide(
        Icons.Rounded.Layers,
        "Edit seperti Pro",
        "Filter, penyesuaian warna, dan sapuan kuas — semuanya di ponselmu.",
    ),
    Slide(
        Icons.Rounded.Brush,
        "Seret & Oles",
        "Kuas, teks, dan efek instan. Tanpa iklan, tanpa watermark.",
    ),
    Slide(
        Icons.Rounded.FilterCenterFocus,
        "Offline & Lokal",
        "Semua diproses di perangkat. Fotomu tetap milikmu.",
    ),
)

@Composable
fun OnboardingScreen(nav: NavHostController) {
    val pager = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF10141A), Color(0xFF1B1440), Color(0xFF251A4E))
                )
            ),
    ) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            val s = slides[page]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
            ) {
                Icon(
                    s.icon,
                    contentDescription = s.title,
                    tint = Primary,
                    modifier = Modifier.size(120.dp),
                )
                Spacer(Modifier.height(40.dp))
                Text(
                    s.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    s.desc,
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(slides.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (pager.currentPage == i) 22.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (pager.currentPage == i) Primary else Color(0xFF3A4350)),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            if (pager.currentPage < slides.size - 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    TextButton(
                        onClick = {
                            nav.context
                                .getSharedPreferences("aksiphoto", 0)
                                .edit().putBoolean("onboarding_done", true).apply()
                            nav.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                        },
                    ) { Text("Lewati", color = TextSecondary) }
                    Button(
                        onClick = {
                            scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                        },
                    ) { Text("Lanjut") }
                }
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    onClick = {
                        nav.context
                            .getSharedPreferences("aksiphoto", 0)
                            .edit().putBoolean("onboarding_done", true).apply()
                        nav.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                    },
                ) { Text("Mulai Mengedit") }
            }
        }
    }
}
