package com.argumentor.app.ui.screens.fallacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R

/**
 * Screen for creating or editing a fallacy.
 * Provides a form with validation for all fallacy fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallacyFormScreen(
    fallacyId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: FallacyFormViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val example by viewModel.example.collectAsState()
    val category by viewModel.category.collectAsState()

    val nameError by viewModel.nameError.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()
    val exampleError by viewModel.exampleError.collectAsState()

    val isSaving by viewModel.isSaving.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val isEditMode = fallacyId != null

    LaunchedEffect(fallacyId) {
        if (fallacyId != null) {
            viewModel.loadFallacy(fallacyId)
        }
    }

    // Show error snackbar if any
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show snackbar or toast
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(if (isEditMode) R.string.fallacy_form_title_edit else R.string.fallacy_form_title_new),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.accessibility_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(R.string.fallacy_form_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(R.string.fallacy_field_name)) },
                    placeholder = { Text(stringResource(R.string.fallacy_field_name_placeholder)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text(stringResource(R.string.fallacy_field_description)) },
                    placeholder = { Text(stringResource(R.string.fallacy_field_description_placeholder)) },
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Example field
                OutlinedTextField(
                    value = example,
                    onValueChange = viewModel::onExampleChange,
                    label = { Text(stringResource(R.string.fallacy_field_example)) },
                    placeholder = { Text(stringResource(R.string.fallacy_field_example_placeholder)) },
                    isError = exampleError != null,
                    supportingText = exampleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Category field (optional)
                OutlinedTextField(
                    value = category,
                    onValueChange = viewModel::onCategoryChange,
                    label = { Text(stringResource(R.string.fallacy_field_category)) },
                    placeholder = { Text(stringResource(R.string.fallacy_field_category_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = { viewModel.saveFallacy(onSuccess = onNavigateBack) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(if (isEditMode) R.string.fallacy_button_save else R.string.fallacy_button_create))
                }

                // Cancel button
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
