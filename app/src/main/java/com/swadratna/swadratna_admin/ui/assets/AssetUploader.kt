package com.swadratna.swadratna_admin.ui.assets

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.swadratna.swadratna_admin.data.model.Asset
import java.io.ByteArrayOutputStream

@Composable
fun AssetUploader(
    modifier: Modifier = Modifier,
    viewModel: AssetUploadViewModel = hiltViewModel(),
    onConfirmed: (Asset) -> Unit = {},
    context: String = "store",
    type: String = "image"
) {
    val uiState by viewModel.uiState.collectAsState()
    val androidContext = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var mimeType by remember { mutableStateOf("image/*") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedUri = uri
        viewModel.setLocalPreview(previewUri = uri?.toString())
        if (uri != null) {
            val resolver = androidContext.contentResolver
            mimeType = resolver.getType(uri) ?: "image/*"
            fileName = queryDisplayName(resolver, uri) ?: "asset_${System.currentTimeMillis()}"
            // Auto-upload on selection with compression to avoid HTTP 413
            val compressed = compressImage(resolver, uri)
            if (compressed != null) {
                val (bytes, compressedMime) = compressed
                mimeType = compressedMime
                fileName = ensureJpegExtension(fileName)
                viewModel.upload(bytes, fileName, mimeType, context, type)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Upload Image (Optional)", style = MaterialTheme.typography.titleMedium)

        val shape = RoundedCornerShape(8.dp)
        val preview = uiState.uploadedAsset?.cdnUrl ?: uiState.uploadedAsset?.url ?: uiState.localPreviewUri
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape)
                .clip(shape)
                .clickable { picker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (!preview.isNullOrBlank()) {
                AsyncImage(
                    model = preview,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(32.dp))
                    Text(text = "Tap to upload the offer image.", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (uiState.isUploading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Status/Error
        uiState.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        // Callback after successful upload (no confirm step)
        LaunchedEffect(uiState.uploadedAsset?.id) {
            val asset = uiState.uploadedAsset
            if (asset != null) {
                onConfirmed(asset)
            }
        }
    }
}

private const val MAX_UPLOAD_BYTES = 1_000_000 // 1MB cap to avoid 413
private const val MAX_DIMENSION = 1280 // Max width/height for uploaded images

private fun compressImage(resolver: android.content.ContentResolver, uri: Uri): Pair<ByteArray, String>? {
    return try {
        val sourceBitmap: Bitmap? = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri))
        } else {
            resolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input) }
        }
        val bitmap = sourceBitmap ?: return null
        var currentBitmap = bitmap

        // Initial downscale if needed
        fun scaleToMaxDimension(bmp: Bitmap, maxDim: Int): Bitmap {
            val w = bmp.width
            val h = bmp.height
            val maxSide = maxOf(w, h)
            val scale = if (maxSide > maxDim) maxDim.toFloat() / maxSide.toFloat() else 1f
            val targetW = (w * scale).toInt()
            val targetH = (h * scale).toInt()
            return if (scale < 1f) Bitmap.createScaledBitmap(bmp, targetW, targetH, true) else bmp
        }

        currentBitmap = scaleToMaxDimension(currentBitmap, MAX_DIMENSION)

        // Try quality compression first
        var quality = 85
        var outBytes: ByteArray
        fun compressWithQuality(bmp: Bitmap, q: Int): ByteArray {
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, q, baos)
            return baos.toByteArray()
        }
        do {
            outBytes = compressWithQuality(currentBitmap, quality)
            quality -= 10
        } while (outBytes.size > MAX_UPLOAD_BYTES && quality >= 50)

        // If still too big, progressively downscale dimensions and re-compress
        var maxDim = MAX_DIMENSION
        while (outBytes.size > MAX_UPLOAD_BYTES && maxDim > 480) {
            maxDim = (maxDim * 0.8).toInt()
            currentBitmap = scaleToMaxDimension(currentBitmap, maxDim)
            quality = 80
            do {
                outBytes = compressWithQuality(currentBitmap, quality)
                quality -= 10
            } while (outBytes.size > MAX_UPLOAD_BYTES && quality >= 50)
        }

        Pair(outBytes, "image/jpeg")
    } catch (e: Exception) {
        null
    }
}

private fun ensureJpegExtension(fileName: String): String {
    val base = fileName.substringBeforeLast('.')
    return "$base.jpg"
}

private fun queryDisplayName(resolver: android.content.ContentResolver, uri: Uri): String? {
    var name: String? = null
    val cursor: Cursor? = resolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) name = it.getString(index)
        }
    }
    return name
}

private fun readBytes(resolver: android.content.ContentResolver, uri: Uri): ByteArray? {
    return try {
        resolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }
}