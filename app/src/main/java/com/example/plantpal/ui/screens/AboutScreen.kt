package com.example.plantpal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About PlantPal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Description Card
            DescriptionCard()

            // How It Works Card
            HowItWorksCard()

            // Tips Card
            TipsCard()

            // FAQ Card
            FAQCard()

            // Version and Credits Card
            VersionCard()
        }
    }
}

@Composable
fun DescriptionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column {
                Text(
                    text = "About PlantPal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PlantPal is your personal plant care companion! Track watering schedules, add photos and care instructions, organize your plants, and receive daily push notifications to keep your green friends happy and healthy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun HowItWorksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "How It Works",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            StepItem(
                number = 1,
                title = "Add Your Plants",
                description = "Add plants with name, species, photo, and optional care instructions (up to 600 characters)"
            )

            StepItem(
                number = 2,
                title = "Set Watering Frequency",
                description = "Specify how often each plant needs water (in days)"
            )

            StepItem(
                number = 3,
                title = "Receive Daily Reminders",
                description = "Get push notifications at 9 AM each day for plants needing water"
            )

            StepItem(
                number = 4,
                title = "In-App Alerts",
                description = "See alerts when opening the app for any plants that need immediate attention"
            )

            StepItem(
                number = 5,
                title = "Mark as Watered",
                description = "Tap the water button to update the watering schedule"
            )

            StepItem(
                number = 6,
                title = "Organize Your Plants",
                description = "Long-press any plant to reorder your list with move up/down options"
            )
        }
    }
}

@Composable
fun StepItem(number: Int, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Tips for Success",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Divider()

            TipItem(
                icon = Icons.Default.CheckCircle,
                text = "Enable notifications to never miss a watering day"
            )

            TipItem(
                icon = Icons.Default.Add,
                text = "Add photos to easily identify your plants"
            )

            TipItem(
                icon = Icons.Default.Edit,
                text = "Use the instructions field to note special care needs"
            )

            TipItem(
                icon = Icons.Default.Star,
                text = "Check the 'Needs Water' section daily for urgent plants"
            )

            TipItem(
                icon = Icons.Default.Favorite,
                text = "Long-press to reorder plants by priority or location"
            )
        }
    }
}

@Composable
fun TipItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun FAQCard() {
    var expandedFAQ by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            FAQItem(
                index = 0,
                question = "How do I add a plant?",
                answer = "Tap the + button on the main screen, fill in the plant details (name, species, watering frequency), optionally add a photo and care instructions (up to 600 characters), and tap 'Add Plant'.",
                isExpanded = expandedFAQ == 0,
                onToggle = { expandedFAQ = if (expandedFAQ == 0) null else 0 }
            )

            FAQItem(
                index = 1,
                question = "How do notifications work?",
                answer = "PlantPal sends push notifications at 9 AM daily if any plants need watering. Make sure to grant notification permissions when prompted. You'll also see in-app alerts when opening the app.",
                isExpanded = expandedFAQ == 1,
                onToggle = { expandedFAQ = if (expandedFAQ == 1) null else 1 }
            )

            FAQItem(
                index = 2,
                question = "How do I change watering frequency?",
                answer = "Tap on any plant to view details, then tap the edit button (pencil icon) in the top bar. Update the watering frequency field and tap 'Update Plant'.",
                isExpanded = expandedFAQ == 2,
                onToggle = { expandedFAQ = if (expandedFAQ == 2) null else 2 }
            )

            FAQItem(
                index = 3,
                question = "How do I reorder my plants?",
                answer = "Long-press on any plant card in the list to open the reorder menu. Choose 'Move Up' or 'Move Down' to rearrange your plants in the order you prefer.",
                isExpanded = expandedFAQ == 3,
                onToggle = { expandedFAQ = if (expandedFAQ == 3) null else 3 }
            )

            FAQItem(
                index = 4,
                question = "What are care instructions?",
                answer = "Each plant can have optional care instructions (up to 600 characters) where you can note special requirements like light preferences, soil type, fertilizer schedule, or any other care tips.",
                isExpanded = expandedFAQ == 4,
                onToggle = { expandedFAQ = if (expandedFAQ == 4) null else 4 }
            )

            FAQItem(
                index = 5,
                question = "What happens if I miss a watering?",
                answer = "The plant will show as overdue with a message like 'Overdue by X days'. You'll continue to receive notifications until you mark the plant as watered. Don't worry - just water it when you can!",
                isExpanded = expandedFAQ == 5,
                onToggle = { expandedFAQ = if (expandedFAQ == 5) null else 5 }
            )
        }
    }
}

@Composable
fun FAQItem(
    index: Int,
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VersionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "PlantPal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Built with ❤️ using:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "• Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• Material Design 3",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• Room Database",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• WorkManager Notifications",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• Coil Image Loading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "© 2026 PlantPal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )


        }
    }
}
