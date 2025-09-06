package com.swadratna.swadratna_admin.ui.home

import com.swadratna.swadratna_admin.base.BaseViewModel
import com.swadratna.swadratna_admin.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel<HomeUiState, HomeEvent>() {
    
    init {
        loadInitialData()
    }

    override fun createInitialState(): HomeUiState = HomeUiState()

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.UpdateMessage -> updateMessage(event.message)
            is HomeEvent.RefreshData -> loadInitialData()
        }
    }

    private fun updateMessage(message: String) {
        updateState { copy(message = message) }
    }

    private fun loadInitialData() {
        launchCoroutine {
            repository.getExample()
                .onSuccess { message ->
                    updateState { copy(message = message) }
                }
                .onFailure { error ->
                    updateState { copy(message = error.message ?: "An error occurred") }
                }
        }
    }
}

data class HomeUiState(
    val message: String = "Loading...",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface HomeEvent {
    data class UpdateMessage(val message: String) : HomeEvent
    data object RefreshData : HomeEvent
}