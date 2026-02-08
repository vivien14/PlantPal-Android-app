package com.example.plantpal.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.plantpal.ui.viewmodel.PlantViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "AddEditPlantScreen"

// Helper function to format date
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Helper function to create image file for camera
private fun createImageFile(context: Context): File? {
    return try {
        // Create an image file name
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "PLANT_${timeStamp}.jpg"

        // Get the cache directory
        val storageDir = context.cacheDir

        // Ensure directory exists
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // Create the file
        val imageFile = File(storageDir, imageFileName)

        // Create the file if it doesn't exist
        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }

        Log.d(TAG, "Created image file: ${imageFile.absolutePath}")
        imageFile
    } catch (e: IOException) {
        Log.e(TAG, "Error creating image file", e)
        null
    }
}

// Helper function to copy image to permanent storage
private fun copyImageToAppStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        Log.d(TAG, "Copying image from URI: $sourceUri")

        val picturesDir = File(context.getExternalFilesDir(null), "Pictures")
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
            Log.d(TAG, "Created pictures directory: ${picturesDir.absolutePath}")
        }

        val fileName = "plant_${System.currentTimeMillis()}.jpg"
        val destFile = File(picturesDir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        Log.d(TAG, "Copied image to: ${destFile.absolutePath}")
        Uri.fromFile(destFile)
    } catch (e: Exception) {
        Log.e(TAG, "Error copying image to app storage", e)
        e.printStackTrace()
        null
    }
}

// Helper function to take persistent URI permission for gallery images
private fun takePersistableUriPermission(context: Context, uri: Uri) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantScreen(
    viewModel: PlantViewModel,
    plantId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = plantId != null && plantId != -1L
    val plant by viewModel.getPlantById(plantId ?: -1L).observeAsState()

    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var wateringFrequency by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var lastWateredDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var instructions by remember { mutableStateOf("") }

    // Keep track of the temporary camera file URI
    val tempCameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // State to trigger camera launch after permission is granted
    var shouldLaunchCamera by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        Log.d(TAG, "Camera result: success=$success, tempUri=${tempCameraImageUri.value}")
        if (success && tempCameraImageUri.value != null) {
            try {
                // Copy the image from cache to permanent storage
                val permanentUri = copyImageToAppStorage(context, tempCameraImageUri.value!!)
                if (permanentUri != null) {
                    photoUri = permanentUri
                    Log.d(TAG, "Photo saved successfully: $permanentUri")
                    showError = null
                } else {
                    Log.e(TAG, "Failed to copy image to permanent storage")
                    showError = "Failed to save photo"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing camera image", e)
                showError = "Error saving photo: ${e.message}"
            }
        } else {
            Log.d(TAG, "Camera capture cancelled or failed")
        }
        // Clean up temp URI
        tempCameraImageUri.value = null
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Camera permission granted")
            shouldLaunchCamera = true
        } else {
            Log.e(TAG, "Camera permission denied")
            showError = "Camera permission is required to take photos"
        }
    }

    // Effect to launch camera when permission is granted
    LaunchedEffect(shouldLaunchCamera) {
        if (shouldLaunchCamera) {
            try {
                val imageFile = createImageFile(context)
                if (imageFile != null) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageFile
                    )
                    tempCameraImageUri.value = uri
                    Log.d(TAG, "Launching camera with URI: $uri")
                    cameraLauncher.launch(uri)
                } else {
                    Log.e(TAG, "Failed to create image file")
                    showError = "Failed to create image file"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching camera", e)
                showError = "Error launching camera: ${e.message}"
            }
            shouldLaunchCamera = false
        }
    }

    // Gallery launcher (don't touch - it works)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                Log.d(TAG, "Gallery image selected: $selectedUri")
                // Take persistent permission for the URI
                takePersistableUriPermission(context, selectedUri)
                // Copy to app storage to ensure we have permanent access
                val permanentUri = copyImageToAppStorage(context, selectedUri)
                photoUri = permanentUri ?: selectedUri
                showError = null
            } catch (e: Exception) {
                Log.e(TAG, "Error processing gallery image", e)
                showError = "Error loading photo: ${e.message}"
            }
        }
    }

    // Load existing plant data when in edit mode
    LaunchedEffect(plant) {
        plant?.let {
            name = it.name
            species = it.species
            wateringFrequency = it.wateringFrequencyDays.toString()
            photoUri = it.photoUri?.let { uri -> Uri.parse(uri) }
            lastWateredDate = it.lastWatered
            instructions = it.instructions ?: ""
        }
    }

    // Handle navigation after deletion
    LaunchedEffect(plant, isDeleting) {
        if (isDeleting && plant == null) {
            // Plant has been deleted, navigate back
            onNavigateBack()
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = lastWateredDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            lastWateredDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Plant" else "Add Plant") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = wateringFrequency,
                onValueChange = { wateringFrequency = it },
                label = { Text("Watering Frequency (days)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Last Watered Date Section
            Text(
                text = "Last Watered",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatDate(lastWateredDate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Tap to change date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Care Instructions Section (optional)
            Text(
                text = "Care Instructions (optional)",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = instructions,
                onValueChange = { 
                    // Limit to 600 characters
                    if (it.length <= 600) {
                        instructions = it
                    }
                },
                label = { Text("Add care tips, notes, or special instructions...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8,
                supportingText = {
                    Text(
                        text = "${instructions.length}/600 characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (instructions.length > 550) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Photo Section
            Text(
                text = "Plant Photo (optional)",
                style = MaterialTheme.typography.titleMedium
            )

            // Error message if any
            showError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Photo preview if available
            photoUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Plant photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Photo selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        Log.d(TAG, "Camera button clicked")
                        // Check for camera permission
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) -> {
                                Log.d(TAG, "Camera permission already granted")
                                // Permission already granted, launch camera directly
                                try {
                                    val imageFile = createImageFile(context)
                                    if (imageFile != null) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            imageFile
                                        )
                                        tempCameraImageUri.value = uri
                                        Log.d(TAG, "Launching camera with URI: $uri")
                                        cameraLauncher.launch(uri)
                                    } else {
                                        Log.e(TAG, "Failed to create image file")
                                        showError = "Failed to create image file"
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error launching camera", e)
                                    showError = "Error launching camera: ${e.message}"
                                }
                            }
                            else -> {
                                Log.d(TAG, "Requesting camera permission")
                                // Request permission
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera")
                }

                OutlinedButton(
                    onClick = {
                        Log.d(TAG, "Gallery button clicked")
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val frequencyValue = wateringFrequency.toIntOrNull()
            val isFormValid = name.isNotBlank() &&
                             species.isNotBlank() &&
                             frequencyValue != null &&
                             frequencyValue > 0

            Button(
                onClick = {
                    val frequency = wateringFrequency.toIntOrNull()
                    if (name.isNotBlank() && species.isNotBlank() && frequency != null && frequency > 0) {
                        if (isEditMode && plant != null) {
                            viewModel.updatePlant(
                                plant!!.copy(
                                    name = name,
                                    species = species,
                                    wateringFrequencyDays = frequency,
                                    lastWatered = lastWateredDate,
                                    photoUri = photoUri?.toString(),
                                    instructions = instructions.ifBlank { null }
                                )
                            )
                        } else {
                            viewModel.addPlant(
                                name = name,
                                species = species,
                                wateringFrequencyDays = frequency,
                                photoUri = photoUri?.toString(),
                                lastWatered = lastWateredDate,
                                instructions = instructions.ifBlank { null }
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text(if (isEditMode) "Update Plant" else "Add Plant")
            }

            if (isEditMode) {
                OutlinedButton(
                    onClick = {
                        plant?.let {
                            isDeleting = true
                            viewModel.deletePlant(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDeleting,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Delete Plant")
                }
            }
        }
    }
}
