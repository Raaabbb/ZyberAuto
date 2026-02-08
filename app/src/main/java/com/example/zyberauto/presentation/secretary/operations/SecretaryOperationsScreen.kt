package com.example.zyberauto.presentation.secretary.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.InventoryItem
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.presentation.common.components.DropdownField
import com.example.zyberauto.presentation.common.components.LoadingSpinner
import com.example.zyberauto.presentation.common.components.SearchBar
import com.example.zyberauto.presentation.secretary.customers.CustomersViewModel
import com.example.zyberauto.presentation.secretary.customers.CustomersUiState
import com.example.zyberauto.presentation.secretary.inventory.InventoryViewModel
import com.example.zyberauto.presentation.secretary.inventory.InventoryUiState
import kotlinx.coroutines.launch

/**
 * Unified Operations screen with tabbed navigation for Inventory and Customers.
 * Replaces the previous grid-based navigation with an embedded tab experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryOperationsScreen(
    onNavigateTo: (String) -> Unit
) {
    val tabs = listOf(
        TabItem("Inventory", Icons.Default.Inventory),
        TabItem("Customers", Icons.Default.People)
    )
    
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(tab.title) },
                    icon = { 
                        Icon(
                            imageVector = tab.icon, 
                            contentDescription = tab.title,
                            modifier = Modifier.size(20.dp)
                        ) 
                    }
                )
            }
        }
        
        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> InventoryTabContent()
                1 -> CustomersTabContent()
            }
        }
    }
}

private data class TabItem(val title: String, val icon: ImageVector)

// ============================================================================
// INVENTORY TAB CONTENT
// ============================================================================

@Composable
private fun InventoryTabContent(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<InventoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Inventory") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is InventoryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingSpinner()
                    }
                }
                is InventoryUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is InventoryUiState.Success -> {
                    val filteredList = state.items.filter { item ->
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No items found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                        ) {
                            items(filteredList) { item ->
                                InventoryItemCard(
                                    item = item,
                                    onIncrease = { viewModel.updateQuantity(item, 1) },
                                    onDecrease = { viewModel.updateQuantity(item, -1) },
                                    onEdit = { itemToEdit = item },
                                    onDelete = { itemToDelete = item }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditInventoryDialog(
            item = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, category, qty, price ->
                viewModel.addItem(name, category, qty, price)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    if (itemToEdit != null) {
        AddEditInventoryDialog(
            item = itemToEdit,
            onDismiss = { itemToEdit = null },
            onSave = { name, category, qty, price ->
                viewModel.updateItem(itemToEdit!!.copy(name = name, category = category, quantity = qty, price = price))
                itemToEdit = null
            }
        )
    }
    
    // Delete Confirmation
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete '${itemToDelete?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteItem(it.id) }
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${item.category} • ₱${item.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Default.Remove, "Decrease", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, "Increase", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AddEditInventoryDialog(
    item: InventoryItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Double) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Parts") }
    var quantityStr by remember { mutableStateOf(item?.quantity?.toString() ?: "0") }
    var priceStr by remember { mutableStateOf(item?.price?.toString() ?: "0.0") }
    
    val categories = com.example.zyberauto.common.AppConstants.InventoryCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Item" else "Edit Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                DropdownField(
                    value = category,
                    onValueChange = { category = it },
                    options = categories,
                    label = "Category",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) quantityStr = it 
                        },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toIntOrNull() ?: 0
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                         onSave(name, category, qty, price)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ============================================================================
// CUSTOMERS TAB CONTENT
// ============================================================================

@Composable
private fun CustomersTabContent(
    viewModel: CustomersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is CustomersUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingSpinner()
                }
            }
            is CustomersUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is CustomersUiState.Success -> {
                val filteredList = state.customers.filter { user ->
                    user.name.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true) ||
                    user.phoneNumber.contains(searchQuery, ignoreCase = true)
                }

                if (filteredList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("No customers found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList) { user ->
                            CustomerCard(user = user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerCard(user: User) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = "Name", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = user.name.ifBlank { "Unnamed User" },
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (user.phoneNumber.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = user.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
