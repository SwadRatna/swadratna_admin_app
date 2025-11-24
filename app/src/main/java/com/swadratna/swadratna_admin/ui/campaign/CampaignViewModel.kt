package com.swadratna.swadratna_admin.ui.campaign

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.CampaignStatus
import com.swadratna.swadratna_admin.data.model.CampaignType
import com.swadratna.swadratna_admin.data.repository.CampaignRepository
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.model.ActivityType
import com.swadratna.swadratna_admin.data.wrapper.Result
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import com.swadratna.swadratna_admin.data.remote.api.AdminCreateCampaignRequest
import com.swadratna.swadratna_admin.data.remote.api.AdminCampaignResponse
import com.swadratna.swadratna_admin.data.remote.api.AdminUpdateCampaignRequest

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class CampaignViewModel @Inject constructor(
    private val repository: CampaignRepository,
    private val sharedPrefsManager: SharedPrefsManager,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    private val _allCampaigns = mutableListOf<Campaign>()

    private var startupAutoCompleteDone = false

    init { refresh() }

    fun handleEvent(event: CampaignEvent) {
        when (event) {
            is CampaignEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
                applyFiltersAndSort()
            }
            is CampaignEvent.FilterChanged -> {
                _uiState.value = _uiState.value.copy(filter = event.filter)
                refresh()
            }
            is CampaignEvent.SortChanged -> {
                _uiState.value = _uiState.value.copy(sortOrder = event.sortOrder)
                applyFiltersAndSort()
            }
            CampaignEvent.RefreshData -> {
                refresh()
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
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                viewModelScope.launch {
                    val req = AdminCreateCampaignRequest(
                        title = event.title,
                        description = event.description,
                        type = "promotion",
                        startDate = event.startDate.toString(),
                        endDate = event.endDate.toString(),
                        targetFranchises = event.targetFranchiseIds,
                        targetCategories = event.targetCategoryIds,
                        imageUrl = event.imageUrl,
                        bannerImageUrl = null,
                        discountType = null,
                        discountValue = null,
                        minOrderAmount = null,
                        maxDiscountAmount = null,
                        promoCode = null,
                        promoCodeLimit = null,
                        priority = null,
                        termsConditions = null,
                        youtubeVideoUrl = event.youtubeVideoUrl
                    )
                    when (val res = repository.adminCreateCampaign(req)) {
                        is Result.Success -> {
                            val created = mapAdminCampaign(res.data)
                            _allCampaigns.add(0, created)
                            applyFiltersAndSort()
                            _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                            sharedPrefsManager.saveCampaigns(_allCampaigns)
                            activityRepository.addActivity(
                                ActivityType.CAMPAIGN_CREATED,
                                "Campaign created",
                                "Campaign '${created.title}' has been created"
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                        }
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
            }
            is CampaignEvent.LoadCampaigns -> {
                refresh()
            }
            is CampaignEvent.EditCampaign -> {
                val local = _allCampaigns.find { it.id == event.campaignId }
                android.util.Log.d("CampaignViewModel", "Local cached campaign: ${local?.id}, youtubeVideoUrl: ${local?.youtubeVideoUrl}")
                _uiState.value = _uiState.value.copy(
                    campaignToEdit = local,
                    isEditMode = true,
                    error = null
                )
                val idLong = event.campaignId.toLongOrNull()
                if (idLong != null) {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    viewModelScope.launch {
                        when (val res = repository.adminGetCampaignDetails(idLong)) {
                            is Result.Success -> {
                                val fresh = mapAdminCampaign(res.data)
                                android.util.Log.d("CampaignViewModel", "Mapped campaign data: ${fresh.id}, youtubeVideoUrl: ${fresh.youtubeVideoUrl}")
                                android.util.Log.d("CampaignViewModel", "Raw response data: youtubeVideoUrl = ${res.data.youtubeVideoUrl}")
                                _uiState.value = _uiState.value.copy(
                                    campaignToEdit = fresh,
                                    isLoading = false
                                )
                                val idx = _allCampaigns.indexOfFirst { it.id == event.campaignId }
                                if (idx != -1) {
                                    _allCampaigns[idx] = fresh
                                    applyFiltersAndSort()
                                }
                            }
                            is Result.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = res.message
                                )
                            }
                            is Result.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                    }
                }
            }
            is CampaignEvent.DeleteCampaign -> {
                val idLong = event.campaignId.toLongOrNull()
                if (idLong == null) {
                    _uiState.value = _uiState.value.copy(error = "Invalid campaign id")
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    viewModelScope.launch {
                        when (val res = repository.adminDeleteCampaign(idLong)) {
                            is Result.Success -> {
                                _allCampaigns.removeAll { it.id == event.campaignId }
                                applyFiltersAndSort()
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                sharedPrefsManager.saveCampaigns(_allCampaigns)
                            }
                            is Result.Error -> {
                                _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                            }
                            is Result.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                    }
                }
            }
            is CampaignEvent.UpdateCampaign -> {
                val idLong = event.id.toLongOrNull()
                if (idLong == null) {
                    _uiState.value = _uiState.value.copy(error = "Invalid campaign id")
                } else {
                    val original = _allCampaigns.find { it.id == event.id }
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    val req = AdminUpdateCampaignRequest(
                        title = event.title,
                        description = event.description,
                        type = toCampaignTypeString(event.type),
                        startDate = event.startDate.toString(),
                        endDate = event.endDate.toString(),
                        targetFranchises = event.targetFranchiseIds,
                        targetCategories = event.targetCategoryIds,
                        imageUrl = event.imageUrl,
                        discountValue = event.discount,
                        youtubeVideoUrl = event.youtubeVideoUrl
                    )
                    viewModelScope.launch {
                        when (val res = repository.adminUpdateCampaign(idLong, req)) {
                            is Result.Success -> {
                                val updated = mapAdminCampaign(res.data)
                                val index = _allCampaigns.indexOfFirst { it.id == event.id }
                                if (index != -1) {
                                    _allCampaigns[index] = updated
                                } else {
                                    _allCampaigns.add(0, updated)
                                }
                                applyFiltersAndSort()
                                _uiState.value = _uiState.value.copy(
                                    campaignToEdit = null,
                                    isEditMode = false,
                                    isLoading = false,
                                    error = null
                                )
                                sharedPrefsManager.saveCampaigns(_allCampaigns)
                                activityRepository.addActivity(
                                    ActivityType.CAMPAIGN_UPDATED,
                                    "Campaign updated",
                                    buildCampaignUpdateDescription(original, updated)
                                )
                            }
                            is Result.Error -> {
                                _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                            }
                            is Result.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                    }
                }
            }
            is CampaignEvent.UpdateCampaignStatus -> {
                val idLong = event.campaignId.toLongOrNull()
                if (idLong == null) {
                    _uiState.value = _uiState.value.copy(error = "Invalid campaign id")
                } else {
                    val prevIndex = _allCampaigns.indexOfFirst { it.id == event.campaignId }
                    val prevListStatus = prevIndex.takeIf { it != -1 }?.let { _allCampaigns[it].status }
                    val prevEdit = _uiState.value.campaignToEdit

                    if (prevIndex != -1) {
                        _allCampaigns[prevIndex] = _allCampaigns[prevIndex].copy(status = event.status)
                    }
                    if (prevEdit?.id == event.campaignId) {
                        _uiState.value = _uiState.value.copy(campaignToEdit = prevEdit.copy(status = event.status))
                    }
                    applyFiltersAndSort()
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                    viewModelScope.launch {
                        when (val res = repository.adminUpdateCampaignStatus(idLong, toCampaignStatusString(event.status))) {
                            is Result.Success -> {
                                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                                sharedPrefsManager.saveCampaigns(_allCampaigns)
                            }
                            is Result.Error -> {
                                if (prevIndex != -1 && prevListStatus != null) {
                                    _allCampaigns[prevIndex] = _allCampaigns[prevIndex].copy(status = prevListStatus)
                                }
                                if (prevEdit?.id == event.campaignId) {
                                    _uiState.value = _uiState.value.copy(campaignToEdit = prevEdit)
                                }
                                applyFiltersAndSort()
                                _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                            }
                            is Result.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildCampaignUpdateDescription(original: Campaign?, updated: Campaign): String {
        val name = updated.title
        if (original == null) return "Campaign '$name' updated"
        val changes = mutableListOf<String>()
        if (original.title != updated.title) changes.add("title")
        if (original.description != updated.description) changes.add("description")
        if (original.type != updated.type) changes.add("type")
        if (original.startDate != updated.startDate || original.endDate != updated.endDate) changes.add("schedule")
        if (original.targetFranchiseIds != updated.targetFranchiseIds) changes.add("target stores")
        if (original.targetCategoryIds != updated.targetCategoryIds) changes.add("target categories")
        val oldImg = (original.imageUrl ?: "").trim()
        val newImg = (updated.imageUrl ?: "").trim()
        if (oldImg != newImg) changes.add("image")
        if (original.discount != updated.discount) changes.add("discount")
        val oldVideo = (original.youtubeVideoUrl ?: "").trim()
        val newVideo = (updated.youtubeVideoUrl ?: "").trim()
        if (oldVideo != newVideo) changes.add("video")
        return when (changes.size) {
            0 -> "Campaign '$name' updated"
            1 -> "Campaign '$name': ${changes[0]} has been updated"
            else -> "Campaign '$name': updated ${changes.joinToString(", ")}"
        }
    }

    fun autoCompleteExpiredCampaignsIfNeeded() {
        if (startupAutoCompleteDone) return
        startupAutoCompleteDone = true
        viewModelScope.launch {
            when (val res = repository.adminListCampaigns(status = null, type = null, search = null, page = null, limit = 1000)) {
                is Result.Success -> {
                    val now = LocalDate.now()
                    val expired = (res.data.campaigns ?: emptyList()).filter { r ->
                        val end = r.endDate?.let { parseServerDate(it) } ?: now
                        val status = r.status?.lowercase()
                        (end.isBefore(now) || end.isEqual(now)) && status != "completed"
                    }
                    expired.forEach { r ->
                        val id = r.id ?: return@forEach
                        repository.adminUpdateCampaignStatus(id, "completed")
                        val idx = _allCampaigns.indexOfFirst { it.id == id.toString() }
                        if (idx != -1) {
                            _allCampaigns[idx] = _allCampaigns[idx].copy(status = CampaignStatus.COMPLETED)
                        }
                    }
                    applyFiltersAndSort()
                    sharedPrefsManager.saveCampaigns(_allCampaigns)
                }
                is Result.Error -> {
                }
                is Result.Loading -> {
                }
            }
        }
    }

    private fun refresh() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val statusParam = when (_uiState.value.filter) {
            CampaignFilter.ALL -> null
            CampaignFilter.ACTIVE -> "active"
            CampaignFilter.SCHEDULED -> "scheduled"
            CampaignFilter.COMPLETED -> "completed"
            CampaignFilter.DRAFT -> null
        }
        val searchParam = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
        when (val res = repository.adminListCampaigns(status = statusParam, type = null, search = searchParam, page = null, limit = null)) {
            is Result.Success -> {
                val mapped = (res.data.campaigns ?: emptyList()).map { mapAdminCampaign(it) }
                _allCampaigns.clear()
                _allCampaigns.addAll(mapped)
                applyFiltersAndSort()
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            }
            is Result.Error -> _uiState.value = _uiState.value.copy(
                isLoading = false, error = res.message
            )
            is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
        }
    }

    private fun mapAdminCampaign(r: AdminCampaignResponse): Campaign {
        return Campaign(
            id = r.id?.toString() ?: "",
            title = r.title ?: "Untitled",
            description = r.description ?: "",
            startDate = r.startDate?.let { parseServerDate(it) } ?: LocalDate.now(),
            endDate = r.endDate?.let { parseServerDate(it) } ?: LocalDate.now(),
            status = when (r.status?.lowercase()) {
                "active" -> CampaignStatus.ACTIVE
                "completed" -> CampaignStatus.COMPLETED
                "scheduled" -> CampaignStatus.SCHEDULED
                "draft" -> CampaignStatus.DRAFT
                else -> CampaignStatus.SCHEDULED
            },
            type = when (r.type?.lowercase()) {
                "discount" -> CampaignType.DISCOUNT
                "bogo" -> CampaignType.BOGO
                "flash_sale" -> CampaignType.FLASH_SALE
                "seasonal" -> CampaignType.SEASONAL
                "promotion" -> CampaignType.SPECIAL_OFFER
                else -> CampaignType.SPECIAL_OFFER
            },
            discount = r.discountValue ?: 0,
            storeCount = r.targetFranchises?.size ?: 0,
            imageUrl = r.imageUrl?.trim()?.trim('`'),
            targetFranchiseIds = r.targetFranchises ?: emptyList(),
            targetCategoryIds = r.targetCategories ?: emptyList(),
            youtubeVideoUrl = r.youtubeVideoUrl?.trim()?.trim('`')
        )
    }

    private fun toCampaignTypeString(type: CampaignType): String = when (type) {
        CampaignType.DISCOUNT -> "discount"
        CampaignType.BOGO -> "bogo"
        CampaignType.SPECIAL_OFFER -> "promotion"
        CampaignType.SEASONAL -> "seasonal"
        CampaignType.FLASH_SALE -> "flash_sale"
    }

    private fun toCampaignStatusString(status: CampaignStatus): String = when (status) {
        CampaignStatus.ACTIVE -> "active"
        CampaignStatus.SCHEDULED -> "scheduled"
        CampaignStatus.COMPLETED -> "completed"
        CampaignStatus.DRAFT -> "draft"
    }

    private fun applyFiltersAndSort() {
        val searchQuery = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.filter
        val sortOrder = _uiState.value.sortOrder
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
    object LoadCampaigns : CampaignEvent
    data class CreateCampaign(
        val title: String,
        val description: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val targetFranchiseIds: List<Int>,
        val targetCategoryIds: List<Int>,
        val imageUrl: String?,
        val youtubeVideoUrl: String?
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
        val targetFranchiseIds: List<Int>,
        val targetCategoryIds: List<Int>,
        val youtubeVideoUrl: String?
    ) : CampaignEvent
    data class UpdateCampaignStatus(val campaignId: String, val status: CampaignStatus) : CampaignEvent
}

private fun parseServerDate(dateStr: String): LocalDate {
    return runCatching { OffsetDateTime.parse(dateStr).toLocalDate() }
        .getOrElse {
            runCatching { LocalDate.parse(dateStr.take(10)) }
                .getOrElse { LocalDate.now() }
        }
}
