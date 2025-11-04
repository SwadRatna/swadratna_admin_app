package com.swadratna.swadratna_admin.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Asset
import com.swadratna.swadratna_admin.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AssetUploadViewModel @Inject constructor(
    private val repository: AssetRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(AssetUploadUiState())
    val uiState: StateFlow<AssetUploadUiState> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = AssetUploadUiState()
    }

    fun setLocalPreview(previewUri: String?) {
        _uiState.value = _uiState.value.copy(localPreviewUri = previewUri)
    }

    fun upload(bytes: ByteArray, fileName: String, mimeType: String, context: String = "store", type: String = "image") {
        _uiState.value = _uiState.value.copy(isUploading = true, error = null)
        viewModelScope.launch {
            repository.uploadAsset(bytes, fileName, mimeType, context, type)
                .onSuccess { asset ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadedAsset = asset,
                        error = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        error = e.message ?: "Failed to upload asset"
                    )
                }
        }
    }


    fun delete() {
        val id = _uiState.value.uploadedAsset?.id ?: return
        _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
        viewModelScope.launch {
            repository.deleteAsset(id)
                .onSuccess { _ ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        uploadedAsset = null,
                        localPreviewUri = null,
                        error = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = e.message ?: "Failed to delete asset"
                    )
                }
        }
    }
}

data class AssetUploadUiState(
    val localPreviewUri: String? = null,
    val uploadedAsset: Asset? = null,
    val isUploading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)