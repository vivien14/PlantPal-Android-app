package com.example.plantpal.ui.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.plantpal.model.Plant
import com.example.plantpal.ui.viewmodel.PlantViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Feature flag to show/hide About menu option
private const val SHOW_ABOUT_MENU = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    viewModel: PlantViewModel,
    onPlantClick: (Long) -> Unit,
    onAddPlantClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val plants by viewModel.allPlants.observeAsState(emptyList())
    val plantsNeedingWater by viewModel.plantsNeedingWater.observeAsState(emptyList())
    var showMenu by remember { mutableStateOf(false) }
    var showWateringAlert by remember { mutableStateOf(false) }
    var hasShownAlert by remember { mutableStateOf(false) }

    // Show alert when plants need water (only once per session)
    LaunchedEffect(plantsNeedingWater) {
        if (plantsNeedingWater.isNotEmpty() && !hasShownAlert) {
            showWateringAlert = true
            hasShownAlert = true
        }
    }

    // Watering Alert Dialog
    if (showWateringAlert && plantsNeedingWater.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showWateringAlert = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (plantsNeedingWater.size == 1) 
                        "Plant Needs Water!" 
                    else 
                        "${plantsNeedingWater.size} Plants Need Water!"
                )
            },
            text = {
                Column {
                    Text("The following plants need watering:")
                    Spacer(modifier = Modifier.height(8.dp))
                    plantsNeedingWater.take(5).forEach { plant ->
                        Text(
                            text = "• ${plant.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (plantsNeedingWater.size > 5) {
                        Text(
                            text = "...and ${plantsNeedingWater.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWateringAlert = false }) {
                    Text("Got it!")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Plants") },
                actions = {
                    if (SHOW_ABOUT_MENU) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    showMenu = false
                                    onAboutClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlantClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Plant")
            }
        }
    ) { paddingValues ->
        if (plants.isEmpty()) {
            EmptyPlantList(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (plantsNeedingWater.isNotEmpty()) {
                    item {
                        Text(
                            text = "Needs Water (${plantsNeedingWater.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(plantsNeedingWater) { plant ->
                        PlantCard(
                            plant = plant,
                            needsWater = true,
                            onClick = { onPlantClick(plant.id) },
                            onWaterClick = { viewModel.waterPlant(plant.id) },
                            onMoveUp = null,
                            onMoveDown = null
                        )
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Plants",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Long press to reorder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                itemsIndexed(plants) { index, plant ->
                    PlantCard(
                        plant = plant,
                        needsWater = plantsNeedingWater.any { it.id == plant.id },
                        onClick = { onPlantClick(plant.id) },
                        onWaterClick = { viewModel.waterPlant(plant.id) },
                        onMoveUp = if (index > 0) { { viewModel.movePlantUp(plant, plants) } } else null,
                        onMoveDown = if (index < plants.size - 1) { { viewModel.movePlantDown(plant, plants) } } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlantCard(
    plant: Plant,
    needsWater: Boolean,
    onClick: () -> Unit,
    onWaterClick: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    val context = LocalContext.current
    var showReorderMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { 
                        if (onMoveUp != null || onMoveDown != null) {
                            showReorderMenu = true 
                        }
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (needsWater) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plant photo thumbnail
            plant.photoUri?.let { uriString ->
                if (uriString.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(uriString))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Plant thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = plant.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Watering countdown
                val daysUntilWater = calculateDaysUntilWatering(plant)
                val daysSinceWatering = (System.currentTimeMillis() - plant.lastWatered) / 86400000L

                val countdownText = when {
                    daysSinceWatering.toInt() == 0 && daysUntilWater <= 0 -> "Watered today"
                    daysUntilWater < 0 -> "Overdue by ${-daysUntilWater} day${if (-daysUntilWater != 1) "s" else ""}"
                    daysUntilWater == 0 -> "Water today"
                    daysUntilWater == 1 -> "Water tomorrow"
                    else -> "Water in $daysUntilWater days"
                }

                Text(
                    text = countdownText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        daysSinceWatering.toInt() == 0 && daysUntilWater <= 0 -> MaterialTheme.colorScheme.primary
                        daysUntilWater < 0 -> MaterialTheme.colorScheme.error
                        daysUntilWater == 0 -> MaterialTheme.colorScheme.error
                        daysUntilWater == 1 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )

                Text(
                    text = "Last watered: ${formatDate(plant.lastWatered)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (needsWater) {
                Button(
                    onClick = onWaterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Water")
                }
            }
        }
    }
    
        // Reorder dropdown menu
        DropdownMenu(
            expanded = showReorderMenu,
            onDismissRequest = { showReorderMenu = false }
        ) {
            if (onMoveUp != null) {
                DropdownMenuItem(
                    text = { Text("Move Up") },
                    onClick = {
                        onMoveUp()
                        showReorderMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up"
                        )
                    }
                )
            }
            if (onMoveDown != null) {
                DropdownMenuItem(
                    text = { Text("Move Down") },
                    onClick = {
                        onMoveDown()
                        showReorderMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyPlantList(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No plants yet!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to add your first plant",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun calculateDaysUntilWatering(plant: Plant): Int {
    val nextWateringTime = plant.lastWatered +
            TimeUnit.DAYS.toMillis(plant.wateringFrequencyDays.toLong())
    val currentTime = System.currentTimeMillis()
    val millisUntilWater = nextWateringTime - currentTime
    return TimeUnit.MILLISECONDS.toDays(millisUntilWater).toInt()
}
