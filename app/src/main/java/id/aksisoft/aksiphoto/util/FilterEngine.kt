package id.aksisoft.aksiphoto.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

data class FilterPreset(val id: String, val label: String)

object FilterEngine {

    val PRESETS: List<FilterPreset> = listOf(
        FilterPreset("original", "Original"),
        FilterPreset("bw", "B&W"),
        FilterPreset("bwcontrast", "B&W Kontras"),
        FilterPreset("sepia", "Sepia"),
        FilterPreset("vintage", "Vintage"),
        FilterPreset("fade", "Fade"),
        FilterPreset("invert", "Invert"),
        FilterPreset("warm", "Hangat"),
        FilterPreset("cold", "Dingin"),
        FilterPreset("cine", "Cinematic"),
        FilterPreset("sunset", "Sunset"),
        FilterPreset("cyber", "Cyberpunk"),
    )

    fun presetById(id: String): Int {
        val i = PRESETS.indexOfFirst { it.id == id }
        return if (i < 0) 0 else i
    }

    /** Combined ColorMatrix: preset lalu penyesuaian (semua 1.0 = netral). */
    fun combinedMatrix(
        presetIndex: Int,
        brightness: Float,   // 1.0 = netral, 0..2
        contrast: Float,     // 1.0 = netral
        saturation: Float,   // 1.0 = netral
    ): ColorMatrix {
        val preset = ColorMatrix(presetMatrix(PRESETS.getOrNull(presetIndex)?.id ?: "original"))
        val adjust = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, ((brightness - 1f) * 40f),
                0f, contrast, 0f, 0f, ((brightness - 1f) * 40f),
                0f, 0f, contrast, 0f, ((brightness - 1f) * 40f),
                0f, 0f, 0f, 1f, 0f,
            )
        )
        val sat = ColorMatrix()
        sat.setSaturation(saturation)
        adjust.postConcat(sat)
        preset.postConcat(adjust)
        return preset
    }

    private fun presetMatrix(id: String): FloatArray {
        return when (id) {
            "bw" -> floatArrayOf(
                0.3f, 0.59f, 0.11f, 0f, 0f,
                0.3f, 0.59f, 0.11f, 0f, 0f,
                0.3f, 0.59f, 0.11f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )
            "bwcontrast" -> floatArrayOf(
                0.36f, 0.71f, 0.13f, 0f, -12f,
                0.36f, 0.71f, 0.13f, 0f, -12f,
                0.36f, 0.71f, 0.13f, 0f, -12f,
                0f, 0f, 0f, 1f, 0f,
            )
            "sepia" -> floatArrayOf(
                0.39f, 0.77f, 0.19f, 0f, 22f,
                0.35f, 0.69f, 0.17f, 0f, 12f,
                0.27f, 0.53f, 0.13f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )
            "vintage" -> floatArrayOf(
                0.9f, 0.08f, 0.02f, 0f, 18f,
                0.11f, 0.78f, 0.06f, 0f, 10f,
                0.12f, 0.13f, 0.72f, 0f, 18f,
                0f, 0f, 0f, 1f, 0f,
            )
            "fade" -> floatArrayOf(
                0.84f, 0.05f, 0.04f, 0f, 28f,
                0.06f, 0.8f, 0.05f, 0f, 26f,
                0.06f, 0.06f, 0.78f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f,
            )
            "invert" -> floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f,
            )
            "warm" -> floatArrayOf(
                1.08f, 0.03f, 0f, 0f, 8f,
                1f, 1f, 0f, 0f, 4f,
                0.92f, 0f, 0.98f, 0f, -6f,
                0f, 0f, 0f, 1f, 0f,
            )
            "cold" -> floatArrayOf(
                0.94f, 0f, 0.06f, 0f, -2f,
                0f, 1f, 0.02f, 0f, 2f,
                0f, 0.04f, 1.1f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f,
            )
            "cine" -> floatArrayOf(
                0.84f, 0.08f, 0.02f, 0f, 6f,
                0.06f, 0.88f, 0.02f, 0f, 8f,
                0.1f, 0.1f, 0.86f, 0f, 4f,
                0f, 0f, 0f, 1f, 0f,
            )
            "sunset" -> floatArrayOf(
                1.12f, 0.06f, 0f, 0f, 10f,
                0.9f, 1f, 0f, 0f, 6f,
                0.5f, 0f, 0.86f, 0f, -8f,
                0f, 0f, 0f, 1f, 0f,
            )
            "cyber" -> floatArrayOf(
                1.05f, 0f, 0.12f, 0f, 2f,
                0.2f, 0.9f, 0.1f, 0f, -2f,
                0.05f, 0.08f, 1.2f, 0f, 16f,
                0f, 0f, 0f, 1f, 0f,
            )
            else -> floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )
        }
    }

    /** Terapkan pipeline ke bitmap baru. */
    fun apply(
        source: Bitmap,
        matrix: ColorMatrix,
    ): Bitmap {
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(out).drawBitmap(source, 0f, 0f, paint)
        return out
    }
}

/** State lintas-layar antara Editor dan Export. */
object EditorSession {
    var sourceUri: String? = null
    var composite: Bitmap? = null
    var sourceName: String = ""

    fun reset() {
        sourceUri = null
        composite?.recycleIfLarge()
        composite = null
    }

    private fun Bitmap.recycleIfLarge() {
        if (width * height > 400_000) this.recycle()
    }
}
