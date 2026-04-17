package com.tubes.nimons360.ui.families

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.tubes.nimons360.data.local.PinnedFamilyEntity
import com.tubes.nimons360.model.FamilyItem
import com.tubes.nimons360.ui.theme.BluePrimary
import com.tubes.nimons360.ui.theme.Charcoal
import com.tubes.nimons360.ui.theme.GrayLight
import com.tubes.nimons360.ui.theme.OrangePrimary

@Composable
fun FamiliesScreen(
    viewModel: FamiliesViewModel,
    onFamilyClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pinnedFamilies by viewModel.pinnedFamilies.collectAsState()
    val displayedFamilies by viewModel.displayedFamilies.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Cari keluarga...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterMode == FilterMode.ALL,
                onClick = { viewModel.setFilter(FilterMode.ALL) },
                label = { Text("Semua") }
            )
            FilterChip(
                selected = filterMode == FilterMode.MY_FAMILIES,
                onClick = { viewModel.setFilter(FilterMode.MY_FAMILIES) },
                label = { Text("Keluargaku") }
            )
        }

        when (val state = uiState) {
            is FamiliesUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            is FamiliesUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Charcoal)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadData() },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            is FamiliesUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Pinned section
                    if (pinnedFamilies.isNotEmpty()) {
                        item {
                            Text(
                                text = "Ditandai",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(pinnedFamilies) { pinned ->
                                    PinnedFamilyChip(
                                        pinned = pinned,
                                        onClick = { onFamilyClick(pinned.familyId) },
                                        onUnpin = { viewModel.unpinFamily(pinned.familyId) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = GrayLight)
                        }
                    }

                    // Main list header
                    item {
                        Text(
                            text = if (filterMode == FilterMode.ALL) "Semua Keluarga" else "Keluargaku",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
                        )
                    }

                    if (displayedFamilies.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Tidak ada keluarga ditemukan",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(displayedFamilies) { family ->
                            val isPinned = pinnedFamilies.any { it.familyId == family.id }
                            FamilyListItem(
                                family = family,
                                isPinned = isPinned,
                                onClick = { onFamilyClick(family.id) },
                                onPinToggle = {
                                    if (isPinned) viewModel.unpinFamily(family.id)
                                    else viewModel.pinFamily(family)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinnedFamilyChip(
    pinned: PinnedFamilyEntity,
    onClick: () -> Unit,
    onUnpin: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GrayLight)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pinned.iconUrl,
                contentDescription = pinned.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = pinned.name,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Charcoal
            )
            IconButton(onClick = onUnpin, modifier = Modifier.size(20.dp)) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = "Hapus tanda",
                    tint = OrangePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FamilyListItem(
    family: FamilyItem,
    isPinned: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrayLight)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = family.iconUrl,
                contentDescription = family.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = family.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Charcoal,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onPinToggle) {
                Icon(
                    if (isPinned) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isPinned) "Hapus tanda" else "Tandai",
                    tint = if (isPinned) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
