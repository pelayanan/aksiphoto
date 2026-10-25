package id.aksisoft.aksiphoto.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import id.aksisoft.aksiphoto.ui.BgDark
import id.aksisoft.aksiphoto.util.EditorSession
import id.aksisoft.aksiphoto.util.GalleryImage
import id.aksisoft.aksiphoto.util.MediaIO
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

/** Handoff data gagleri → viewer. */
object ViewerSession {
    var images: List<GalleryImage> = emptyList()
    var startIndex: Int = 0
    /** Diberi iterator tetap di depan (tanggal dd MMM yyyy). */
    private val dateFmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)

    fun dateLabel(dateAdded: Long): String = dateFmt.format(java.util.Date(dateAdded * 1000L))
}

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 6f

/**
 * Viewer foto layar penuh:
 * - pinch & double-tap zoom, geser saat zoom
 * - swipe kiri/kanan antar foto
 * - tap satu kali → sembunyikan tampilan
 * - aksi: Bagikan, Info, Edit, Hapus
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewerScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var images by remember { mutableStateOf(ViewerSession.images) }
    val pagerState = rememberPagerState(
        initialPage = ViewerSession.startIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
        initialPageOffsetFraction = 0f,
    ) { images.size }

    var scale by remember { mutableFloatStateOf(MIN_SCALE) }
    var offset by remember { mutableStateOf(                Offset.Zero) }
    var chromeVisible by remember { mutableStateOf(true) }
    var infoFor by remember { mutableStateOf<GalleryImage?>(null) }
    var deleteFor by remember { mutableStateOf<GalleryImage?>(null) }

    // Reset zoom saat pindah halaman
    LaunchedEffect(pagerState.currentPage) {
        scale = MIN_SCALE
        offset =                 Offset.Zero
    }

    // Hapus konfirmasi (Android R+ menggunakan dialog sistem)
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = images.getOrNull(pagerState.currentPage)?.uri
            if (uri != null) {
                images = images.filterNot { it.uri == uri }
                ViewerSession.images = images
                Toast.makeText(ctx, "Terhapus", Toast.LENGTH_SHORT).show()
                if (images.isEmpty()) nav.popBackStack()
            }
        }
    }

    fun share(img: GalleryImage) {
        val file = MediaIO.shareFile(ctx, Uri.parse(img.uri))
        if (file != null) {
            val shareUri = FileProvider.getUriForFile(
                ctx, ctx.packageName + ".fileprovider", file,
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ctx.startActivity(Intent.createChooser(intent, "Bagikan"))
        } else {
            Toast.makeText(ctx, "Tidak dapat membagikan", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestDelete(img: GalleryImage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pi = MediaStore.createDeleteRequest(
                    ctx.contentResolver, listOf(Uri.parse(img.uri)),
                )
                deleteLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
            } catch (_: Exception) {
                Toast.makeText(ctx, "Tidak dapat menghapus", Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                ctx.contentResolver.delete(Uri.parse(img.uri), null, null)
                images = images.filterNot { it.uri == img.uri }
                ViewerSession.images = images
                Toast.makeText(ctx, "Terhapus", Toast.LENGTH_SHORT).show()
                if (images.isEmpty()) nav.popBackStack()
            } catch (_: Exception) {
                Toast.makeText(ctx, "Tidak dapat menghapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
    ) {
        if (images.isEmpty()) {
            LaunchedEffect(Unit) { nav.popBackStack() }
            return@Box
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(state = pagerState) { page ->
                val img = images[page]
                AsyncImage(
                    model = img.uri,
                    contentDescription = img.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                var hadPinch = false
                                var active = true
                                while (active) {
                                    val event = awaitPointerEvent()
                                    val pressed = event.changes.count { it.pressed }
                                    if (pressed == 0) {
                                        active = false
                                        break
                                    }
                                    if (pressed >= 2) {
                                        hadPinch = true
                                        val zoom = event.calculateZoom()
                                        val pan = event.calculatePan()
                                        val newScale = (scale * zoom).coerceIn(
                                            MIN_SCALE, MAX_SCALE,
                                        )
                                        scale = newScale
                                        offset = if (newScale > MIN_SCALE) {
                                            offset + pan
                                        } else {
                                                            Offset.Zero
                                        }
                                        event.changes.forEach { it.consume() }
                                    } else if (hadPinch && scale > MIN_SCALE) {
                                        val drag = event.changes.first().positionChange()
                                        offset += drag
                                        event.changes.forEach { it.consume() }
                                    } else if (hadPinch) {
                                        hadPinch = false
                                    }
                                }
                                if (scale < MIN_SCALE * 1.02f) {
                                    scale = MIN_SCALE
                                    offset =                 Offset.Zero
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { chromeVisible = !chromeVisible },
                                onDoubleTap = {
                                    if (scale > MIN_SCALE) {
                                        scale = MIN_SCALE
                                        offset =                 Offset.Zero
                                    } else {
                                        scale = 2.5f
                                        offset =                 Offset.Zero
                                    }
                                },
                            )
                        },
                )
            }
        }
        val current = images.getOrNull(pagerState.currentPage) ?: images.first()

        // Bar Atas
        if (chromeVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .background(Color(0x99000000))
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        "Kembali",
                        tint = Color.White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        current.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        "${pagerState.currentPage + 1}/${images.size}",
                        color = Color(0xFF9BB0C1),
                        fontSize = 12.sp,
                    )
                }
            }

            // Bar Aksi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .background(Color(0x99000000))
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BottomAction(Icons.AutoMirrored.Rounded.Send, "Bagikan") {
                    share(current)
                }
                BottomAction(Icons.Rounded.Info, "Info") { infoFor = current }
                BottomAction(Icons.Rounded.Edit, "Edit") {
                    EditorSession.sourceUri = current.uri
                    EditorSession.sourceName = current.name
                    nav.navigate("editor")
                }
                BottomAction(Icons.Rounded.Delete, "Hapus") { deleteFor = current }
            }
        }
    }

    if (infoFor != null) {
        val img = infoFor!!
        AlertDialog(
            onDismissRequest = { infoFor = null },
            containerColor = Color(0xFF242C36),
            title = { Text("Info Foto", color = Color.White) },
            text = {
                Column {
                    Text("Nama: ${img.name}", color = Color.White, fontSize = 14.sp)
                    Text(
                        "Tanggal: ${ViewerSession.dateLabel(img.dateAdded)}",
                        color = Color(0xFF9BB0C1),
                        fontSize = 14.sp,
                    )
                    Text(
                        "Sumber: ${if (img.mine) "Folder AksiPhoto" else "Galeri"}",
                        color = Color(0xFF9BB0C1),
                        fontSize = 14.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { infoFor = null }) {
                    Text("Tutup", color = Color(0xFF8E7BFF))
                }
            },
        )
    }

    if (deleteFor != null) {
        AlertDialog(
            onDismissRequest = { deleteFor = null },
            containerColor = Color(0xFF242C36),
            title = { Text("Hapus foto?", color = Color.White) },
            text = {
                Text(
                    "${deleteFor!!.name} akan dihapus dari galeri.",
                    color = Color(0xFF9BB0C1),
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val img = deleteFor!!
                    deleteFor = null
                    requestDelete(img)
                }) {
                    Text("Hapus", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteFor = null }) {
                    Text("Batal", color = Color(0xFF9BB0C1))
                }
            },
        )
    }
}

@Composable
private fun BottomAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Icon(icon, label, tint = Color.White)
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}
