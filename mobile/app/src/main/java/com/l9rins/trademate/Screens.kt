package com.l9rins.trademate

// --- IMPORTS ---
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l9rins.trademate.data.Client
import com.l9rins.trademate.ui.theme.*
import java.io.File
// Important for SearchTextField:
import androidx.compose.foundation.text.BasicTextField

// --- DASHBOARD (Live Data) ---
@Composable
fun DashboardScreen(viewModel: MainViewModel = viewModel()) {
    var showSettings by remember { mutableStateOf(false) }
    val activeCount by viewModel.statsActive.collectAsState()
    val pendingCount by viewModel.statsPending.collectAsState()
    val completedCount by viewModel.statsCompleted.collectAsState()
    val revenue by viewModel.statsRevenue.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(horizontal = 24.dp), contentPadding = PaddingValues(top = 60.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(text = "WORKSPACE VITALITY", fontSize = 11.sp, color = TradeMateTeal, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp); Spacer(modifier = Modifier.height(8.dp)); Text(text = buildAnnotatedString { append("WELCOME, "); withStyle(style = SpanStyle(color = TradeMateGreen)) { append("LORENZ") } }, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp, color = TextPrimary) }
                    IconButton(onClick = { showSettings = true }, modifier = Modifier.background(SurfaceWhite, CircleShape).shadow(2.dp, CircleShape)) { Icon(Icons.Default.Settings, null, tint = TextSecondary) }
                }
            }
            item { AnalyticsChartCard() }
            item { Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { StatCard(Modifier.weight(1f), Icons.Outlined.WorkOutline, "ACTIVE JOBS", "$activeCount", "LIVE", true); StatCard(Modifier.weight(1f), Icons.Default.Schedule, "PENDING", "$pendingCount", "WAITING", false) } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { StatCard(Modifier.weight(1f), Icons.Outlined.MonetizationOn, "REVENUE", "$${String.format("%.0f", revenue)}", "PAID", true); StatCard(Modifier.weight(1f), Icons.Outlined.CheckCircle, "COMPLETED", "$completedCount", "DONE", true) } }
        }
        if (showSettings) SettingsDialog(onDismiss = { showSettings = false })
    }
}

// --- JOB SCREEN (With Camera Logic) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(viewModel: MainViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    val clients by viewModel.clientList.collectAsState()
    var showAddJob by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Camera Handlers
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var jobToUpdate by remember { mutableStateOf<com.l9rins.trademate.data.Job?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && jobToUpdate != null && tempPhotoUri != null) {
            viewModel.updateJob(jobToUpdate!!.copy(photoUri = tempPhotoUri.toString()))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 60.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("REVENUE PIPELINE", fontSize = 11.sp, color = TradeMateTeal, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("JOB BOARD", fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp, color = TextPrimary)
            }

            item {
                Button(
                    onClick = { showAddJob = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = TradeMateGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("New Contract", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            if (jobs.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No active jobs.", color = TextSecondary) } }
            } else {
                items(count = jobs.size, key = { jobs[it].id }) { index ->
                    val job = jobs[index]
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { viewModel.deleteJob(job); true } else false }
                    )
                    SwipeToDismissBox(state = dismissState, backgroundContent = { SwipeToDeleteBackground(dismissState) }) {
                        JobItem(
                            title = job.title,
                            clientName = job.clientName,
                            price = job.price,
                            status = job.status,
                            photoUri = job.photoUri,
                            onCameraClick = {
                                val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                tempPhotoUri = uri
                                jobToUpdate = job
                                cameraLauncher.launch(uri)
                            },
                            onCalendarClick = { viewModel.addToCalendar(context, job) },
                            onInvoiceClick = { viewModel.shareInvoice(context, job) },
                            onPhotoClick = { }
                        )
                    }
                }
            }
        }

        if (showAddJob) {
            AddJobDialog(
                clients = clients,
                onDismiss = { showAddJob = false },
                onConfirm = { t, c, p, s -> viewModel.addJob(t, c, p, s); showAddJob = false }
            )
        }
    }
}

// --- DIRECTORY SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(titleMain: String, titleHighlight: String, subtitle: String, buttonText: String, viewModel: MainViewModel = viewModel()) {
    val clients by viewModel.filteredClients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        LazyColumn(modifier = Modifier.padding(horizontal = 24.dp), contentPadding = PaddingValues(top = 60.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Column { Text(subtitle.uppercase(), fontSize = 11.sp, color = TradeMateTeal, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp); Spacer(modifier = Modifier.height(8.dp)); Text(buildAnnotatedString { append(titleMain + " "); withStyle(SpanStyle(color = TradeMateGreen)) { append(titleHighlight) } }, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp, color = TextPrimary) } }
            item { Row(verticalAlignment = Alignment.CenterVertically) { Card(modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(2.dp)) { Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null, tint = TextSecondary); Spacer(modifier = Modifier.width(12.dp)); SearchTextField(value = searchQuery, onValueChange = { viewModel.searchQuery.value = it }, modifier = Modifier.weight(1f)) { if (searchQuery.isEmpty()) Text("Search...", color = TextSecondary.copy(alpha = 0.5f)); it() } } }; Spacer(modifier = Modifier.width(12.dp)); Card(modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = TradeMateGreen), elevation = CardDefaults.cardElevation(4.dp)) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Tune, null, tint = Color.White) } } } }
            item { Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = TextPrimary), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, null, tint = TradeMateGreen); Spacer(modifier = Modifier.width(12.dp)); Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) } }
            if (clients.isEmpty()) { item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No records.", color = TextSecondary) } } } else { items(count = clients.size, key = { clients[it].id }) { index -> val client = clients[index]; val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { viewModel.deleteClient(client); true } else false }); SwipeToDismissBox(state = dismissState, backgroundContent = { SwipeToDeleteBackground(dismissState) }) { DirectoryItem(title = client.name, subtitle = client.email, initials = client.name, status = "Active", onCallClick = { viewModel.makeCall(client.phone) }) } } }
        }
        if (showAddDialog) { AddClientDialog({ showAddDialog = false }, { n, e, p -> viewModel.addClient(n, e, p); showAddDialog = false }) }
    }
}

// --- HELPER COMPOSABLES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobDialog(clients: List<Client>, onDismiss: () -> Unit, onConfirm: (String, String, Double, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf("") }
    val statuses = listOf("Pending", "Active", "Paid")
    var selectedStatus by remember { mutableStateOf("Active") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("New Contract", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = selectedClient, onValueChange = {}, label = { Text("Select Client") }, modifier = Modifier.fillMaxWidth(), readOnly = true, trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } })
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(SurfaceWhite)) {
                        if (clients.isEmpty()) { DropdownMenuItem(text = { Text("No clients yet.") }, onClick = { expanded = false }) }
                        else { clients.forEach { client -> DropdownMenuItem(text = { Text(client.name) }, onClick = { selectedClient = client.name; expanded = false }) } }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price ($)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    statuses.forEach { status -> FilterChip(selected = status == selectedStatus, onClick = { selectedStatus = status }, label = { Text(status) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TradeMateGreen.copy(alpha=0.2f), selectedLabelColor = TradeMateGreen)) }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { if (title.isNotBlank() && price.isNotBlank() && selectedClient.isNotBlank()) { onConfirm(title, selectedClient, price.toDoubleOrNull() ?: 0.0, selectedStatus) } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TradeMateGreen)) { Text("Create Contract", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Preferences feature coming soon!", color = TextSecondary)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TradeMateGreen)) { Text("Close", color = Color.White) }
            }
        }
    }
}

// Ensure you import androidx.compose.foundation.text.BasicTextField
@Composable
fun SearchTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit) {
    BasicTextField(value = value, onValueChange = onValueChange, modifier = modifier, singleLine = true, decorationBox = decorationBox, textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 16.sp))
}

@Composable
fun AddClientDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("New Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (name.isNotBlank()) onConfirm(name, email, phone) }, colors = ButtonDefaults.buttonColors(containerColor = TradeMateGreen)) { Text("Create Profile", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}