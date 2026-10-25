package id.aksisoft.aksiphoto.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import id.aksisoft.aksiphoto.util.GalleryImage
import id.aksisoft.aksiphoto.util.MediaIO
import id.aksisoft.aksiphoto.ui.BgDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var images by remember { mutableStateOf(listOf<GalleryImage>()) }
    var mine by remember { mutableIntStateOf(0) } // 0 = semua, 1 = folder AksiPhoto

    fun reload() {
        images = if (mine == 1) MediaIO.myImages(ctx) else MediaIO.galleryImages(ctx)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) reload()
        else {
            Toast.makeText(ctx, "Izin galeri diperlukan", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(mine) {
        val perm = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(ctx, perm) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) reload() else permLauncher.launch(perm)
    }

    // Muat ulang saat kembali ke galeri (mis. foto dihapus lewat viewer)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(mine == 1) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val permOk = ContextCompat.checkSelfPermission(
                    ctx,
                    if (Build.VERSION.SDK_INT >= 33) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                ) == PackageManager.PERMISSION_GRANTED
                if (permOk) reload()
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    fun open(img: GalleryImage) {
        ViewerSession.images = images
        ViewerSession.startIndex = images.indexOfFirst { it.uri == img.uri }.coerceAtLeast(0)
        nav.navigate("viewer")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galeri", fontWeight = FontWeight.Bold, color = Color.White) },
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
        containerColor = BgDark,
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { mine = 0 },
                    label = { Text("Semua Foto") },
                    colors = chipColors(mine == 0),
                )
                AssistChip(
                    onClick = { mine = 1 },
                    label = { Text("Folder AksiPhoto") },
                    colors = chipColors(mine == 1),
                )
            }
            if (images.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Belum ada foto yang terlihat",
                        color = Color(0xFFF2F6FA),
                        fontSize = 17.sp,
                    )
                    Text(
                        "Izinkan akses galeri lalu buka lagi.",
                        color = Color(0xFF9BB0C1),
                        fontSize = 14.sp,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        12.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(images.size, key = { images[it].uri }) { i ->
                        val img = images[i]
                        AsyncImage(
                            model = img.uri,
                            contentDescription = img.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { open(img) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun chipColors(selected: Boolean) = if (selected) {
    androidx.compose.material3.AssistChipDefaults.assistChipColors(
        containerColor = Color(0xFF6C5CE7),
        labelColor = Color.White,
    )
} else {
    androidx.compose.material3.AssistChipDefaults.assistChipColors(
        containerColor = Color(0xFF242C36),
        labelColor = Color(0xFF9BB0C1),
    )
}
