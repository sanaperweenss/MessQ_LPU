package com.example.messqlpu.presentation.screens.dining

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import java.util.Calendar
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.messqlpu.domain.model.DiningHall
import com.example.messqlpu.domain.model.FoodItem
import com.example.messqlpu.presentation.components.MessQButton
import com.example.messqlpu.presentation.components.ModernCard
import com.example.messqlpu.presentation.viewmodel.DiningDetailViewModel
import com.example.messqlpu.ui.theme.AccentGold
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.PrimaryPurpleBg
import com.example.messqlpu.ui.theme.SuccessGreen

@Composable
fun DiningDetailScreen(
    hallId: String,
    onBackClick: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel = remember(hallId) { DiningDetailViewModel(hallId) }
    val hall by viewModel.diningHall.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val reviews = viewModel.reviews

    val tabs = listOf("Overview", "Menu", "Reviews", "Live Stats")

    var showReservationDialog by remember { mutableStateOf(false) }
    var bookedReservation by remember { mutableStateOf<com.example.messqlpu.domain.model.TableReservation?>(null) }
    var resDate by remember { mutableStateOf("Today") }
    var resTimeSlot by remember { mutableStateOf("12:30 PM") }
    var resPartySize by remember { mutableIntStateOf(2) }
    var resSeating by remember { mutableStateOf("AC Indoor") }

    if (hall == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryPurple)
        }
        return
    }

    val currentHall = hall!!

    val calendar = Calendar.getInstance()
    val customReservationTimePicker = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                val format = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                resTimeSlot = format.format(cal.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    if (showReservationDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            onDismissRequest = { showReservationDialog = false },
            title = {
                Text("🪑 Reserve a Table", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Date Selection
                    Text("Select Date", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Today", "Tomorrow")) { d ->
                            FilterChip(
                                selected = resDate == d,
                                onClick = { resDate = d },
                                label = { Text(d, maxLines = 1) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Time Slot Selection
                    Text("Select Time Slot", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    val slotList = listOf(
                        "12:30 PM" to "🟢 6 Free",
                        "1:15 PM" to "🟢 4 Free",
                        "2:00 PM" to "🔴 Full",
                        "7:30 PM" to "🟢 8 Free",
                        "8:15 PM" to "🟢 5 Free"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(slotList) { (time, status) ->
                            val isFull = status.contains("Full")
                            FilterChip(
                                selected = resTimeSlot == time && !isFull,
                                enabled = !isFull,
                                onClick = { if (!isFull) resTimeSlot = time },
                                label = {
                                    Text(
                                        text = "$time ($status)",
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (resTimeSlot == time) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryPurple.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { customReservationTimePicker.show() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (slotList.none { it.first == resTimeSlot }) "⏰ $resTimeSlot" else "⏰ Custom Time...",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Party Size Selection
                    Text("Party Size (Guests)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(1, 2, 4, 6, 8)) { size ->
                            FilterChip(
                                selected = resPartySize == size,
                                onClick = { resPartySize = size },
                                label = { Text("$size ${if (size == 1) "Guest" else "Guests"}", maxLines = 1) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Seating Preference Selection
                    Text("Seating Preference", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("AC Indoor", "Window View", "Quiet Corner")) { pref ->
                            FilterChip(
                                selected = resSeating == pref,
                                onClick = { resSeating = pref },
                                label = { Text(pref, maxLines = 1) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bookTable(resDate, resTimeSlot, resPartySize, resSeating) { res ->
                            showReservationDialog = false
                            bookedReservation = res
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Confirm Reservation", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReservationDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (bookedReservation != null) {
        val res = bookedReservation!!
        AlertDialog(
            onDismissRequest = { bookedReservation = null },
            title = {
                Text("🎉 Table Reserved!", fontWeight = FontWeight.Bold, color = SuccessGreen)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Booking ID: #${res.reservationId}", fontWeight = FontWeight.Bold)
                    Text("Location: ${res.diningHallName}")
                    Text("Date & Time: ${res.date} at ${res.timeSlot}")
                    Text("Guests: ${res.partySize} | Preference: ${res.seatingPreference}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Show this pass at the entrance counter on arrival.",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { bookedReservation = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got It")
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MessQButton(
                        text = "🎟️ Join Queue",
                        onClick = { viewModel.joinQueue(onNavigateToQueue) },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = { showReservationDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple)
                    ) {
                        Text(
                            text = "🪑 Reserve Table",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // HERO IMAGE with Back & Share buttons
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = currentHall.imageUrl,
                        contentDescription = currentHall.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out ${currentHall.name} on MessQ LPU! Current wait time: ${currentHall.waitTimeMinutes} mins.")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share ${currentHall.name}"))
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // DETAILS HEADER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = currentHall.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Low Crowd pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentHall.crowdLevel.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${currentHall.rating} (${currentHall.reviewCount} reviews)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Distance
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = currentHall.distanceDisplay,
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 TABS: Overview, Menu, Reviews, Live Stats
                    SecondaryScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = PrimaryPurple,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTab),
                                color = PrimaryPurple,
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, tabTitle ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { viewModel.setTab(index) },
                                text = {
                                    Text(
                                        text = tabTitle,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // TAB CONTENT
            when (selectedTab) {
                0 -> { // OVERVIEW
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 3 Badges: Hygienic, Spacious, Affordable
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Hygienic", "Spacious", "Affordable").forEach { badge ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PrimaryPurpleBg,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badge,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PrimaryPurple,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            // About
                            Column {
                                Text(
                                    text = "About",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentHall.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 20.sp
                                    )
                                )
                            }

                            // Timings
                            Column {
                                Text(
                                    text = "Timings",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentHall.openingHours,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }

                            // Facilities (Indoor Seating, AC Hall, Drinking Water, Card Payment)
                            Column {
                                Text(
                                    text = "Facilities",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currentHall.facilities.forEach { facility ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = when {
                                                        facility.contains("Seating", true) -> Icons.Default.Chair
                                                        facility.contains("AC", true) -> Icons.Default.AcUnit
                                                        facility.contains("Water", true) -> Icons.Default.WaterDrop
                                                        else -> Icons.Default.CreditCard
                                                    },
                                                    contentDescription = null,
                                                    tint = PrimaryPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = when {
                                                        facility.contains("Seating", true) -> "Indoor"
                                                        facility.contains("AC", true) -> "AC Hall"
                                                        facility.contains("Water", true) -> "Drinking"
                                                        else -> "Card"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }

                1 -> { // MENU
                    items(menuItems) { food ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                            ModernCard(onClick = { onNavigateToFoodDetail(food.id) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = food.imageUrl,
                                        contentDescription = food.name,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = food.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(text = food.description, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant), maxLines = 1)
                                        Text(text = "₹${food.price.toInt()}", style = MaterialTheme.typography.titleSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold))
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryPurple)
                                }
                            }
                        }
                    }
                }

                2 -> { // REVIEWS
                    items(reviews) { review ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                            ModernCard {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = review.authorName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(text = review.date, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(review.rating.toInt()) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = review.comment, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                    }
                }

                3 -> { // LIVE STATS
                    item {
                        Column(modifier = Modifier.padding(20.dp)) {
                            ModernCard {
                                Text(text = "Real-Time Counter Velocity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "3 active dispensing counters currently operational. Average student service speed is 38 seconds per token.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { currentHall.occupancyPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = SuccessGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Current Hall Occupancy: ${currentHall.occupancyPercent}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}
