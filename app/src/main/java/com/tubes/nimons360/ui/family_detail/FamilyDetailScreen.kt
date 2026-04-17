package com.tubes.nimons360.ui.family_detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.tubes.nimons360.model.FamilyMember
import com.tubes.nimons360.ui.theme.BluePrimary
import com.tubes.nimons360.ui.theme.Charcoal
import com.tubes.nimons360.ui.theme.GrayLight
import com.tubes.nimons360.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    viewModel: FamilyDetailViewModel,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val joinState by viewModel.joinState.collectAsState()
    val leaveState by viewModel.leaveState.collectAsState()
    val context = LocalContext.current

    var showJoinDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }

    // Handle join/leave state changes
    LaunchedEffect(joinState) {
        if (joinState is ActionState.Success) {
            showJoinDialog = false
            joinCode = ""
            viewModel.resetJoinState()
        }
    }
    LaunchedEffect(leaveState) {
        if (leaveState is ActionState.Success) {
            showLeaveDialog = false
            viewModel.resetLeaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Keluarga") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (uiState is FamilyDetailUiState.Success) {
                        val pinned = (uiState as FamilyDetailUiState.Success).isPinned
                        IconButton(onClick = { viewModel.togglePin() }) {
                            Icon(
                                if (pinned) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (pinned) "Hapus tanda" else "Tandai",
                                tint = if (pinned) OrangePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is FamilyDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            is FamilyDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Charcoal)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadDetail() },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            is FamilyDetailUiState.Success -> {
                val family = state.family
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        AsyncImage(
                            model = family.iconUrl,
                            contentDescription = family.name,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = family.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                        Text(
                            text = "${family.members.size} anggota",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Family code (only for members)
                    if (family.isMember && !family.familyCode.isNullOrBlank()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GrayLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Kode Keluarga",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            family.familyCode!!,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary,
                                            letterSpacing = 4.sp
                                        )
                                    }
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("family_code", family.familyCode))
                                    }) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Salin kode", tint = BluePrimary)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // Action buttons
                    item {
                        if (family.isMember) {
                            Button(
                                onClick = { showLeaveDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (leaveState is ActionState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Keluar dari Keluarga", fontSize = 16.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = { showJoinDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (joinState is ActionState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Gabung Keluarga", fontSize = 16.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // Error messages
                    if (joinState is ActionState.Error) {
                        item {
                            Text(
                                text = (joinState as ActionState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    if (leaveState is ActionState.Error) {
                        item {
                            Text(
                                text = (leaveState as ActionState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Members header
                    item {
                        Text(
                            text = "Anggota",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = GrayLight
                        )
                    }

                    items(family.members) { member ->
                        MemberRow(member)
                    }
                }
            }
        }
    }

    // Join dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false; joinCode = "" },
            title = { Text("Masukkan Kode Keluarga") },
            text = {
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { if (it.length <= 6) joinCode = it },
                    label = { Text("Kode (6 karakter)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (joinCode.length == 6) viewModel.join(joinCode)
                    },
                    enabled = joinCode.length == 6 && joinState !is ActionState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) { Text("Gabung") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false; joinCode = "" }) { Text("Batal") }
            }
        )
    }

    // Leave dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Keluar dari Keluarga") },
            text = { Text("Apakah kamu yakin ingin keluar dari keluarga ini?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.leave() },
                    enabled = leaveState !is ActionState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) { Text("Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun MemberRow(member: FamilyMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar initial
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(20.dp),
            color = BluePrimary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = member.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = member.fullName,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Charcoal
            )
            Text(
                text = member.email,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 68.dp, end = 16.dp), color = GrayLight)
}
