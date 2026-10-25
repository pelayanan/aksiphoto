package id.aksisoft.aksiphoto.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
data class GalleryImage(
    val uri: String,
    val name: String,
    val dateAdded: Long,
    val mine: Boolean = false,
)

/** I/O gambar: MediaStore loader + penyimpan + share. */
object MediaIO {

    suspend fun loadSampled(context: Context, uri: Uri, maxDim: Int = 1800): Bitmap? =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            try {
                resolver.openInputStream(uri)!!.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
            } catch (_: Exception) {
                return@withContext null
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxDim / 2 &&
                bounds.outHeight / (sample * 2) >= maxDim / 2
            ) sample *= 2
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bmp = try {
                resolver.openInputStream(uri)!!.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
            } catch (_: Exception) {
                null
            } ?: return@withContext null
            applyExif(context, uri, bmp)
        }

    fun galleryImages(context: Context): List<GalleryImage> {
        // Semua foto: tanpa filter relPath (folder AksiPhoto ditampilkan lewat myImages()).
        return queryImages(context, null, false)
    }

    /** File milik AksiPhoto (Pictures/AksiPhoto). */
    fun myImages(context: Context): List<GalleryImage> {
        return queryImages(context, PicturesRel, true)
    }

    val PicturesRel = "Pictures/AksiPhoto"

    private fun queryImages(
        context: Context,
        relativePath: String?,
        mine: Boolean,
    ): List<GalleryImage> {
        val out = ArrayList<GalleryImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
        )
        val selection: String?
        val args: Array<String>?
        if (relativePath != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?"
            args = arrayOf("$relativePath%")
        } else {
            selection = null
            args = null
        }
        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, args,
                MediaStore.Images.Media.DATE_ADDED + " DESC",
            )?.use { c ->
                val iId = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val iName = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val iDate = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    out.add(
                        GalleryImage(
                            uri = Uri.withAppendedPath(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id.toString(),
                            ).toString(),
                            name = c.getString(iName) ?: "",
                            dateAdded = c.getLong(iDate),
                            mine = mine,
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int,
        prefix: String,
        pendingUri: Uri? = null,
    ): Uri? {
        return try {
            val resolver = context.contentResolver
            val mime = when (format) {
                Bitmap.CompressFormat.JPEG -> "image/jpeg"
                Bitmap.CompressFormat.PNG -> "image/png"
                else -> "image/webp"
            }
            var target: Uri? = pendingUri
            if (target == null) {
                val values = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        prefix + "_" + System.currentTimeMillis() + when (format) {
                            Bitmap.CompressFormat.PNG -> ".png"
                            else -> if (format == Bitmap.CompressFormat.WEBP) ".webp" else ".jpg"
                        },
                    )
                    put(MediaStore.Images.Media.MIME_TYPE, mime)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, PicturesRel)
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                target = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
            if (target == null) return null
            val stream = resolver.openOutputStream(target)
            if (stream == null) return null
            stream.use { os: OutputStream -> bitmap.compress(format, quality, os) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && pendingUri == null) {
                val done = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(target, done, null, null)
            }
            target
        } catch (_: Exception) {
            null
        }
    }

    fun shareFile(context: Context, sourceUri: Uri): File? = try {
        val dir = File(context.cacheDir, "photos")
        dir.mkdirs()
        val target = File(dir, "share_" + System.currentTimeMillis() + ".jpg")
        context.contentResolver.openInputStream(sourceUri)!!.use { inp ->
            target.outputStream().use { out -> inp.copyTo(out) }
        }
        target
    } catch (_: Exception) {
        null
    }

    fun createPendingCameraUri(context: Context): Uri? {
        return try {
            val values = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "cam_" + System.currentTimeMillis() + ".jpg",
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, PicturesRel)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (_: Exception) {
            null
        }
    }

    fun finishPending(context: Context, uri: Uri) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, values, null, null)
        } catch (_: Exception) {
        }
    }

    fun compressFormatFor(name: String): Bitmap.CompressFormat = when {
        name.endsWith(".png", true) -> Bitmap.CompressFormat.PNG
        name.endsWith(".webp", true) -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.JPEG
    }

    private fun applyExif(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = try {
            val stream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(stream)
            stream.close()
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } catch (_: Exception) {
            return bitmap
        }
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        val m = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }
}
