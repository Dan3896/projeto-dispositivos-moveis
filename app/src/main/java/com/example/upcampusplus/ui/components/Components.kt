package com.example.upcampusplus.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.upcampusplus.R
import com.example.upcampusplus.domain.isOverdue
import com.example.upcampusplus.domain.model.Task
import com.example.upcampusplus.ui.common.formatDue
import com.example.upcampusplus.ui.common.labelRes
import com.example.upcampusplus.ui.theme.ClarityTheme
import java.time.LocalDate

/** Cabeçalho único de todas as telas: título, subtítulo e um espaço à direita (ex.: XP). */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = ClarityTheme.colors
    val spacing = ClarityTheme.spacing
    Box(
        modifier
            .fillMaxWidth()
            .background(colors.header)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = spacing.lg, vertical = spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onHeader,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(spacing.xs))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onHeaderMuted)
            }
            if (trailing != null) {
                Spacer(Modifier.width(spacing.md))
                trailing()
            }
        }
    }
}

/** Contador de Hoje: número e rótulo; cor por significado. */
@Composable
fun CounterCard(value: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.semantics(mergeDescendants = true) { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(ClarityTheme.spacing.md)) {
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Itens lado a lado que quebram para a linha de baixo quando falta largura (fonte grande). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WrappingRow(
    modifier: Modifier = Modifier,
    minItemWidth: androidx.compose.ui.unit.Dp = 96.dp,
    content: @Composable (Modifier) -> Unit,
) {
    val fontScale = LocalDensity.current.fontScale
    val spacing = ClarityTheme.spacing
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        // Cresce com a fonte até 1,5x: com fonte em 200% os contadores quebram em 2 linhas, não em 3.
        content(Modifier.weight(1f).widthIn(min = minItemWidth * fontScale.coerceAtMost(1.5f)))
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, count: String? = null) {
    Row(
        modifier.fillMaxWidth().padding(top = ClarityTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        if (count != null) {
            Text(count, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Badge(text: String, container: Color, content: Color) {
    Surface(color = container, shape = MaterialTheme.shapes.small) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = content,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/**
 * Cartão de tarefa usado em Hoje e Tarefas: borda na cor da prioridade, caixa de conclusão,
 * título, área · prioridade · prazo e selo ATRASADA. Toque edita; toque longo remove.
 * Tarefa do Inbox não tem caixa (só organizada conclui, RN-20): mostra o selo INBOX e o toque organiza.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: Task,
    areaName: String?,
    today: LocalDate,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ClarityTheme.colors
    val spacing = ClarityTheme.spacing
    val overdue = task.isOverdue(today)
    val priorityLabel = stringResource(task.priority.labelRes())
    val removeLabel = stringResource(R.string.action_remove_task)
    val editLabel = stringResource(if (task.isInInbox) R.string.action_organize_task else R.string.action_edit_task)
    val stateLabel = stringResource(if (task.isCompleted) R.string.state_completed else R.string.state_pending)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val noDueLabel = stringResource(R.string.due_none)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            val stripe = if (task.isInInbox) MaterialTheme.colorScheme.outlineVariant else colors.border(task.priority)
            Box(Modifier.width(6.dp).fillMaxHeight().background(stripe))
            Row(
                Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClickLabel = editLabel,
                        onLongClickLabel = removeLabel,
                        onLongClick = onRemove,
                        onClick = onClick,
                    )
                    // RF-010: remoção acessível sem depender do toque longo.
                    .semantics {
                        customActions = listOf(CustomAccessibilityAction(removeLabel) { onRemove(); true })
                    }
                    .padding(end = spacing.md, top = spacing.xs, bottom = spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (task.isInInbox) {
                    // Mesmo espaço da caixa, para os títulos ficarem alinhados na lista.
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Inbox, contentDescription = null, tint = muted)
                    }
                } else {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = onCheckedChange,
                        modifier = Modifier.semantics {
                            contentDescription = task.title
                            stateDescription = stateLabel
                        },
                    )
                }
                Column(Modifier.weight(1f).padding(vertical = spacing.sm)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted) muted else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(spacing.xs))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (task.isInInbox) {
                            Badge(
                                stringResource(R.string.badge_inbox),
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        val meta = buildAnnotatedString {
                            var empty = true
                            fun part(block: AnnotatedString.Builder.() -> Unit) {
                                if (!empty) append(" · ")
                                block()
                                empty = false
                            }
                            areaName?.let { part { append(it) } }
                            // No Inbox a prioridade ainda é o padrão: só área e data, se já tiverem sido escolhidas.
                            if (task.organized) {
                                part {
                                    withStyle(SpanStyle(color = colors.text(task.priority), fontWeight = FontWeight.SemiBold)) {
                                        append(priorityLabel)
                                    }
                                }
                            }
                            when {
                                task.dueDate != null -> part { append(formatDue(task.dueDate, today)) }
                                task.organized -> part { append(noDueLabel) }
                            }
                        }
                        if (meta.isNotEmpty()) {
                            Text(meta, style = MaterialTheme.typography.bodyMedium, color = muted)
                        }
                        if (overdue) {
                            Badge(stringResource(R.string.badge_overdue), colors.overdueContainer, colors.overdue)
                        }
                    }
                }
            }
        }
    }
}

/** Seletor de opções lado a lado (área, prioridade); quebra linha se faltar largura. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> OptionSelector(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClarityTheme.spacing.sm),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
                enabled = enabled,
            )
        }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier, hint: String? = null) {
    Column(
        modifier.fillMaxWidth().padding(vertical = ClarityTheme.spacing.xl, horizontal = ClarityTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        if (hint != null) {
            Spacer(Modifier.height(ClarityTheme.spacing.xs))
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Linha de estado vazio dentro de uma seção (RF-014). */
@Composable
fun EmptyLine(message: String, modifier: Modifier = Modifier) {
    Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(vertical = ClarityTheme.spacing.md),
    )
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(ClarityTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(ClarityTheme.spacing.md))
        Text(message, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(ClarityTheme.spacing.lg))
        Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ConfirmDialog(
    title: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = text?.let { { Text(it) } },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    )
}

/** Cartão de dica, com ícone opcional. */
@Composable
fun InfoCard(title: String, text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(Modifier.padding(ClarityTheme.spacing.lg)) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(ClarityTheme.spacing.md))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
