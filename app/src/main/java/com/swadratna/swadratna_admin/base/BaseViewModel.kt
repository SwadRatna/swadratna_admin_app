package com.swadratna.swadratna_admin.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Event> : ViewModel() {
    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    abstract fun createInitialState(): State

    abstract fun handleEvent(event: Event)

    protected fun updateState(reducer: State.() -> State) {
        val newState = uiState.value.reducer()
        _uiState.value = newState
    }

    protected fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}