package com.example.upcampusplus.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upcampusplus.R
import com.example.upcampusplus.domain.HistoryEntry
import com.example.upcampusplus.ui.common.formatFullDate
import com.example.upcampusplus.ui.common.formatTime
import com.example.upcampusplus.ui.components.CounterCard
import com.example.upcampusplus.ui.components.EmptyState
import com.example.upcampusplus.ui.components.ErrorState
import com.example.upcampusplus.ui.components.LoadingState
import com.example.upcampusplus.ui.components.ScreenHeader
import com.example.upcampusplus.ui.components.WrappingRow
import com.example.upcampusplus.ui.theme.ClarityTheme
import java.time.LocalDate

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.history_title), stringResource(R.string.history_subtitle))
        when (val s = state) {
            HistoryUiState.Loading -> LoadingState()
            HistoryUiState.Error -> ErrorState(stringResource(R.string.load_error), onRetry = viewModel::retry)
            is HistoryUiState.Content -> HistoryContent(s, query, viewModel::onQueryChange)
        }
    }
}

@Composable
private fun HistoryContent(state: HistoryUiState.Content, query: String, onQueryChange: (String) -> Unit) {
    val spacing = ClarityTheme.spacing
    val history = state.history

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        item(key = "counters") {
            // 2 por linha em celulares: quatro lado a lado quebrariam "Faculdade" no meio da palavra.
            WrappingRow(minItemWidth = 150.dp) { itemModifier ->
                state.areas.forEach { area ->
                    CounterCard(
                        value = history.areaCounts[area.id] ?: 0,
                        label = area.name,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = itemModifier,
                    )
                }
            }
        }
        item(key = "search") {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.search_history)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm),
            )
        }
        when {
            history.totalEntries == 0 -> item(key = "empty") { EmptyState(stringResource(R.string.history_empty)) }
            history.days.isEmpty() -> item(key = "no-results") {
                EmptyState(stringResource(R.string.history_no_results, history.query))
            }
            else -> history.days.forEach { day ->
                item(key = "day-${day.date}") { DayLabel(day.date, state.today) }
                items(day.entries, key = { it.key }) { entry -> TimelineItem(entry, state) }
            }
        }
    }
}

@Composable
private fun DayLabel(date: LocalDate, today: LocalDate) {
    val label = when (date) {
        today -> stringResource(R.string.day_today)
        today.minusDays(1) -> stringResource(R.string.day_yesterday)
        else -> formatFullDate(date)
    }
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = ClarityTheme.spacing.md).semantics { heading() },
    )
}

@Composable
private fun TimelineItem(entry: HistoryEntry, state: HistoryUiState.Content) {
    val spacing = ClarityTheme.spacing
    val meta = listOfNotNull(entry.areaId?.let { state.areaNames[it] }, formatTime(entry.at, state.zone))
    Card(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(spacing.md), verticalAlignment = Alignment.Top) {
            Box(Modifier.padding(top = 6.dp).size(10.dp).background(ClarityTheme.colors.success, CircleShape))
            Spacer(Modifier.width(spacing.md))
            Column {
                Text(stringResource(R.string.history_completed, entry.text), style = MaterialTheme.typography.bodyLarge)
                Text(
                    meta.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
