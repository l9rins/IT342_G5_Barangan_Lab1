package com.l9rins.trademate

// --- IMPORTS ARE CRITICAL ---
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.l9rins.trademate.ui.theme.*

// 1. Dashboard Stat Card
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    trend: String? = null,
    isTrendPositive: Boolean = true
) {
    Card(
        modifier = modifier.aspectRatio(1.3f),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(BackgroundLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = TradeMateTeal, modifier = Modifier.size(20.dp))
                }
                if (trend != null) {
                    Text(
                        text = trend,
                        color = if (isTrendPositive) TradeMateGreen else Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.background(
                            if (isTrendPositive) TradeMateGreen.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        ).padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column {
                Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
        }
    }
}

// 2. Client Directory Item
@Composable
fun DirectoryItem(
    title: String,
    subtitle: String,
    status: String? = null,
    initials: String,
    onCallClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), clip = false),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(TradeMateGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Text(text = initials.take(2).uppercase(), color = TradeMateGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(subtitle, fontSize = 13.sp, color = TextSecondary)
            }
            IconButton(onClick = onCallClick, modifier = Modifier.size(36.dp).background(BackgroundLight, CircleShape)) {
                Icon(Icons.Default.Phone, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// 3. Analytics Chart (THE ONE THAT WAS CRASHING)
@Composable
fun AnalyticsChartCard() {
    Card(modifier = Modifier.fillMaxWidth().height(220.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column { Text("ANALYTICS ENGINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Professional Performance", fontSize = 10.sp, color = TextSecondary) }
                Box(modifier = Modifier.background(TradeMateGreen.copy(alpha = 0.1f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("LIVE", fontSize = 10.sp, color = TradeMateGreen, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // This is the Canvas block that needs the imports above
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val path = Path().apply {
                    moveTo(0f, height * 0.7f)
                    cubicTo(width * 0.2f, height * 0.9f, width * 0.3f, height * 0.8f, width * 0.4f, height * 0.5f)
                    cubicTo(width * 0.5f, height * 0.2f, width * 0.7f, height * 0.2f, width * 0.8f, height * 0.4f)
                    cubicTo(width * 0.9f, height * 0.6f, width * 0.95f, height * 0.5f, width, height * 0.45f)
                }

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(TradeMateTeal.copy(alpha = 0.3f), TradeMateTeal.copy(alpha = 0.0f))))
                drawPath(path = path, color = TradeMateTeal, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

// 4. Swipe Background
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteBackground(dismissState: SwipeToDismissBoxState) {
    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red.copy(alpha = 0.9f) else Color.Transparent
    Box(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
        Icon(Icons.Default.Delete, "Delete", tint = Color.White)
    }
}

// 5. SUPER JOB ITEM
@Composable
fun JobItem(
    title: String,
    clientName: String,
    price: Double,
    status: String,
    photoUri: String?,
    onCameraClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onInvoiceClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    val statusColor = when (status) {
        "Paid" -> TradeMateGreen
        "Active" -> TradeMateTeal
        else -> Color(0xFFFFA500)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp), clip = false),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // INFO ROW
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(40.dp).background(statusColor, CircleShape))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(clientName, fontSize = 13.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${String.format("%.0f", price)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(status.uppercase(), fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TOOLBAR ROW
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA)).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Button
                if (photoUri != null) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).clickable { onPhotoClick() }) {
                        Image(painter = rememberAsyncImagePainter(photoUri), contentDescription = "Proof", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                } else {
                    IconButton(onClick = onCameraClick) { Icon(Icons.Outlined.CameraAlt, "Add Photo", tint = TextSecondary) }
                }

                // Calendar Button
                IconButton(onClick = onCalendarClick) { Icon(Icons.Outlined.Event, "Sync Calendar", tint = TradeMateTeal) }

                // Invoice Button
                Button(
                    onClick = onInvoiceClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("INVOICE", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}