package com.example.monthlyfinancetracker.ui.screens.commitments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.ui.components.CommitmentItemCard
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.components.MonthPicker
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary

@Composable
fun CommitmentsListScreen(
    viewModel: CommitmentsViewModel,
    onBack: () -> Unit,
    onCommitmentClick: (commitmentId: Long, year: Int, month: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val commitments by viewModel.filteredCommitments.collectAsState()

    Scaffold(
        topBar = {
            if (uiState.isSearchOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search commitments...") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleSearch() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                CompactTopBar(
                    title = "Commitments",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MonthPicker(
                year = uiState.selectedYear,
                month = uiState.selectedMonth,
                onPreviousMonth = { viewModel.onPreviousMonth() },
                onNextMonth = { viewModel.onNextMonth() }
            )

            // Filter Chips Bar (All, Paid, Partial, Pending)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommitmentFilter.values().forEach { filter ->
                    val isSelected = uiState.activeFilter == filter
                    val filterName = when (filter) {
                        CommitmentFilter.ALL -> "All"
                        CommitmentFilter.PAID -> "Paid"
                        CommitmentFilter.PARTIAL -> "Partial"
                        CommitmentFilter.PENDING -> "Pending"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filterName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (commitments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No commitments match your filter/search.",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(commitments) { item ->
                        CommitmentItemCard(
                            item = item,
                            onClick = {
                                onCommitmentClick(item.commitment.id, item.year, item.month)
                            }
                        )
                    }
                }
            }
        }
    }
}
