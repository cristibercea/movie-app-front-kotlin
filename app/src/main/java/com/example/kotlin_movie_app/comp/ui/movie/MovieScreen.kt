package com.example.kotlin_movie_app.comp.ui.movie

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlin_movie_app.comp.data.MovieLocation
import com.example.kotlin_movie_app.core.ImageUtils
import com.example.kotlin_movie_app.core.Result
import com.example.kotlin_movie_app.core.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(itemId: String, onClose: () -> Unit) {
    val movieViewModel = viewModel<MovieViewModel>(factory = MovieViewModel.Factory(itemId))
    val movieUiState = movieViewModel.uiState
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var dateIso by rememberSaveable { mutableStateOf("") } // Parsare ca String ISO
    var location by rememberSaveable { mutableStateOf(MovieLocation(0.0, 0.0)) }
    var seen by rememberSaveable { mutableStateOf(false) }
    var rating: Float by rememberSaveable { mutableFloatStateOf(0.0f) }
    var image by rememberSaveable { mutableStateOf("-") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.isoToMillis(dateIso)
    )
    var isDataLoaded by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val base64 = ImageUtils.uriToBase64(context, uri)
                withContext(Dispatchers.Main) { if (base64 != null) image = base64 }
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            scope.launch(Dispatchers.IO) {
                val base64 = ImageUtils.uriToBase64(context, tempCameraUri!!)
                withContext(Dispatchers.Main) { if (base64 != null) image = base64 }
            }
        }
    }

    LaunchedEffect(movieUiState.loadResult) {
        if (!isDataLoaded && movieUiState.loadResult is Result.Success) {
            Log.d(TAG, "edit an existing movie")
            val movie = movieUiState.movie
            name = movie.name
            description = movie.description
            dateIso = movie.date.ifEmpty { DateUtils.millisToIso(System.currentTimeMillis()) }
            location = movie.location
            seen = movie.seen
            rating = movie.rating
            image = movie.image
            isDataLoaded = true
        } else if (!isDataLoaded && itemId == "none") {
            Log.d(TAG, "add a new movie")
            dateIso = DateUtils.millisToIso(System.currentTimeMillis())
            isDataLoaded = true
        }
    }

    LaunchedEffect(movieUiState.submitResult) { if(movieUiState.submitResult is Result.Success) onClose() }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Edit Movie") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (movieUiState.loadResult is Result.Loading || movieUiState.submitResult is Result.Loading)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { showImageSourceDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                ) { Base64ImageDisplay(base64String = image, modifier = Modifier.fillMaxSize()) }
                if (showImageSourceDialog) {
                    AlertDialog(
                        onDismissRequest = { showImageSourceDialog = false },
                        title = { Text("Select Image Source") },
                        text = { Text("Choose where to get the image from:") },
                        confirmButton = {
                            TextButton(onClick = {
                                showImageSourceDialog = false
                                val uri = ImageUtils.createTempPictureUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            }) {
                                Icon(Icons.Default.CameraAlt, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Camera")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showImageSourceDialog = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Icon(Icons.Default.PhotoLibrary, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Gallery")
                            }
                        }
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = if (rating == 0.0f) "" else rating.toString(),
                    onValueChange = {
                        val parsed = it.toFloatOrNull()
                        if (parsed != null && parsed in 0.0f..10.0f) rating = parsed
                        else if (it.isEmpty()) rating = 0.0f
                    },
                    label = { Text("Rating (0-10)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = DateUtils.isoToDisplay(dateIso),
                    onValueChange = { },
                    label = { Text("Release Date") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { seen = !seen },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = seen, onCheckedChange = { seen = it })
                    Text(text = "Already Watched?", modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                movieViewModel.saveOrUpdateMovie(
                                    itemId, name, description, dateIso, location, seen, rating, image
                                )
                            }
                        }
                    ) { Text("Save") }
                }
                if (movieUiState.submitResult is Result.Error) {
                    Text(
                        text = "Error: ${(movieUiState.submitResult as Result.Error).exception?.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis -> dateIso = DateUtils.millisToIso(millis) }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) { DatePicker(state = datePickerState) }
        }
    }
}