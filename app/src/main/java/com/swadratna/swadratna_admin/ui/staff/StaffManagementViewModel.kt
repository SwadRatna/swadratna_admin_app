package com.swadratna.swadratna_admin.ui.staff

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType
import com.swadratna.swadratna_admin.data.model.CreateStaffRequest
import com.swadratna.swadratna_admin.data.model.ShiftTiming
import com.swadratna.swadratna_admin.data.model.Staff
import com.swadratna.swadratna_admin.data.model.StaffStatus
import com.swadratna.swadratna_admin.data.model.UpdateStaffRequest
import com.swadratna.swadratna_admin.data.model.WorkingHours
import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.repository.StaffRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

import com.swadratna.swadratna_admin.utils.ApiConstants

private data class PendingStaffImage(val name: String, val email: String, val phone: String, val imageUrl: String)

@HiltViewModel
class StaffManagementViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val activityRepository: ActivityRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState.asStateFlow()
    
    private val _allStaff = mutableListOf<Staff>()
    private val pendingImages = mutableListOf<PendingStaffImage>()
    
    fun loadStores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStores = true, storesError = null) }
            try {
                val result = storeRepository.getStores(page = 1, limit = 100, restaurantId = 1000007)
                result.onSuccess { response ->
                    val stores = response.stores ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            stores = stores,
                            isLoadingStores = false,
                            storesError = null
                        )
                    }
                }
                result.onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            stores = emptyList(),
                            isLoadingStores = false,
                            storesError = exception.message ?: "Failed to load stores"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        stores = emptyList(),
                        isLoadingStores = false,
                        storesError = e.message ?: "Failed to load stores"
                    )
                }
            }
        }
    }

    fun loadStaff(storeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            staffRepository.getStaff(storeId)
                .onSuccess { response ->
                    _allStaff.clear()
                    response.staff?.let { staffList ->
                        _allStaff.addAll(staffList)
                        // Attach any pending uploaded image URLs to matched staff entries
                        if (pendingImages.isNotEmpty()) {
                            val newMappings = mutableMapOf<Int, String>()
                            val remaining = mutableListOf<PendingStaffImage>()
                            for (p in pendingImages) {
                                val match = _allStaff.find { s ->
                                    val nameMatches = (s.name ?: "") == p.name
                                    val emailMatches = s.email?.equals(p.email, ignoreCase = true) == true
                                    val phoneMatches = s.phone == p.phone || s.mobileNumber == p.phone
                                    nameMatches && (emailMatches || phoneMatches)
                                }
                                if (match != null) {
                                    newMappings[match.id] = p.imageUrl
                                } else {
                                    remaining.add(p)
                                }
                            }
                            pendingImages.clear()
                            pendingImages.addAll(remaining)
                            if (newMappings.isNotEmpty()) {
                                _uiState.update { it.copy(imagesByStaffId = it.imagesByStaffId + newMappings) }
                            }
                        }
                        // Capture any passwords provided by backend in staff list
                        val passwordsFromList = staffList.mapNotNull { s ->
                            val pwd = s.password
                            if (!pwd.isNullOrBlank()) s.id to pwd else null
                        }.toMap()
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                staffList = _allStaff,
                                error = null,
                                passwordsByStaffId = it.passwordsByStaffId + passwordsFromList
                            )
                        }
                    } ?: run {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                staffList = emptyList(),
                                error = null
                            )
                        }
                    }
                    applyFiltersAndSort()
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load staff"
                        )
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun createStaff(
        name: String,
        email: String,
        phone: String,
        address: String,
        role: String,
        salary: Double,
        joinDate: String,
        startTime: String,
        endTime: String,
        status: String,
        imageUrl: String?,
        storeId: Int?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val sanitizedImage = sanitizeImageUrl(imageUrl)
            val startTimeSanitized = sanitizeTime(startTime)
            val endTimeSanitized = sanitizeTime(endTime)
            val request = CreateStaffRequest(
                address = sanitizeText(address),
                email = sanitizeText(email),
                joinDate = sanitizeDate(joinDate),
                name = sanitizeText(name),
                phone = sanitizePhoneNumber(phone),
                role = sanitizeText(role),
                salary = salary,
                shiftTiming = ShiftTiming(startTimeSanitized, endTimeSanitized),
                imageUrl = sanitizedImage,
                status = sanitizeText(status),
                storeId = storeId ?: 0 // Use 0 as default for "General" staff
            )
            
            staffRepository.createStaff(request)
                .onSuccess { response ->
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STAFF_CREATED,
                        "New staff member added",
                        "Staff member '$name' has been successfully added with role '$role'"
                    )
                    
                    Log.d("StaffManagementVM", "CreateStaff success: message='${response.message}'")
                    val extractedPassword = response.password ?: extractPasswordFromMessage(response.message)
                    val staffIdFromResponse = response.staff?.id
                    if (!extractedPassword.isNullOrBlank() && staffIdFromResponse != null) {
                        _uiState.update { it.copy(passwordsByStaffId = it.passwordsByStaffId + (staffIdFromResponse to extractedPassword)) }
                    }
                    // Cache uploaded image URL locally for display in list if backend doesn't return it
                    if (!sanitizedImage.isNullOrBlank() && staffIdFromResponse != null) {
                        val normalized = normalizeUrl(sanitizedImage) ?: sanitizedImage
                        _uiState.update { it.copy(imagesByStaffId = it.imagesByStaffId + (staffIdFromResponse to normalized)) }
                    }
                    // If backend does not return the created staff id, store pending match to attach after next load
                    if (staffIdFromResponse == null && !sanitizedImage.isNullOrBlank()) {
                        pendingImages.add(PendingStaffImage(name = name, email = email, phone = phone, imageUrl = sanitizedImage))
                    }
                    if (extractedPassword != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                generatedPassword = extractedPassword,
                                isPasswordDialogVisible = true
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    storeId?.let { loadStaff(it) }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create staff"
                        )
                    }
                }
        }
    }

    // Helper to extract password from a free-form message string
    private fun extractPasswordFromMessage(message: String?): String? {
        if (message.isNullOrBlank()) return null
        // Common patterns: "password is <pwd>", "password: <pwd>", "Password=<pwd>"
        val patterns = listOf(
            Regex("(?i)password\\s*(?:is|:|=)\\s*([\\S]+)"), // captures non-whitespace token after indicator
            Regex("(?i)pass\\s*(?:is|:|=)\\s*([\\S]+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(message)
            val pwd = match?.groups?.get(1)?.value
            if (!pwd.isNullOrBlank()) return pwd.trim()
        }
        // Try to find quoted password if present
        val quoted = Regex("(?i)password[^\"]*\"([^\"]+)\"").find(message)?.groups?.get(1)?.value
        return quoted?.trim()
    }

    fun dismissPasswordDialog() {
        _uiState.update { it.copy(isPasswordDialogVisible = false, generatedPassword = null) }
    }
    
    private fun buildStaffUpdateDescription(original: Staff?, request: UpdateStaffRequest): String {
        val name = request.name
        if (original == null) return "Staff member '$name' updated"
        val changes = mutableListOf<String>()
        if ((original.name ?: "").trim() != request.name.trim()) changes.add("name")
        val oldEmail = (original.email ?: "").trim()
        if (oldEmail != request.email.trim()) changes.add("email")
        val oldPhoneUnified = (original.phone ?: original.mobileNumber ?: "").trim()
        val newPhoneUnified = request.phone.trim()
        if (oldPhoneUnified != newPhoneUnified) changes.add("phone")
        val oldAddress = (original.address ?: "").trim()
        if (oldAddress != request.address.trim()) changes.add("address")
        val oldRole = (original.position ?: "").trim()
        if (oldRole != request.role.trim()) changes.add("role")
        val oldSalary = original.salary
        if (oldSalary == null || oldSalary != request.salary) changes.add("salary")
        val oldJoin = (original.joinDate ?: "").trim()
        val newJoin = request.joinDate.trim()
        if (oldJoin != newJoin) changes.add("join date")
        val oldStart = ((original.workingHours?.startTime ?: original.shiftTiming?.startTime) ?: "").trim()
        val oldEnd = ((original.workingHours?.endTime ?: original.shiftTiming?.endTime) ?: "").trim()
        val newStart = request.shiftTiming.startTime.trim()
        val newEnd = request.shiftTiming.endTime.trim()
        if (oldStart != newStart || oldEnd != newEnd) changes.add("shift timing")
        val oldStatus = original.status.name.lowercase()
        val newStatus = request.status.lowercase()
        if (oldStatus != newStatus) changes.add("status")
        val oldStore = original.storeId
        if ((oldStore ?: 0) != request.storeId) changes.add("store")
        val oldImage = (original.imageUrl ?: "").trim()
        val newImage = (request.imageUrl ?: "").trim()
        if (oldImage != newImage) changes.add("image")
        return when (changes.size) {
            0 -> "Staff member '$name' updated"
            1 -> "Staff member '$name': ${changes[0]} has been updated"
            else -> "Staff member '$name': updated ${changes.joinToString(", ")}"
        }
    }
    
    fun updateStaff(
        staffId: Int,
        name: String,
        email: String,
        phone: String,
        mobileNumber: String,
        address: String,
        role: String,
        salary: Double,
        joinDate: String,
        startTime: String,
        endTime: String,
        status: String,
        imageUrl: String? = null,
        password: String? = null,
        storeId: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Capture the original store ID before update
            val originalStaff = _allStaff.find { it.id == staffId }
            val originalStoreId = originalStaff?.storeId
            
            val sanitizedImage = sanitizeImageUrl(imageUrl)
            val startTimeSanitized = sanitizeTime(startTime)
            val endTimeSanitized = sanitizeTime(endTime)
            val request = UpdateStaffRequest(
                address = sanitizeText(address),
                email = sanitizeText(email),
                joinDate = sanitizeDate(joinDate),
                name = sanitizeText(name),
                phone = sanitizePhoneNumber(phone),
                mobileNumber = sanitizePhoneNumber(mobileNumber),
                role = sanitizeText(role),
                salary = salary,
                shiftTiming = ShiftTiming(startTimeSanitized, endTimeSanitized),
                imageUrl = sanitizedImage,
                status = sanitizeText(status),
                password = password,
                storeId = storeId ?: 0
            )
            
            staffRepository.updateStaff(staffId, request)
                .onSuccess { response ->
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STAFF_UPDATED,
                        "Staff member updated",
                        buildStaffUpdateDescription(originalStaff, request)
                    )
                    
                    val updatedPwd = response.password
                    val updatedStaffId = response.staff?.id ?: staffId
                    if (!updatedPwd.isNullOrBlank()) {
                        _uiState.update { it.copy(passwordsByStaffId = it.passwordsByStaffId + (updatedStaffId to updatedPwd)) }
                    }
                    // Cache uploaded image URL locally for display in list if backend doesn't return it
                    if (!sanitizedImage.isNullOrBlank()) {
                        val normalized = normalizeUrl(sanitizedImage) ?: sanitizedImage
                        _uiState.update { it.copy(imagesByStaffId = it.imagesByStaffId + (updatedStaffId to normalized)) }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    // Update the staff member in the local _allStaff list
                    val staffIndex = _allStaff.indexOfFirst { it.id == staffId }
                    if (staffIndex != -1) {
                        val updatedStaff = _allStaff[staffIndex].copy(
                            name = sanitizeText(name),
                            email = sanitizeText(email),
                            phone = sanitizePhoneNumber(phone),
                            mobileNumber = sanitizePhoneNumber(mobileNumber),
                            address = sanitizeText(address),
                            position = sanitizeText(role),
                            salary = salary,
                            joinDate = sanitizeDate(joinDate),
                            workingHours = if (startTimeSanitized.isNotBlank() && endTimeSanitized.isNotBlank()) {
                                com.swadratna.swadratna_admin.data.model.WorkingHours(startTimeSanitized, endTimeSanitized)
                            } else null,
                            imageUrl = sanitizedImage ?: _allStaff[staffIndex].imageUrl,
                            status = try {
                                com.swadratna.swadratna_admin.data.model.StaffStatus.valueOf(sanitizeText(status).uppercase())
                            } catch (e: Exception) {
                                _allStaff[staffIndex].status
                            },
                            storeId = storeId ?: 0
                        )
                        _allStaff[staffIndex] = updatedStaff
                    }
                    
                    // Apply filters and sort to refresh the UI
                    applyFiltersAndSort()
                    
                    // If storeId was changed to 0 (General), we should also reload the original store's staff
                    // to ensure consistency with the backend
                    if (storeId == 0 && originalStoreId != null && originalStoreId != 0) {
                        loadStaff(originalStoreId)
                    }
                    
                    // Always reload staff data after update to ensure we have latest from backend
                    loadStaff(storeId ?: 0)
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update staff"
                        )
                    }
                }
        }
    }

    fun editStaff(staffId: Int) {
        // TODO: Implement edit staff functionality
    }

    fun deleteStaff(staffId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Find the staff member before deletion for activity tracking
            val staffToDelete = _allStaff.find { it.id == staffId }
            
            staffRepository.deleteStaff(staffId)
                .onSuccess {
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STAFF_DELETED,
                        "Staff member deleted",
                        "Staff member '${staffToDelete?.name ?: "Unknown"}' has been successfully deleted"
                    )
                    
                    _allStaff.removeIf { it.id == staffId }
                    applyFiltersAndSort()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to delete staff"
                        )
                    }
                }
        }
    }
    
    fun updateFilter(filter: String?) {
        _uiState.update { it.copy(selectedFilter = filter, isFilterMenuVisible = false) }
        applyFiltersAndSort()
    }
    
    fun updateSortOrder(sortOrder: String) {
        _uiState.update { it.copy(selectedSortOrder = sortOrder, isSortMenuVisible = false) }
        applyFiltersAndSort()
    }
    
    // Normalize URLs (handles relative paths from backend)
    private fun normalizeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            val base = ApiConstants.BASE_URL.trimEnd('/')
            val path = trimmed.trimStart('/')
            "$base/$path"
        }
    }

    // Remove accidental formatting characters/backticks from URL
    private fun sanitizeImageUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return url?.trim()?.trim('`')?.takeIf { it.isNotBlank() }
    }

    private fun sanitizeText(value: String): String {
        // Trim whitespace and strip accidental backticks from generic text fields
        return value.trim().trim('`')
    }

    private fun sanitizeTime(value: String): String {
        val raw = value.trim().trim('`')
        val hhmm = Regex("^(\\d{1,2}):(\\d{2})$")
        val hhmmss = Regex("^(\\d{1,2}):(\\d{2}):(\\d{2})$")
        val justHour = Regex("^\\d{1,2}$")
        return when {
            hhmm.matches(raw) -> {
                val (h, m) = hhmm.find(raw)!!.groupValues.drop(1)
                val hour = h.toInt().coerceIn(0, 23)
                val minute = m.toInt().coerceIn(0, 59)
                "%02d:%02d".format(hour, minute)
            }
            hhmmss.matches(raw) -> {
                val (h, m) = hhmmss.find(raw)!!.groupValues.drop(1)
                val hour = h.toInt().coerceIn(0, 23)
                val minute = m.toInt().coerceIn(0, 59)
                "%02d:%02d".format(hour, minute)
            }
            justHour.matches(raw) -> {
                val hour = raw.toInt().coerceIn(0, 23)
                "%02d:%02d".format(hour, 0)
            }
            else -> {
                return try {
                    val t = java.time.LocalTime.parse(raw)
                    "%02d:%02d".format(t.hour.coerceIn(0, 23), t.minute.coerceIn(0, 59))
                } catch (e: Exception) {
                    raw
                }
            }
        }
    }

    private fun sanitizePhoneNumber(number: String): String {
        // Keep digits only; backend typically expects numeric mobile numbers
        val trimmed = number.trim().trim('`')
        return trimmed.filter { it.isDigit() }
    }

    private fun sanitizeDate(value: String): String {
        val raw = value.trim().trim('`')
        val dmySlash = Regex("^(\\d{2})/(\\d{2})/(\\d{4})$")
        val dmyDash = Regex("^(\\d{2})-(\\d{2})-(\\d{4})$")
        val ymdDash = Regex("^(\\d{4})-(\\d{2})-(\\d{2})$")
        return when {
            ymdDash.matches(raw) -> raw
            dmySlash.matches(raw) -> {
                val (d, m, y) = dmySlash.find(raw)!!.groupValues.drop(1)
                String.format("%s-%02d-%02d", y, m.toInt(), d.toInt())
            }
            dmyDash.matches(raw) -> {
                val (d, m, y) = dmyDash.find(raw)!!.groupValues.drop(1)
                String.format("%s-%02d-%02d", y, m.toInt(), d.toInt())
            }
            else -> raw // fallback
        }
    }

    private fun applyFiltersAndSort() {
        val searchQuery = _uiState.value.searchQuery.lowercase()
        val selectedFilter = _uiState.value.selectedFilter
        val sortOrder = _uiState.value.selectedSortOrder
        
        // Overlay any locally cached image URLs onto the fetched staff list
        val imagesById = _uiState.value.imagesByStaffId
        val staffWithImages = _allStaff.map { s ->
            val rawUrl = imagesById[s.id] ?: s.imageUrl
            val normalized = normalizeUrl(rawUrl)
            if (!normalized.isNullOrBlank()) s.copy(imageUrl = normalized) else s
        }
        
        val filteredStaff = staffWithImages.filter { staff ->
            val matchesSearch = searchQuery.isEmpty() ||
                staff.name?.lowercase()?.contains(searchQuery) == true ||
                staff.position?.lowercase()?.contains(searchQuery) == true

            val matchesStatus = selectedFilter == null || staff.status.name == selectedFilter

            matchesSearch && matchesStatus
        }
        
        val sortedStaff = when (sortOrder) {
            "NAME_ASC" -> filteredStaff.sortedBy { it.name ?: "" }
            "NAME_DESC" -> filteredStaff.sortedByDescending { it.name ?: "" }
            "POSITION_ASC" -> filteredStaff.sortedBy { it.position ?: "" }
            "POSITION_DESC" -> filteredStaff.sortedByDescending { it.position ?: "" }
            else -> filteredStaff
        }
        
        _uiState.update { it.copy(staffList = sortedStaff) }
    }

    fun onEvent(event: StaffEvent) {
        when (event) {
            is StaffEvent.ToggleFilterMenu -> {
                _uiState.value = _uiState.value.copy(
                    isFilterMenuVisible = !_uiState.value.isFilterMenuVisible,
                    isSortMenuVisible = false
                )
            }
            is StaffEvent.ToggleSortMenu -> {
                _uiState.value = _uiState.value.copy(
                    isSortMenuVisible = !_uiState.value.isSortMenuVisible,
                    isFilterMenuVisible = false
                )
            }
        }
    }
}

data class StaffManagementUiState(
    val searchQuery: String = "",
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFilterMenuVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false,
    val selectedFilter: String? = null,
    val selectedSortOrder: String = "NAME_ASC",
    val passwordsByStaffId: Map<Int, String> = emptyMap(),
    val imagesByStaffId: Map<Int, String> = emptyMap(),
    val generatedPassword: String? = null,
    val isPasswordDialogVisible: Boolean = false,
    val stores: List<Store> = emptyList(),
    val isLoadingStores: Boolean = false,
    val storesError: String? = null
)

sealed interface StaffEvent {
    object ToggleFilterMenu : StaffEvent
    object ToggleSortMenu : StaffEvent
}
