package com.example.zyberauto.presentation.secretary.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomersUiState>(CustomersUiState.Loading)
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = CustomersUiState.Loading
            repository.getAllCustomers()
                .catch { e ->
                    _uiState.value = CustomersUiState.Error(e.message ?: "Failed to load customers")
                }
                .collectLatest { customers ->
                    _uiState.value = CustomersUiState.Success(customers)
                }
        }
    }
}

sealed class CustomersUiState {
    object Loading : CustomersUiState()
    data class Success(val customers: List<User>) : CustomersUiState()
    data class Error(val message: String) : CustomersUiState()
}
