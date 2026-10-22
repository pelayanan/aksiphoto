package id.aksisoft.aksiphoto.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import id.aksisoft.aksiphoto.BuildConfig
import id.aksisoft.aksiphoto.R
import id.aksisoft.aksiphoto.ui.BgDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Tentang", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Kembali",
                            tint = Color(0xFFF2F6FA),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark),
            )
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "Logo AksiPhoto",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp)),
            )
            Text(
                "AksiPhoto",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Aksi = aksi + Photo. Editor foto lokal yang ringan, offline, tanpa iklan.",
                color = Color(0xFF9BB0C1),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Dibuat oleh Aksisoft Media",
                        color = Color(0xFFF2F6FA),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        "Software house Surabaya, fokus tools kreator lokal. Website: aksisoft.web.id · hello@aksisoft.web.id",
                        color = Color(0xFF9BB0C1),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Button(
                        onClick = {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://aksisoft.web.id")),
                            )
                        },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) { Text("Kunjungi aksisoft.web.id") }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Teknis",
                        color = Color(0xFFF2F6FA),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        "Versi " + BuildConfig.VERSION_NAME + " (versionCode " + BuildConfig.VERSION_CODE + ")\n"
                            + "Package: id.aksisoft.aksiphoto\n"
                            + "Target: Android 16 (API 36)\n"
                            + "Kotlin + Jetpack Compose · MIT License utama",
                        color = Color(0xFF9BB0C1),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            Text(
                "Aksiin fotomu! 📸",
                color = Color(0xFF8B6CF0),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
