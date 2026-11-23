package io.github.hitchclimber.universaltimers.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.hitchclimber.universaltimers.R
import io.github.hitchclimber.universaltimers.data.StepType
import io.github.hitchclimber.universaltimers.data.TimerBundle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bundles: List<TimerBundle>,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onBundleClick: (TimerBundle) -> Unit,
    onDeleteBundle: (TimerBundle) -> Unit,
    onEditBundle: (TimerBundle) -> Unit,
    onAddClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Universal Timers") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(
                                if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                            ),
                            contentDescription = "Toggle theme",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        if (bundles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No timers yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(bundles, key = { it.id }) { bundle ->
                    SwipeItem(
                        onDelete = { onDeleteBundle(bundle) },
                        onEdit = { onEditBundle(bundle) }
                    ) {
                        BundleCard(
                            bundle = bundle,
                            onClick = { onBundleClick(bundle) },
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeItem(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    content: @Composable () -> Unit,
) {

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmed by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm deletion") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteConfirmed = true
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
                false // If delete, don't dismiss until the user confirms
            } else {
                true // Otherwise doesn`t matter
            }
        }
    )

    if (deleteConfirmed) {
        LaunchedEffect(deleteConfirmed) {
            onDelete()
            deleteConfirmed = false
        }
    }

    if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
        LaunchedEffect(dismissState.currentValue) {
            onEdit()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondary
                    else -> Color.Transparent
                },
                label = "swipe-bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError,
                    )
                    SwipeToDismissBoxValue.StartToEnd -> Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSecondary,
                    )
                    else -> Unit
                }
            }
        },
    ) {
        content()
    }
}

@Composable
private fun BundleCard(
    bundle: TimerBundle,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = bundle.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val totalMs = computeBundleTotalMs(bundle)
                bundle.blocks.sumOf { it.steps.size * it.repetitions }
                val totalSets = bundle.blocks.sumOf { it.repetitions }

                InfoChip(
                    label = formatDurationCompact(totalMs),
                    color = MaterialTheme.colorScheme.primary,
                )
                InfoChip(
                    label = "$totalSets sets",
                    color = MaterialTheme.colorScheme.tertiary,
                )

                // Show work/rest breakdown for first block
                bundle.blocks.firstOrNull()?.let { block ->
                    val work = block.steps.firstOrNull { it.type == StepType.WORK }
                    val rest = block.steps.firstOrNull { it.type == StepType.REST }
                    if (work != null) {
                        Text(
                            text = "${work.baseDurationMs / 1000}s" +
                                    if (rest != null) " / ${rest.baseDurationMs / 1000}s" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

private fun computeBundleTotalMs(bundle: TimerBundle): Long {
    var total = 0L
    for ((blockIndex, block) in bundle.blocks.withIndex()) {
        for (rep in 0 until block.repetitions) {
            for ((stepIndex, step) in block.steps.withIndex()) {
                // Skip final REST of last rep unless another block follows
                if (step.type == StepType.REST
                    && stepIndex == block.steps.lastIndex
                    && rep == block.repetitions - 1
                    && blockIndex == bundle.blocks.lastIndex
                ) continue
                total += maxOf(step.baseDurationMs + step.deltaMs * rep, step.minMs)
            }
        }
    }
    return total
}

private fun formatDurationCompact(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return when {
        min > 0 && sec > 0 -> "${min}m ${sec}s"
        min > 0 -> "${min}m"
        else -> "${sec}s"
    }
}
