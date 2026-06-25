package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PinRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class PinState {
    object Loading : PinState()
    object Setup : PinState()
    data class Exists(val pin: String) : PinState()
}

class MainViewModel(private val repository: PinRepository) : ViewModel() {

    private val _isUnlocked = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    val pinState: StateFlow<PinState> = repository.pin
        .map { pin -> if (pin == null) PinState.Setup else PinState.Exists(pin) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PinState.Loading
        )

    fun savePin(pin: String) {
        viewModelScope.launch {
            repository.savePin(pin)
        }
    }

    fun setUnlocked(unlocked: Boolean) {
        _isUnlocked.value = unlocked
    }
}

class MainViewModelFactory(private val repository: PinRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
