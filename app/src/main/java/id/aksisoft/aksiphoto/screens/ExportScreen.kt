package id.aksisoft.aksiphoto.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import id.aksisoft.aksiphoto.util.EditorSession
import id.aksisoft.aksiphoto.util.MediaIO
import id.aksisoft.aksiphoto.ui.BgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var format by remember { mutableIntStateOf(0) } // 0 jpg, 1 png, 2 webp
    var quality by remember { mutableFloatStateOf(92f) }
    var res by remember { mutableIntStateOf(1080) } // sisi panjang; 0 = asli
    var saving by remember { mutableStateOf(false) }
    var savedUri by remember { mutableStateOf<Uri?>(null) }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Ekspor", fontWeight = FontWeight.Bold, color = Color.White) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExportCard("Format File") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = format == 0,
                        onClick = { format = 0 },
                        label = { Text("JPG") },
                    )
                    FilterChip(
                        selected = format == 1,
                        onClick = { format = 1 },
                        label = { Text("PNG") },
                    )
                    FilterChip(
                        selected = format == 2,
                        onClick = { format = 2 },
                        label = { Text("WebP") },
                    )
                }
            }
            ExportCard("Resolusi (sisi panjang)") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = res == 0, onClick = { res = 0 }, label = { Text("Asli") })
                    FilterChip(selected = res == 2048, onClick = { res = 2048 }, label = { Text("2048") })
                    FilterChip(selected = res == 1080, onClick = { res = 1080 }, label = { Text("1080") })
                    FilterChip(selected = res == 720, onClick = { res = 720 }, label = { Text("720") })
                }
            }
            ExportCard("Kualitas Kompresi") {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Slider(
                        value = quality,
                        onValueChange = { quality = it },
                        valueRange = 10f..100f,
                        enabled = format != 1,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "%d%%".format(quality.toInt()),
                        color = Color(0xFF9BB0C1),
                        fontSize = 13.sp,
                        modifier = Modifier.width(52.dp),
                    )
                }
            }
            Button(
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    saving = true
                    scope.launch {
                        val bmp = EditorSession.composite
                        if (bmp == null) {
                            saving = false
                            return@launch
                        }
                        val fmt = when (format) {
                            0 -> Bitmap.CompressFormat.JPEG
                            1 -> Bitmap.CompressFormat.PNG
                            else -> Bitmap.CompressFormat.WEBP
                        }
                        val out = withContext(Dispatchers.IO) {
                            val scaled = if (res > 0) scaleTo(bmp, res) else bmp
                            val uri = MediaIO.saveBitmap(
                                ctx, scaled, fmt, quality.toInt(), "aksiphoto",
                            )
                            uri
                        }
                        savedUri = out
                        saving = false
                        if (out != null) {
                            Toast.makeText(
                                ctx,
                                "Tersimpan di Pictures/AksiPhoto",
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            Toast.makeText(ctx, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
            ) {
                Text(if (saving) "Menyimpan…" else "Simpan ke Galeri")
            }
            OutlinedButton(
                enabled = savedUri != null,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val uri = savedUri ?: return@OutlinedButton
                    scope.launch {
                        val file = withContext(Dispatchers.IO) { MediaIO.shareFile(ctx, uri) }
                        if (file != null) {
                            val shareUri = FileProvider.getUriForFile(
                                ctx, ctx.packageName + ".fileprovider", file,
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, shareUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            ctx.startActivity(
                                Intent.createChooser(intent, "Bagikan hasil edit"),
                            )
                        }
                    }
                },
            ) { Text("Bagikan Hasil") }
            Text(
                "Sumber: " + (EditorSession.sourceName.ifEmpty { "Kanvas baru AksiPhoto" }),
                color = Color(0xFF6D7B89),
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ExportCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color(0xFF9BB0C1), fontSize = 13.sp)
            Spacer(Modifier.padding(4.dp))
            content()
        }
    }
}

private fun scaleTo(bmp: Bitmap, longSide: Int): Bitmap {
    val w = bmp.width
    val h = bmp.height
    if (w <= 0 || h <= 0) return bmp
    val scale = longSide.toFloat() / maxOf(w, h)
    if (scale >= 1f) return bmp
    val nw = (w * scale).toInt().coerceAtLeast(1)
    val nh = (h * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bmp, nw, nh, true)
}
