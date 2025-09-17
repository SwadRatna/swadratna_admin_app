package com.swadratna.swadratna_admin.ui.campaign

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.CampaignType
import com.swadratna.swadratna_admin.data.remote.api.CreateCampaignRequest
import com.swadratna.swadratna_admin.data.repository.CampaignRepository
import com.swadratna.swadratna_admin.data.wrapper.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.collections.toMutableList

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class CampaignViewModel @Inject constructor(
    private val repository: CampaignRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun handleEvent(event: CampaignEvent) {
        when (event) {
            is CampaignEvent.SearchQueryChanged ->
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            is CampaignEvent.FilterChanged ->
                _uiState.value = _uiState.value.copy(filter = event.filter)
            is CampaignEvent.SortChanged ->
                _uiState.value = _uiState.value.copy(sortOrder = event.sortOrder)
            CampaignEvent.RefreshData -> refresh()
            is CampaignEvent.CreateCampaign -> createCampaign(event)
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

    private fun createCampaign(event: CampaignEvent.CreateCampaign) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        val req = CreateCampaignRequest(
            title = event.title,
            description = event.description,
            startDate = event.startDate.toString(),
            endDate = event.endDate.toString(),
            type = CampaignType.DISCOUNT.name,
            discount = 15,
            targetFranchises = event.targetFranchises,
            menuCategories = event.menuCategories,
            imageUrl = event.imageUrl
        )

        when (val res = repository.createCampaign(req)) {
            is Result.Success -> {
                val updated = _uiState.value.campaigns.toMutableList().apply { add(0, res.data) }
                _uiState.value = _uiState.value.copy(campaigns = updated, isLoading = false, error = null)
            }
            is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
            is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
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