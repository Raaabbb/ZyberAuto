package com.example.zyberauto.presentation.secretary.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.InventoryItem
import com.example.zyberauto.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            _uiState.value = InventoryUiState.Loading
            repository.getInventory()
                .catch { e ->
                    _uiState.value = InventoryUiState.Error(e.message ?: "Failed to load inventory")
                }
                .collectLatest { items ->
                    _uiState.value = InventoryUiState.Success(items)
                }
        }
    }

    fun addItem(name: String, category: String, quantity: Int, price: Double) {
        viewModelScope.launch {
            val newItem = InventoryItem(
                name = name,
                category = category,
                quantity = quantity,
                price = price
            )
            repository.addItem(newItem)
        }
    }

    fun updateItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(lastUpdated = java.util.Date()))
        }
    }
    
    fun updateQuantity(item: InventoryItem, delta: Int) {
        val newQuantity = (item.quantity + delta).coerceAtLeast(0)
        if (newQuantity != item.quantity) {
             viewModelScope.launch {
                 repository.updateItem(item.copy(quantity = newQuantity, lastUpdated = java.util.Date()))
             }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }
}

sealed class InventoryUiState {
    object Loading : InventoryUiState()
    data class Success(val items: List<InventoryItem>) : InventoryUiState()
    data class Error(val message: String) : InventoryUiState()
}
