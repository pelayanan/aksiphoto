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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import id.aksisoft.aksiphoto.util.EditorSession
import id.aksisoft.aksiphoto.util.GalleryImage
import id.aksisoft.aksiphoto.util.MediaIO
import id.aksisoft.aksiphoto.ui.BgDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var images by remember { mutableStateOf(listOf<GalleryImage>()) }
    var menuFor by remember { mutableStateOf<GalleryImage?>(null) }

    fun reload() {
        images = MediaIO.myImages(ctx)
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(ctx, "Terhapus", Toast.LENGTH_SHORT).show()
            reload()
        }
    }

    LaunchedEffect(Unit) { reload() }

    fun openEditor(img: GalleryImage) {
        EditorSession.sourceUri = img.uri
        EditorSession.sourceName = img.name
        nav.navigate("editor")
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
        }
    }

    fun requestDelete(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pi = MediaStore.createDeleteRequest(ctx.contentResolver, listOf(uri))
                deleteLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
            } catch (_: Exception) {
                Toast.makeText(ctx, "Tidak dapat menghapus", Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                ctx.contentResolver.delete(uri, null, null)
                Toast.makeText(ctx, "Terhapus", Toast.LENGTH_SHORT).show()
                reload()
            } catch (_: Exception) {
                Toast.makeText(ctx, "Tidak dapat menghapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Folder AksiPhoto",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            "Pictures/AksiPhoto",
                            color = Color(0xFF9BB0C1),
                            fontSize = 12.sp,
                        )
                    }
                },
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
            if (images.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Belum ada file", color = Color(0xFFF2F6FA), fontSize = 17.sp)
                    Text(
                        "Semua hasil edit, kamera, dan batch tersimpan di folder ini.",
                        color = Color(0xFF9BB0C1),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
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
                                .clickable { menuFor = img },
                        )
                    }
                }
            }
        }
    }

    menuFor?.let { img ->
        AlertDialog(
            onDismissRequest = { menuFor = null },
            title = { Text(img.name, maxLines = 2) },
            text = {
                Text(
                    "Edit lagi, bagikan, atau hapus file ini.",
                    color = Color(0xFF9BB0C1),
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                Column {
                    Button(
                        onClick = {
                            menuFor = null
                            openEditor(img)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Edit di Editor") }
                    OutlinedButton(
                        onClick = {
                            menuFor = null
                            share(img)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) { Text("Bagikan") }
                    TextButton(
                        onClick = {
                            menuFor = null
                            requestDelete(Uri.parse(img.uri))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) { Text("Hapus", color = Color(0xFFFF5252)) }
                }
            },
        )
    }
}
