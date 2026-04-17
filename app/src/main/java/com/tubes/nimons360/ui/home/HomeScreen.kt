package com.tubes.nimons360.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.tubes.nimons360.model.DiscoverFamily
import com.tubes.nimons360.model.MyFamily
import com.tubes.nimons360.ui.theme.BluePrimary
import com.tubes.nimons360.ui.theme.Charcoal
import com.tubes.nimons360.ui.theme.GrayLight

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onFamilyClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        }
        is HomeUiState.Error -> {
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
        is HomeUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    SectionHeader("Keluargaku")
                    if (state.myFamilies.isEmpty()) {
                        EmptyState("Kamu belum bergabung ke keluarga manapun.")
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.myFamilies) { family ->
                                MyFamilyCard(family = family, onClick = { onFamilyClick(family.id) })
                            }
                        }
                    }
                }
                item {
                    SectionHeader("Temukan Keluarga")
                    if (state.discoverFamilies.isEmpty()) {
                        EmptyState("Tidak ada keluarga baru untuk ditemukan.")
                    }
                }
                items(state.discoverFamilies) { family ->
                    DiscoverFamilyItem(family = family, onClick = { onFamilyClick(family.id) })
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Charcoal,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp)
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MyFamilyCard(family: MyFamily, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrayLight)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = family.iconUrl,
                contentDescription = family.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = family.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${family.members.size} anggota",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscoverFamilyItem(family: DiscoverFamily, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = family.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Charcoal
                )
                Text(
                    text = "${family.members.size} anggota",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Lihat",
                color = BluePrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
