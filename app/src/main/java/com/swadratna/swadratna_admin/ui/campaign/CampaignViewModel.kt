package com.swadratna.swadratna_admin.ui.campaign

import androidx.lifecycle.ViewModel
import com.swadratna.swadratna_admin.model.Campaign
import com.swadratna.swadratna_admin.model.CampaignStatus
import com.swadratna.swadratna_admin.model.CampaignType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CampaignViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    init {
        // Initialize with mock data
        _uiState.value = CampaignUiState(
            searchQuery = "",
            campaigns = listOf(
                Campaign(
                    id = "1",
                    title = "Diwali Mega Offer",
                    description = "Enjoy 15% off this festive season on all product",
                    startDate = LocalDate.of(2025, 10, 15),
                    endDate = LocalDate.of(2025, 10, 22),
                    status = CampaignStatus.ACTIVE,
                    type = CampaignType.DISCOUNT,
                    discount = 15,
                    storeCount = 12,
                    imageUrl = null
                ),
                Campaign(
                    id = "2",
                    title = "Winter Holiday Deals",
                    description = "Enjoy 20% off on order above 100",
                    startDate = LocalDate.of(2024, 11, 15),
                    endDate = LocalDate.of(2024, 12, 31),
                    status = CampaignStatus.COMPLETED,
                    type = CampaignType.SEASONAL,
                    discount = 20,
                    storeCount = 25,
                    imageUrl = null
                ),
                Campaign(
                    id = "3",
                    title = "Summer Flash Sale",
                    description = "Get up to 30% off on selected items",
                    startDate = LocalDate.of(2025, 5, 1),
                    endDate = LocalDate.of(2025, 5, 15),
                    status = CampaignStatus.SCHEDULED,
                    type = CampaignType.FLASH_SALE,
                    discount = 30,
                    storeCount = 18,
                    imageUrl = null
                )
            )
        )
    }

    fun handleEvent(event: CampaignEvent) {
        when (event) {
            is CampaignEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            }
            is CampaignEvent.FilterChanged -> {
                _uiState.value = _uiState.value.copy(filter = event.filter)
            }
            is CampaignEvent.SortChanged -> {
                _uiState.value = _uiState.value.copy(sortOrder = event.sortOrder)
            }
            CampaignEvent.RefreshData -> {
                // TODO: Implement refresh logic
            }
            is CampaignEvent.CreateCampaign -> {
                // Create a new campaign and add it to the list
                val newCampaign = Campaign(
                    id = UUID.randomUUID().toString(),
                    title = event.title,
                    description = event.description,
                    startDate = event.startDate,
                    endDate = event.endDate,
                    status = CampaignStatus.SCHEDULED,
                    type = CampaignType.DISCOUNT, // Default type, can be updated later
                    discount = 15, // Default discount percentage
                    storeCount = 0, // Will be calculated based on franchises
                    imageUrl = event.imageUrl ?: "https://via.placeholder.com/150"
                )
                
                val updatedCampaigns = _uiState.value.campaigns.toMutableList().apply {
                    add(0, newCampaign) // Add to the beginning of the list
                }
                
                _uiState.value = _uiState.value.copy(campaigns = updatedCampaigns)
            }
        }
    }
}

data class CampaignUiState(
    val searchQuery: String = "",
    val campaigns: List<Campaign> = emptyList(),
    val filter: CampaignFilter = CampaignFilter.ALL,
    val sortOrder: CampaignSortOrder = CampaignSortOrder.DATE_DESC,
    val isLoading: Boolean = false,
    val error: String? = null
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
    data class CreateCampaign(
        val title: String,
        val description: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val targetFranchises: String,
        val menuCategories: List<String>,
        val imageUrl: String?
    ) : CampaignEvent
}