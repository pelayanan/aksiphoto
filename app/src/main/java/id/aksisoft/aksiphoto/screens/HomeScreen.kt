package id.aksisoft.aksiphoto.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AwtColor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import id.aksisoft.aksiphoto.util.EditorSession
import id.aksisoft.aksiphoto.util.GalleryImage
import id.aksisoft.aksiphoto.util.MediaIO
import id.aksisoft.aksiphoto.ui.BgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var images by remember { mutableStateOf(listOf<GalleryImage>()) }
    var pendingCamUri by remember { mutableStateOf<Uri?>(null) }

    fun reload() {
        images = MediaIO.galleryImages(ctx)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) reload()
        else Toast.makeText(ctx, "Izin galeri diperlukan untuk menampilkan foto", Toast.LENGTH_LONG).show()
    }
    val camLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = pendingCamUri
        if (result.resultCode == android.app.Activity.RESULT_OK && uri != null) {
            MediaIO.finishPending(ctx, uri)
            Toast.makeText(ctx, ctx.getString(id.aksisoft.aksiphoto.R.string.msg_saved), Toast.LENGTH_SHORT).show()
            reload()
        } else if (uri != null) {
            try { ctx.contentResolver.delete(uri, null, null) } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        val perm = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(ctx, perm) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) reload() else permLauncher.launch(perm)
    }

    fun takePhoto() {
        val uri = MediaIO.createPendingCameraUri(ctx) ?: return
        pendingCamUri = uri
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        val ok = ctx.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        if (!ok) {
            Toast.makeText(ctx, ctx.getString(id.aksisoft.aksiphoto.R.string.msg_no_camera), Toast.LENGTH_SHORT).show()
            return
        }
        camLauncher.launch(intent)
    }

    fun openEditor(img: GalleryImage?) {
        if (img != null) {
            EditorSession.sourceUri = img.uri
            EditorSession.sourceName = img.name
        } else {
            EditorSession.sourceUri = null
            EditorSession.sourceName = ""
        }
        nav.navigate("editor")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AksiPhoto", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark),
                actions = {
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(Icons.Rounded.Settings, "Pengaturan", tint = Color(0xFF9BB0C1))
                    }
                    IconButton(onClick = { nav.navigate("about") }) {
                        Icon(Icons.Rounded.Info, "Tentang", tint = Color(0xFF9BB0C1))
                    }
                },
            )
        },
        containerColor = BgDark,
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                "Mulai aksi",
                color = Color(0xFF9BB0C1),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickAction(Icons.Rounded.PhotoLibrary, "Galeri", Modifier.weight(1f)) {
                    nav.navigate("gallery")
                }
                QuickAction(Icons.Rounded.AddAPhoto, "Kamera", Modifier.weight(1f)) { takePhoto() }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickAction(Icons.Rounded.Collections, "Foto Baru", Modifier.weight(1f)) {
                    openEditor(null)
                }
                QuickAction(Icons.Rounded.FolderSpecial, "Folder Saya", Modifier.weight(1f)) {
                    nav.navigate("files")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                QuickAction(Icons.Rounded.AutoFixHigh, "Batch Proses", Modifier.fillMaxWidth()) {
                    nav.navigate("batch")
                }
            }

            Text(
                "Foto terbaru",
                color = Color(0xFF9BB0C1),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 12.dp),
            )
            if (images.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2027)),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        "Belum ada foto.\nIzinkan akses galeri atau ambil foto lewat kamera, lalu kembali ke sini.",
                        color = Color(0xFF9BB0C1),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            } else {
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(images.size) { i ->
                        val img = images[i]
                        AsyncImage(
                            model = img.uri,
                            contentDescription = img.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(110.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { openEditor(img) },
                        )
                    }
                }
            }
            Text(
                "AksiPhoto v1.1.0 · offline · tanpa iklan",
                color = Color(0xFF6D7B89),
                fontSize = 12.sp,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2027)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() },
        onClick = { onClick() },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A2027), Color(0xFF171D25))
                    )
                )
                .padding(vertical = 18.dp),
        ) {
            Icon(icon, label, tint = Color(0xFF8B6CF0), modifier = Modifier.size(30.dp))
            Text(
                label,
                color = Color(0xFFF2F6FA),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
