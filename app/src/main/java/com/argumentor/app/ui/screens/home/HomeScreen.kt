package com.argumentor.app.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
import com.argumentor.app.data.model.Topic
import com.argumentor.app.ui.common.UiState
import com.argumentor.app.ui.components.EngagingEmptyState
import com.argumentor.app.ui.components.HighlightedText
import com.argumentor.app.ui.components.TopicCardSkeleton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTopic: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFallacyCatalog: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Extract topics from uiState
    val topics = when (val state = uiState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    // Extract loading state from uiState
    val isLoading = uiState is UiState.Loading
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Trigger refresh when pull to refresh is triggered
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refresh()
        }
    }

    // Reset pull to refresh state when refresh completes
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) },
                        selected = true,
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nav_new_topic)) },
                        label = { Text(stringResource(R.string.nav_new_topic)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToCreate()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_statistics)) },
                        label = { Text(stringResource(R.string.nav_statistics)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToStatistics()
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.FileUpload, contentDescription = stringResource(R.string.nav_import_export)) },
                        label = { Text(stringResource(R.string.nav_import_export)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToImportExport()
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.School, contentDescription = stringResource(R.string.nav_fallacy_catalog)) },
                        label = { Text(stringResource(R.string.nav_fallacy_catalog)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToFallacyCatalog()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSettings()
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.home_title),
                            modifier = Modifier.semantics { heading() }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.accessibility_menu)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onNavigateToCreate) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.accessibility_create_topic)
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                // Search bar with loading indicator
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        placeholder = { Text(stringResource(R.string.home_search_hint)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.accessibility_search)
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.action_clear_search)
                                    )
                                }
                            }
                        }
                    )

                    // Clear filters chip when active
                    val currentTag = selectedTag
                    val hasActiveFilters = searchQuery.isNotEmpty() || currentTag != null
                    if (hasActiveFilters && currentTag != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.onTagSelected(null) },
                                label = { Text(currentTag) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.action_remove_filter),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )

                            if (searchQuery.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        viewModel.onSearchQueryChange("")
                                        viewModel.onTagSelected(null)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.action_clear_all))
                                }
                            }
                        }
                    }

                    // Linear progress indicator (better visibility than circular in trailing icon)
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(if (hasActiveFilters) 8.dp else 16.dp))
                    }
                }

                // Topics list with live region for accessibility or error state
                when (uiState) {
                    is UiState.Error -> {
                        EngagingEmptyState(
                            icon = Icons.Default.ErrorOutline,
                            title = stringResource(R.string.home_error_title),
                            description = (uiState as UiState.Error).message,
                            actionText = stringResource(R.string.action_retry),
                            onAction = { viewModel.retry() }
                        )
                    }
                    is UiState.Loading, is UiState.Initial -> {
                        // Show loading indicator or keep existing list
                        if (topics.isEmpty()) {
                            // Show skeleton screens for better perceived performance
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(5) { // Show 5 skeleton cards
                                    TopicCardSkeleton()
                                }
                            }
                        } else {
                            // Show existing list while loading
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                        // contentDescription will need manual fix
                                    },
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = topics,
                                    key = { it.id }
                                ) { topic ->
                                    TopicCard(
                                        topic = topic,
                                        selectedTag = selectedTag,
                                        searchQuery = searchQuery,
                                        onClick = { onNavigateToTopic(topic.id) },
                                        onTagClick = viewModel::onTagSelected,
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                    is UiState.Empty -> {
                        EngagingEmptyState(
                            icon = Icons.Default.Topic,
                            title = stringResource(R.string.home_empty_title),
                            description = stringResource(R.string.home_empty_description),
                            actionText = stringResource(R.string.home_empty_action),
                            onAction = onNavigateToCreate
                        )
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .semantics {
                                    liveRegion = LiveRegionMode.Polite
                                    // contentDescription will need manual fix
                                },
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = topics,
                                key = { it.id }
                            ) { topic ->
                                TopicCard(
                                    topic = topic,
                                    selectedTag = selectedTag,
                                    searchQuery = searchQuery,
                                    onClick = { onNavigateToTopic(topic.id) },
                                    onTagClick = viewModel::onTagSelected,
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }
                    }
                }
            }

            // Pull-to-refresh indicator
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicCard(
    topic: Topic,
    selectedTag: String?,
    searchQuery: String = "",
    onClick: () -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .semantics(mergeDescendants = true) {
                // contentDescription will need manual fix
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title with stronger visual weight and search highlights
            HighlightedText(
                text = topic.title,
                query = searchQuery,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary with reduced visual weight and search highlights
            HighlightedText(
                text = topic.summary,
                query = searchQuery,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2
            )

            // Tags section with proper spacing
            if (topic.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    topic.tags.take(3).forEach { tag ->
                        FilterChip(
                            selected = tag == selectedTag,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTagClick(tag)
                            },
                            label = {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                    if (topic.tags.size > 3) {
                        Text(
                            text = stringResource(R.string.more_tags, topic.tags.size - 3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}
