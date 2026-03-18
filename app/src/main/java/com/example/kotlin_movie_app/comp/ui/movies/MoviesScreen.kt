package com.example.kotlin_movie_app.comp.ui.movies

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlin_movie_app.core.utils.observeConnectivityAsFlow

enum class SeenFilter { ALL, SEEN, NOT_SEEN }
const val PAGE_SIZE = 5

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(onItemClick: (id: String?) -> Unit, onAddItem: () -> Unit, onLogout: () -> Unit) {
    val moviesViewModel = viewModel<MoviesViewModel>(factory = MoviesViewModel.Factory)
    val allMovies by moviesViewModel.uiState.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchText by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var seenFilter by remember { mutableStateOf(SeenFilter.ALL) }
    var minRating by remember { mutableStateOf("") }
    var maxRating by remember { mutableStateOf("") }
    var visibleItemCount by remember { mutableIntStateOf(PAGE_SIZE) }
    val context = LocalContext.current
    val isOnline by remember { observeConnectivityAsFlow(context) }.collectAsState(initial = true)
    var currentTemp by remember { mutableStateOf("--") }

    LaunchedEffect(isOnline) { if (isOnline) moviesViewModel.loadItems() }
    LaunchedEffect(searchText, seenFilter, minRating, maxRating) { visibleItemCount = PAGE_SIZE }
    LaunchedEffect(Unit) {
        while (true) {
            // Citim temperatura bateriei (singurul senzor termic garantat pe telefoane)
            val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val rawTemp = intent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val celsius = rawTemp / 10f
            if (celsius > -50 && celsius < 100 && celsius != 0f) currentTemp = String.format("%.1f", celsius)
            kotlinx.coroutines.delay(5000)
        }
    }

    val filteredMovies = remember(allMovies, searchText, seenFilter, minRating, maxRating) {
        allMovies.filter { movie ->
            val matchesSearch = movie.name.contains(searchText, ignoreCase = true)
            val matchesSeen = when (seenFilter) {
                SeenFilter.ALL -> true
                SeenFilter.SEEN -> movie.seen
                SeenFilter.NOT_SEEN -> !movie.seen
            }
            val r = movie.rating
            val minR = minRating.toDoubleOrNull() ?: 0.0
            val maxR = maxRating.toDoubleOrNull() ?: 10.0
            val matchesRating = r in minR..maxR

            matchesSearch && matchesSeen && matchesRating
        }
    }

    val displayedMovies = remember(filteredMovies, visibleItemCount) {
        filteredMovies.take(visibleItemCount)
    }

    val listState = rememberLazyListState()
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == totalItems - 1
        }
    }

    LaunchedEffect(isAtBottom) { if (isAtBottom && visibleItemCount < filteredMovies.size) visibleItemCount += PAGE_SIZE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    PulsingStatusTitle(text = "My Movie App", isOnline = isOnline)
                },
                actions = {
                    if (currentTemp != "--") {
                        Text(
                            text = "${currentTemp}°C",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    Button(
                        onClick = onLogout,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)
                    ) { Text("Logout") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search by title...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showFilters = !showFilters },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(56.dp)
                ) { Icon(Icons.Default.FilterList, null) }
            }
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Text("Seen Status:", style = MaterialTheme.typography.labelLarge)
                    Row {
                        FilterChip(selected = seenFilter == SeenFilter.ALL, onClick = { seenFilter = SeenFilter.ALL }, label = { Text("All") }, modifier = Modifier.padding(end = 4.dp))
                        FilterChip(selected = seenFilter == SeenFilter.SEEN, onClick = { seenFilter = SeenFilter.SEEN }, label = { Text("Seen") }, modifier = Modifier.padding(end = 4.dp))
                        FilterChip(selected = seenFilter == SeenFilter.NOT_SEEN, onClick = { seenFilter = SeenFilter.NOT_SEEN }, label = { Text("Unseen") })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rating Range:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = minRating, onValueChange = { minRating = it }, label = { Text("Min") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                        OutlinedTextField(value = maxRating, onValueChange = { maxRating = it }, label = { Text("Max") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    }
                }
            }
            if (filteredMovies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No movies found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(displayedMovies, key = { it._id }) { movie -> MovieCard(movie = movie, onItemClick = onItemClick) }
                    if (visibleItemCount < filteredMovies.size) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
