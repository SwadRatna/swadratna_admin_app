package com.swadratna.swadratna_admin.ui.campaign

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.CampaignStatus
import com.swadratna.swadratna_admin.data.model.CampaignType
import com.swadratna.swadratna_admin.data.repository.CampaignRepository
import com.swadratna.swadratna_admin.data.wrapper.Result
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.toMutableList

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class CampaignViewModel @Inject constructor(
    private val repository: CampaignRepository,
    private val sharedPrefsManager: SharedPrefsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    private val _allCampaigns = mutableListOf<Campaign>()

    init { refresh() }

    fun handleEvent(event: CampaignEvent) {
        when (event) {
            is CampaignEvent.SearchQueryChanged ->
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            is CampaignEvent.FilterChanged -> {
                _uiState.value = _uiState.value.copy(filter = event.filter)
                applyFiltersAndSort()
            }
            is CampaignEvent.SortChanged -> {
                _uiState.value = _uiState.value.copy(sortOrder = event.sortOrder)
                applyFiltersAndSort()
            }
            CampaignEvent.RefreshData -> {
                applyFiltersAndSort()
            }
            CampaignEvent.ToggleFilterMenu -> {
                _uiState.value = _uiState.value.copy(
                    isFilterMenuVisible = !_uiState.value.isFilterMenuVisible,
                    isSortMenuVisible = false
                )
            }
            CampaignEvent.ToggleSortMenu -> {
                _uiState.value = _uiState.value.copy(
                    isSortMenuVisible = !_uiState.value.isSortMenuVisible,
                    isFilterMenuVisible = false
                )
            }
            is CampaignEvent.CreateCampaign -> {
                val newCampaign = Campaign(
                    id = UUID.randomUUID().toString(),
                    title = event.title,
                    description = event.description,
                    startDate = event.startDate,
                    endDate = event.endDate,
                    status = CampaignStatus.SCHEDULED,
                    type = CampaignType.DISCOUNT,
                    discount = 15,
                    storeCount = 0,
                    imageUrl = event.imageUrl ?: "https://via.placeholder.com/150"
                )
                
                _allCampaigns.add(0, newCampaign)
                applyFiltersAndSort()
                
                viewModelScope.launch {
                    sharedPrefsManager.saveCampaigns(_allCampaigns)
                }
            }
            is CampaignEvent.EditCampaign -> {
                val campaignToEdit = _allCampaigns.find { it.id == event.campaignId }
                if (campaignToEdit != null) {
                    _uiState.value = _uiState.value.copy(
                        campaignToEdit = campaignToEdit,
                        isEditMode = true
                    )
                }
            }
            is CampaignEvent.DeleteCampaign -> {
                _allCampaigns.removeAll { it.id == event.campaignId }
                applyFiltersAndSort()
                
                viewModelScope.launch {
                    sharedPrefsManager.saveCampaigns(_allCampaigns)
                }
            }
            is CampaignEvent.UpdateCampaign -> {
                val index = _allCampaigns.indexOfFirst { it.id == event.id }
                if (index != -1) {
                    _allCampaigns[index] = _allCampaigns[index].copy(
                        title = event.title,
                        description = event.description,
                        startDate = event.startDate,
                        endDate = event.endDate,
                        type = event.type,
                        discount = event.discount,
                        imageUrl = event.imageUrl,
                        targetFranchises = event.targetFranchises,
                        menuCategories = event.menuCategories
                    )
                }
                
                applyFiltersAndSort()
                
                _uiState.value = _uiState.value.copy(
                    campaignToEdit = null,
                    isEditMode = false
                )
                
                viewModelScope.launch {
                    sharedPrefsManager.saveCampaigns(_allCampaigns)
                }
            }
        }
    }

    private fun refresh() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        when (val res = repository.getCampaigns()) {
            is Result.Success -> _uiState.value = _uiState.value.copy(
                campaigns = res.data, isLoading = false, error = null
            )
            is Result.Error -> _uiState.value = _uiState.value.copy(
                isLoading = false, error = res.message
            )
            is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
        }
    }

    private fun applyFiltersAndSort() {
        val searchQuery = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.filter
        val sortOrder = _uiState.value.sortOrder
        
        // Apply search and filter
        var filteredCampaigns = _allCampaigns.filter { campaign ->
            val matchesSearch = searchQuery.isEmpty() ||
                campaign.title.lowercase().contains(searchQuery) ||
                campaign.description.lowercase().contains(searchQuery)
            
            val matchesFilter = when (filter) {
                CampaignFilter.ALL -> true
                CampaignFilter.ACTIVE -> campaign.status == CampaignStatus.ACTIVE
                CampaignFilter.SCHEDULED -> campaign.status == CampaignStatus.SCHEDULED
                CampaignFilter.COMPLETED -> campaign.status == CampaignStatus.COMPLETED
                CampaignFilter.DRAFT -> campaign.status == CampaignStatus.DRAFT
            }
            
            matchesSearch && matchesFilter
        }
        
        // Apply sorting
        filteredCampaigns = when (sortOrder) {
            CampaignSortOrder.DATE_ASC -> filteredCampaigns.sortedBy { it.startDate }
            CampaignSortOrder.DATE_DESC -> filteredCampaigns.sortedByDescending { it.startDate }
            CampaignSortOrder.TITLE_ASC -> filteredCampaigns.sortedBy { it.title }
            CampaignSortOrder.TITLE_DESC -> filteredCampaigns.sortedByDescending { it.title }
        }
        
        _uiState.value = _uiState.value.copy(campaigns = filteredCampaigns)
    }
}

data class CampaignUiState(
    val searchQuery: String = "",
    val campaigns: List<Campaign> = emptyList(),
    val filter: CampaignFilter = CampaignFilter.ALL,
    val sortOrder: CampaignSortOrder = CampaignSortOrder.DATE_DESC,
    val isLoading: Boolean = false,
    val error: String? = null,
    val campaignToEdit: Campaign? = null,
    val isEditMode: Boolean = false,
    val isFilterMenuVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false
)

enum class CampaignFilter {
    ALL,
    ACTIVE,
    SCHEDULED,
    COMPLETED,
    DRAFT
}

enum class CampaignSortOrder {
    DATE_ASC,
    DATE_DESC,
    TITLE_ASC,
    TITLE_DESC
}

sealed interface CampaignEvent {
    data class SearchQueryChanged(val query: String) : CampaignEvent
    data class FilterChanged(val filter: CampaignFilter) : CampaignEvent
    data class SortChanged(val sortOrder: CampaignSortOrder) : CampaignEvent
    object RefreshData : CampaignEvent
    object ToggleFilterMenu : CampaignEvent
    object ToggleSortMenu : CampaignEvent
    data class CreateCampaign(
        val title: String,
        val description: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val targetFranchises: String,
        val menuCategories: List<String>,
        val imageUrl: String?
    ) : CampaignEvent
    data class EditCampaign(val campaignId: String) : CampaignEvent
    data class DeleteCampaign(val campaignId: String) : CampaignEvent
    data class UpdateCampaign(
        val id: String,
        val title: String,
        val description: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val type: CampaignType,
        val discount: Int,
        val imageUrl: String?,
        val targetFranchises: String,
        val menuCategories: List<String>
    ) : CampaignEvent
}