package com.example.ui.hub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class RideOption(
    val id: String,
    val name: String,
    val eta: String,
    val price: String,
    val capacity: Int,
    val desc: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun UberRidesScreen() {
    var pickupAddress by remember { mutableStateOf("📍 Current Location (Grand Central Ave)") }
    var destinationAddress by remember { mutableStateOf("🎯 METOU Innovation Tower, Downtown") }
    var selectedRideId by remember { mutableStateOf("uberx") }
    var isBookingRide by remember { mutableStateOf(false) }
    var isRideActive by remember { mutableStateOf(false) }
    var driverEtaMinutes by remember { mutableIntStateOf(3) }

    val rideOptions = listOf(
        RideOption("uberx", "METOU UberX", "3 mins away", "$14.50", 4, "Affordable everyday rides", Icons.Default.DirectionsCar, PrimaryNeon),
        RideOption("comfort", "METOU Comfort", "5 mins away", "$21.80", 4, "Newer cars with extra legroom", Icons.Default.AirportShuttle, TertiaryCyan),
        RideOption("black", "METOU Black VIP", "2 mins away", "$38.00", 4, "Luxury rides with top-rated drivers", Icons.Default.Star, RomanticRed),
        RideOption("moto", "METOU Moto Fast", "1 min away", "$7.50", 1, "Quick zip through city traffic", Icons.Default.TwoWheeler, WarningAmber)
    )

    LaunchedEffect(isBookingRide) {
        if (isBookingRide) {
            delay(2000L)
            isBookingRide = false
            isRideActive = true
        }
    }

    LaunchedEffect(isRideActive) {
        if (isRideActive) {
            while (driverEtaMinutes > 1) {
                delay(4000L)
                driverEtaMinutes--
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Location Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WHERE TO GO?", color = PrimaryLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Pickup Field
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = pickupAddress,
                            onValueChange = { pickupAddress = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryNeon,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Destination Field
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(RomanticRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = destinationAddress,
                            onValueChange = { destinationAddress = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryNeon,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        }

        if (isRideActive) {
            // Live Driver Tracking Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("DRIVER ARRIVING IN", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("$driverEtaMinutes MINS", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = SuccessGreen.copy(alpha = 0.2f)
                            ) {
                                Text("ON THE WAY", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Driver Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryNeon),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Carlos Mendez", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Tesla Model Y • White • Plate #7MTC92", color = TextSecondary, fontSize = 12.sp)
                                Text("⭐ 4.98 Rating (2,410 rides)", color = WarningAmber, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, "Call", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Driver", color = TextPrimary, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    isRideActive = false
                                    driverEtaMinutes = 3
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RomanticRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel Ride", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Vehicle Tier Selector
            item {
                Text("CHOOSE YOUR RIDE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            items(rideOptions) { opt ->
                val isSelected = selectedRideId == opt.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRideId = opt.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, opt.color) else androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(opt.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(opt.icon, contentDescription = opt.name, tint = opt.color, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(opt.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Person, "Capacity", tint = TextMuted, modifier = Modifier.size(12.dp))
                                Text("${opt.capacity}", color = TextMuted, fontSize = 11.sp)
                            }
                            Text("${opt.eta} • ${opt.desc}", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(opt.price, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text("Est. fare", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { isBookingRide = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_request_uber"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isBookingRide
                ) {
                    if (isBookingRide) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Finding Nearby Driver...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.DirectionsCar, "Request", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm ${rideOptions.first { it.id == selectedRideId }.name}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
