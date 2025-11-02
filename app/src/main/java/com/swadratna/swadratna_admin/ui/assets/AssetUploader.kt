package com.swadratna.swadratna_admin.ui.assets

import android.database.Cursor
import android.net.Uri
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

@Composable
fun AssetUploader(
    modifier: Modifier = Modifier,
    viewModel: AssetUploadViewModel = hiltViewModel(),
    onConfirmed: (Asset) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var mimeType by remember { mutableStateOf("image/*") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedUri = uri
        viewModel.setLocalPreview(previewUri = uri?.toString())
        if (uri != null) {
            val resolver = context.contentResolver
            mimeType = resolver.getType(uri) ?: "image/*"
            fileName = queryDisplayName(resolver, uri) ?: "asset_${System.currentTimeMillis()}"
            // Auto-upload on selection
            val bytes = readBytes(resolver, uri)
            if (bytes != null) {
                viewModel.upload(bytes, fileName, mimeType)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Upload Image (Optional)", style = MaterialTheme.typography.titleMedium)

        // Upload card styled like create campaign screen
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