package id.aksisoft.aksiphoto.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import android.graphics.BitmapShader
import android.graphics.PorterDuff
import android.graphics.Shader
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Delete

import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import id.aksisoft.aksiphoto.util.EditorSession
import id.aksisoft.aksiphoto.util.FilterEngine
import id.aksisoft.aksiphoto.util.MediaIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Tool { BRUSH, ERASER, TEXT, CROP }

private val brushColors = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFF6C5CE7, 0xFF2979FF,
    0xFFFF5252, 0xFFFFEA00, 0xFF00E676, 0xFFFF7A3D,
).map { it.toInt() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    var baked by remember { mutableStateOf<Bitmap?>(null) }
    var original by remember { mutableStateOf<Bitmap?>(null) } // sumber penghapus
    var presetIndex by remember { mutableIntStateOf(0) }
    var brightness by remember { mutableFloatStateOf(1f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var tool by remember { mutableStateOf(Tool.BRUSH) }
    var color by remember { mutableIntStateOf(brushColors[2]) }
    var strokeUi by remember { mutableFloatStateOf(20f) } // dp
    var undoStack by remember { mutableStateOf(listOf<Bitmap>()) }
    var redoStack by remember { mutableStateOf(listOf<Bitmap>()) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var showTextDialog by remember { mutableStateOf(false) }
    var showAdjust by remember { mutableStateOf(false) }
    var showColorPanel by remember { mutableStateOf(false) }
    var showCropMenu by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var textDraft by remember { mutableStateOf("") }
    var textSizeDraft by remember { mutableFloatStateOf(6f) } // % lebar bitmap
    var path = androidx.compose.runtime.remember { mutableStateListOf<Offset>() }

    // ---- load image saat masuk layar
    LaunchedEffect(Unit) {
        val uri = EditorSession.sourceUri
        val src = withContext(Dispatchers.IO) {
            if (uri != null) {
                MediaIO.loadSampled(ctx, uri.toUri())
            } else {
                val bmp = Bitmap.createBitmap(1440, 1800, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                c.drawColor(0xFFF7F4EE.toInt())
                val p = Paint().apply { color = 0xFF6C5CE7.toInt() }
                p.textSize = 70f
                p.isAntiAlias = true
                c.drawText("AksiPhoto", 120f, 950f, p)
                bmp
            }
        }
        baked = src
        original = src?.copy(Bitmap.Config.ARGB_8888, false)
        loading = src == null
        if (src != null) EditorSession.composite = src
    }

    // ---- matriks pratinjau live (ColorFilter)
    val previewMatrix = FilterEngine.combinedMatrix(presetIndex, brightness, contrast, saturation)
    val previewColors = androidx.compose.ui.graphics.ColorMatrix(
        previewMatrix.getArray(),
    )

    fun snapshotMatrix() = FilterEngine.combinedMatrix(presetIndex, brightness, contrast, saturation)

    fun pushUndo(current: Bitmap) {
        undoStack = (undoStack + current).takeLast(4)
        redoStack = emptyList()
    }

    fun cloneOf(b: Bitmap): Bitmap {
        val out = b.copy(Bitmap.Config.ARGB_8888, true) ?: b
        return out
    }

    // ---- commit stroke: baked -> "+ stroke"
    fun commitStroke(strokePath: List<Offset>, isEraser: Boolean, scale: Float) {
        val b = baked ?: return
        pushUndo(b)
        val nb = cloneOf(b)
        val c = Canvas(nb)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = (strokeUi * density) / scale
        }
        if (isEraser) {
            val orig = original
            if (orig != null && orig.width == nb.width && orig.height == nb.height) {
                paint.shader = BitmapShader(orig, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            } else {
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
        } else {
            paint.color = color
        }
        if (strokePath.size > 1) {
            val p = android.graphics.Path()
            p.moveTo(strokePath[0].x, strokePath[0].y)
            strokePath.drop(1).forEach { p.lineTo(it.x, it.y) }
            c.drawPath(p, paint)
        }
        baked = nb
        EditorSession.composite = nb
    }

    fun commitText() {
        val b = baked ?: return
        pushUndo(b)
        val nb = cloneOf(b)
        val c = Canvas(nb)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = color
            textSize = nb.width * (textSizeDraft / 100f)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        c.drawText(textDraft, nb.width / 2f, nb.height / 2f + p.textSize / 3f, p)
        baked = nb
        EditorSession.composite = nb
        showTextDialog = false
    }

    fun applyPreview() {
        val b = baked ?: return
        if (presetIndex == 0 && brightness == 1f && contrast == 1f && saturation == 1f) return
        pushUndo(b)
        val m = snapshotMatrix()
        baked = FilterEngine.apply(b, m)
        presetIndex = 0
        brightness = 1f
        contrast = 1f
        saturation = 1f
        EditorSession.composite = baked
    }

    fun crop(aspect: Float?) {
        val b = baked ?: return
        pushUndo(b)
        val bw = b.width.toFloat()
        val bh = b.height.toFloat()
        val targetW: Int
        val targetH: Int
        val x0: Float
        val y0: Float
        if (aspect == null) { // potong tengah 80%
            targetW = (bw * 0.8f).toInt()
            targetH = (bh * 0.8f).toInt()
            x0 = (bw - targetW) / 2
            y0 = (bh - targetH) / 2
        } else {
            val curA = bw / bh
            if (curA > aspect) { // terlalu lebar
                targetH = bh.toInt()
                targetW = (bh * aspect).toInt()
                x0 = (bw - targetW) / 2
                y0 = 0f
            } else {
                targetW = bw.toInt()
                targetH = (bw / aspect).toInt()
                x0 = 0f
                y0 = (bh - targetH) / 2
            }
        }
        val nb = Bitmap.createBitmap(b, x0.toInt(), y0.toInt(), targetW, targetH)
        original = nb.copy(Bitmap.Config.ARGB_8888, false)
        baked = nb
        EditorSession.composite = nb
    }

    fun doUndo() {
        val cur = baked ?: return
        val last = undoStack.lastOrNull() ?: return
        redoStack = (redoStack + cur).takeLast(4)
        undoStack = undoStack.dropLast(1)
        baked = last
        EditorSession.composite = last
    }

    fun doRedo() {
        val cur = baked ?: return
        val next = redoStack.lastOrNull() ?: return
        undoStack = (undoStack + cur).takeLast(4)
        redoStack = redoStack.dropLast(1)
        baked = next
        EditorSession.composite = next
    }

    Scaffold(
        containerColor = Color(0xFF10141A),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        EditorSession.sourceName.ifEmpty { "Foto Baru" },
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF10141A)),
                actions = {
                    IconButton(
                        onClick = { doUndo() },
                        enabled = undoStack.isNotEmpty(),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, "Undo", tint = Color(0xFFF2F6FA))
                    }
                    IconButton(
                        onClick = { doRedo() },
                        enabled = redoStack.isNotEmpty(),
                    ) {
                        Icon(Icons.Rounded.Redo, "Redo", tint = Color(0xFFF2F6FA))
                    }
                    Button(onClick = {
                        applyPreview()
                        EditorSession.composite = baked
                        nav.navigate("export")
                    }) { Text("Ekspor") }
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            // ---------- kanvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { viewSize = it },
                contentAlignment = Alignment.Center,
            ) {
                if (loading) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
                val b = baked
                if (b != null) {
                    Image(
                        bitmap = b.asImageBitmap(),
                        contentDescription = "Kanvas",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.colorMatrix(previewColors),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .pointerInput(tool, b) {
                                if (tool == Tool.BRUSH || tool == Tool.ERASER) {
                                    detectDragGestures(
                                        onDragStart = { off ->
                                            val bmOff = mapToBitmap(
                                                off, viewSize, b.width, b.height
                                            )
                                            path.clear()
                                            path.add(bmOff)
                                        },
                                        onDrag = { change, _ ->
                                            path.add(
                                                mapToBitmap(
                                                    change.position, viewSize, b.width, b.height
                                                )
                                            )
                                        },
                                        onDragEnd = {
                                            val scale = scaleFor(viewSize, b.width, b.height)
                                            commitStroke(path.toList(), tool == Tool.ERASER, scale)
                                            path.clear()
                                        },
                                    )
                                }
                            },
                    )
                    // pratinjau kuas bergerak
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        if (path.size > 1 && (tool == Tool.BRUSH || tool == Tool.ERASER)) {
                            val bp = Path()
                            bp.moveTo(path[0].x, path[0].y)
                            path.drop(1).forEach { bp.lineTo(it.x, it.y) }
                            drawPath(
                                bp,
                                color = if (tool == Tool.BRUSH) Color(color) else Color.White.copy(alpha = 0.5f),
                                style = Stroke(
                                    width = strokeUi.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                ),
                            )
                        }
                    }
                }
            }

            // ---------- panel penyesuaian
            if (showAdjust) {
                Surface(
                    color = Color(0xFF1A2027),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        AdjustRow("Cahaya", brightness, 0.4f..2f) {
                            brightness = it
                        }
                        AdjustRow("Kontras", contrast, 0.4f..2f) {
                            contrast = it
                        }
                        AdjustRow("Saturasi", saturation, 0f..2.5f) {
                            saturation = it
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = {
                                brightness = 1f; contrast = 1f; saturation = 1f
                            }) { Text("Reset", color = Color(0xFF9BB0C1)) }
                        }
                    }
                }
            }

            // ---------- panel warna + ukuran kuas
            if (showColorPanel) {
                Surface(
                    color = Color(0xFF1A2027),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            brushColors.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(c), CircleShape)
                                        .clickable { color = c },
                                )
                            }
                        }
                        Text(
                            "Ukuran kuas",
                            color = Color(0xFF9BB0C1),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Slider(
                            value = strokeUi,
                            onValueChange = { strokeUi = it },
                            valueRange = 2f..60f,
                        )
                    }
                }
            }

            // ---------- chip filter
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp),
            ) {
                items(FilterEngine.PRESETS.size) { i ->
                    val p = FilterEngine.PRESETS[i]
                    val sel = presetIndex == i
                    Surface(
                        color = if (sel) Color(0xFF6C5CE7) else Color(0xFF242C36),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { presetIndex = i },
                    ) {
                        Text(
                            p.label,
                            color = if (sel) Color.White else Color(0xFF9BB0C1),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            // ---------- toolbar alat
            Surface(
                color = Color(0xFF1A2027),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolButton(Icons.Rounded.Brush, "Kuas", tool == Tool.BRUSH) {
                        tool = Tool.BRUSH
                        showColorPanel = !showColorPanel
                        showAdjust = false
                    }
                    ToolButton(Icons.Rounded.Remove, "Hapus", tool == Tool.ERASER) {
                        tool = Tool.ERASER
                        showColorPanel = false
                        showAdjust = false
                    }
                    ToolButton(Icons.Rounded.TextFields, "Teks", tool == Tool.TEXT) {
                        tool = Tool.TEXT
                        showTextDialog = true
                        showColorPanel = false
                        showAdjust = false
                    }
                    Box {
                        ToolButton(Icons.Rounded.Crop, "Potong", showCropMenu) {
                            showCropMenu = true
                        }
                        DropdownMenu(
                            expanded = showCropMenu,
                            onDismissRequest = { showCropMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("1:1") },
                                onClick = { crop(1f); showCropMenu = false },
                            )
                            DropdownMenuItem(
                                text = { Text("4:5 (IG)") },
                                onClick = { crop(0.8f); showCropMenu = false },
                            )
                            DropdownMenuItem(
                                text = { Text("16:9") },
                                onClick = { crop(16f / 9f); showCropMenu = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Tengah 80%") },
                                onClick = { crop(null); showCropMenu = false },
                            )
                        }
                    }
                    ToolButton(Icons.Rounded.Tune, "Sesuaikan", showAdjust) {
                        showAdjust = !showAdjust
                        showColorPanel = false
                    }
                    ToolButton(Icons.Rounded.Check, "Terapkan", false) {
                        applyPreview()
                    }
                }
            }
        }
    }

    // ---------- dialog teks
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text("Tambah Teks") },
            text = {
                Column {
                    OutlinedTextField(
                        value = textDraft,
                        onValueChange = { textDraft = it },
                        placeholder = { Text("Tulis teks…") },
                        singleLine = true,
                    )
                    Text(
                        "Ukuran teks",
                        color = Color(0xFF9BB0C1),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Slider(
                        value = textSizeDraft,
                        onValueChange = { textSizeDraft = it },
                        valueRange = 2f..20f,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        brushColors.takeLast(6).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(c), CircleShape)
                                    .clickable { color = c },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { commitText() }) { Text("Letakkan") }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun AdjustRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color(0xFFF2F6FA), fontSize = 14.sp, modifier = Modifier.width(80.dp))
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF8B6CF0)),
        )
        Text(
            "%.2f".format(value),
            color = Color(0xFF9BB0C1),
            fontSize = 12.sp,
            modifier = Modifier.width(44.dp),
        )
    }
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Icon(
            icon,
            label,
            tint = if (selected) Color(0xFF8B6CF0) else Color(0xFF9BB0C1),
            modifier = Modifier.size(26.dp),
        )
        Text(
            label,
            color = if (selected) Color(0xFF8B6CF0) else Color(0xFF9BB0C1),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private fun mapToBitmap(
    view: Offset,
    viewSize: IntSize,
    bw: Int,
    bh: Int,
): Offset {
    if (viewSize.width == 0 || viewSize.height == 0) return Offset(0f, 0f)
    val scale = minOf(
        viewSize.width.toFloat() / bw,
        viewSize.height.toFloat() / bh,
    )
    val dx = (viewSize.width - bw * scale) / 2f
    val dy = (viewSize.height - bh * scale) / 2f
    val x = (view.x - dx) / scale
    val y = (view.y - dy) / scale
    return Offset(x.coerceIn(0f, bw - 1f), y.coerceIn(0f, bh - 1f))
}

private fun scaleFor(viewSize: IntSize, bw: Int, bh: Int): Float {
    if (viewSize.width == 0 || viewSize.height == 0) return 1f
    return minOf(
        viewSize.width.toFloat() / bw,
        viewSize.height.toFloat() / bh,
    )
}
