package id.aksisoft.aksiphoto.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import id.aksisoft.aksiphoto.util.GalleryImage
import id.aksisoft.aksiphoto.util.MediaIO
import id.aksisoft.aksiphoto.ui.BgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var images by remember { mutableStateOf(listOf<GalleryImage>()) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var widthTarget by remember { mutableIntStateOf(1080) }
    var quality by remember { mutableFloatStateOf(88f) }
    var format by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var doneCount by remember { mutableIntStateOf(0) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) images = MediaIO.galleryImages(ctx)
        else Toast.makeText(ctx, "Izin galeri diperlukan", Toast.LENGTH_LONG).show()
    }
    LaunchedEffect(Unit) {
        val perm = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED) {
            images = MediaIO.galleryImages(ctx)
        } else {
            permLauncher.launch(perm)
        }
    }

    fun runBatch() {
        val chosen = images.filter { selected.contains(it.uri) }
        if (chosen.isEmpty()) {
            Toast.makeText(ctx, "Pilih minimal satu foto", Toast.LENGTH_SHORT).show()
            return
        }
        running = true
        progress = 0f
        doneCount = 0
        scope.launch {
            val fmt = when (format) {
                0 -> Bitmap.CompressFormat.JPEG
                1 -> Bitmap.CompressFormat.PNG
                else -> Bitmap.CompressFormat.WEBP
            }
            var ok = 0
            chosen.forEachIndexed { idx, img ->
                val res = withContext(Dispatchers.IO) {
                    try {
                        val src = MediaIO.loadSampled(ctx, img.uri.toUri(), 3000)
                            ?: return@withContext false
                        val scaled = scaleToWidth(src, widthTarget)
                        val uri = MediaIO.saveBitmap(
                            ctx, scaled, fmt, quality.toInt(), "batch",
                        )
                        uri != null
                    } catch (_: Exception) {
                        false
                    }
                }
                if (res) ok++
                doneCount = idx + 1
                progress = (idx + 1).toFloat() / chosen.size
            }
            running = false
            Toast.makeText(
                ctx,
                "Batch selesai: " + ok + "/" + chosen.size + " foto tersimpan di Pictures/AksiPhoto",
                Toast.LENGTH_LONG,
            ).show()
            if (ok > 0) {
                // Refresh agar foto batch yang baru tersimpan langsung terlihat
                withContext(Dispatchers.IO) { kotlinx.coroutines.delay(350) }
                images = MediaIO.galleryImages(ctx)
            }
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Batch Proses", fontWeight = FontWeight.Bold, color = Color.White) },
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
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Lebar target (px)", color = Color(0xFF9BB0C1), fontSize = 13.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        FilterChip(selected = widthTarget == 720, onClick = { widthTarget = 720 }, label = { Text("720") })
                        FilterChip(selected = widthTarget == 1080, onClick = { widthTarget = 1080 }, label = { Text("1080") })
                        FilterChip(selected = widthTarget == 2048, onClick = { widthTarget = 2048 }, label = { Text("2048") })
                        FilterChip(selected = widthTarget == 0, onClick = { widthTarget = 0 }, label = { Text("Asli") })
                    }
                    Text(
                        "Kualitas",
                        color = Color(0xFF9BB0C1),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Slider(
                            value = quality,
                            onValueChange = { quality = it },
                            valueRange = 40f..100f,
                            enabled = format != 1,
                            modifier = Modifier.padding(end = 12.dp).weight(1f),
                        )
                        Text(
                            "%d%%".format(quality.toInt()),
                            color = Color(0xFF9BB0C1),
                            fontSize = 13.sp,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = format == 0, onClick = { format = 0 }, label = { Text("JPG") })
                        FilterChip(selected = format == 1, onClick = { format = 1 }, label = { Text("PNG") })
                        FilterChip(selected = format == 2, onClick = { format = 2 }, label = { Text("WebP") })
                    }
                    if (running) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        )
                        Text(
                            "Memproses " + doneCount + " / " + selected.size,
                            color = Color(0xFF9BB0C1),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        Button(
                            onClick = {
                                if (selected.size == images.size && images.isNotEmpty()) {
                                    selected = setOf()
                                } else {
                                    selected = images.map { it.uri }.toSet()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                if (selected.size == images.size && images.isNotEmpty()) {
                                    "Kosongkan"
                                } else {
                                    "Pilih Semua"
                                },
                            )
                        }
                        Button(
                            onClick = { runBatch() },
                            enabled = !running && selected.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                        ) { Text("Proses " + selected.size + " Foto") }
                    }
                }
            }
            if (images.isEmpty()) {
                Text(
                    "Galeri kosong / izin belum diberikan",
                    color = Color(0xFF9BB0C1),
                    modifier = Modifier.padding(20.dp),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(images.size, key = { images[it].uri }) { i ->
                        val img = images[i]
                        val sel = selected.contains(img.uri)
                        AsyncImage(
                            model = img.uri,
                            contentDescription = img.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(Color(0xFF242C36), RoundedCornerShape(10.dp))
                                .clickable {
                                    selected =
                                        if (sel) selected - img.uri else selected + img.uri
                                }
                                .then(
                                    if (sel) {
                                        Modifier.border(
                                            3.dp,
                                            Color(0xFF6C5CE7),
                                            RoundedCornerShape(10.dp),
                                        )
                                    } else {
                                        Modifier
                                    }
                                ),
                        )
                    }
                }
            }
        }
    }
}

private fun scaleToWidth(bmp: Bitmap, targetWidth: Int): Bitmap {
    if (targetWidth <= 0 || bmp.width <= targetWidth) return bmp
    val h = (bmp.height.toFloat() * targetWidth / bmp.width).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bmp, targetWidth, h, true)
}
