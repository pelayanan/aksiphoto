package id.aksisoft.aksiphoto.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import id.aksisoft.aksiphoto.ui.BgDark
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("aksiphoto", 0) }
    var defaultQuality by androidx.compose.runtime.remember {
        mutableFloatStateOf(prefs.getFloat("default_quality", 92f))
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Kualitas simpan bawaan",
                        color = Color(0xFFF2F6FA),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        Slider(
                            value = defaultQuality,
                            onValueChange = { defaultQuality = it },
                            onValueChangeFinished = {
                                prefs.edit().putFloat("default_quality", defaultQuality).apply()
                            },
                            valueRange = 50f..100f,
                            modifier = Modifier.padding(end = 12.dp).fillMaxWidth(0.75f),
                        )
                        Text(
                            "%d%%".format(defaultQuality.toInt()),
                            color = Color(0xFF9BB0C1),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Penyimpanan",
                        color = Color(0xFFF2F6FA),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        "Hasil edit tersimpan di Pictures/AksiPhoto. Cache bagikan/ekspor ditaruh di folder cache privat aplikasi.",
                        color = Color(0xFF9BB0C1),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Button(
                        onClick = {
                            var cleared = 0
                            File(ctx.cacheDir, "photos").listFiles()?.forEach {
                                if (it.delete()) cleared++
                            }
                            Toast.makeText(
                                ctx,
                                "Cache dibersihkan (" + cleared + " file)",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    ) { Text("Bersihkan Cache") }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Privasi",
                        color = Color(0xFFF2F6FA),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        "Semua pemrosesan dilakukan di perangkat. AksiPhoto tidak mengirim foto, tidak menaruh iklan, dan tidak meminta akun.",
                        color = Color(0xFF9BB0C1),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            Text(
                "AksiPhoto v1.1.0 (versionCode 2) · target Android 16 (API 36)",
                color = Color(0xFF6D7B89),
                fontSize = 12.sp,
            )
        }
    }
}
