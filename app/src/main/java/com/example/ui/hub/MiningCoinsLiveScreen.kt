package com.example.ui.hub

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

data class MinedBlock(
    val blockNumber: Long,
    val hash: String,
    val reward: String,
    val timeAgo: String
)

@Composable
fun MiningCoinsLiveScreen() {
    // Starting balance with high precision
    var totalMinedCoins by remember { mutableDoubleStateOf(0.00142850) }
    var miningRatePerSec by remember { mutableDoubleStateOf(0.00000010) } // 0.0000001 in sec!
    var isMiningActive by remember { mutableStateOf(true) }
    var isOverclocked by remember { mutableStateOf(false) }
    var overclockTimer by remember { mutableIntStateOf(0) }
    var claimedToWalletCount by remember { mutableDoubleStateOf(0.0) }
    var showClaimSuccess by remember { mutableStateOf(false) }

    val recentBlocks = remember {
        mutableStateListOf(
            MinedBlock(1894210, "0x8F4A...B92C", "+0.00000010 METOU", "Just now"),
            MinedBlock(1894209, "0x3D11...EE42", "+0.00000010 METOU", "2s ago"),
            MinedBlock(1894208, "0x9A77...66B1", "+0.00000010 METOU", "4s ago"),
            MinedBlock(1894207, "0xFA02...114C", "+0.00000010 METOU", "6s ago")
        )
    }

    // Live 1-second mining ticker adding precisely 0.0000001 (or overclocked rate) every second
    LaunchedEffect(isMiningActive, isOverclocked) {
        var currentBlockNum = 1894211L
        while (isMiningActive) {
            delay(1000L)
            val currentRate = if (isOverclocked) 0.00000050 else 0.00000010
            totalMinedCoins += currentRate
            
            if (isOverclocked && overclockTimer > 0) {
                overclockTimer--
                if (overclockTimer == 0) {
                    isOverclocked = false
                }
            }

            // Periodically add to live block feed
            if (System.currentTimeMillis() % 3 == 0L) {
                currentBlockNum++
                val randomHex = (1000..9999).random()
                recentBlocks.add(
                    0,
                    MinedBlock(
                        currentBlockNum,
                        "0x${randomHex}...${(1000..9999).random()}",
                        "+${String.format(Locale.US, "%.8f", currentRate)} METOU",
                        "Just now"
                    )
                )
                if (recentBlocks.size > 10) {
                    recentBlocks.removeAt(recentBlocks.lastIndex)
                }
            }
        }
    }

    // Infinite rotation for cooling fan
    val infiniteTransition = rememberInfiniteTransition(label = "fan_rotation")
    val fanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 400 else 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fan_angle"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mining Header & Live Ticker Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOverclocked) RomanticRed else PrimaryNeon.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isMiningActive) SuccessGreen else WarningAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMiningActive) "RIG ACTIVE • 0.0000001 / SEC" else "RIG PAUSED",
                                color = if (isMiningActive) SuccessGreen else WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOverclocked) RomanticRedSoft else DarkSurfaceVariant
                        ) {
                            Text(
                                text = if (isOverclocked) "⚡ TURBO 5X (${overclockTimer}s)" else "STANDARD RIG",
                                color = if (isOverclocked) RomanticRed else PrimaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Huge Coin Display
                    Text(
                        text = "TOTAL MINED REWARD",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.8f", totalMinedCoins),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "METOU COINS (MTC)",
                        color = PrimaryLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fan & GPU Hardware Visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Rotating Fan
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(DarkBackground)
                                    .border(1.dp, PrimaryNeon.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cyclone,
                                    contentDescription = "Mining Fan",
                                    tint = if (isOverclocked) RomanticRed else PrimaryNeon,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .rotate(if (isMiningActive) fanAngle else 0f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("GPU FAN 1", color = TextMuted, fontSize = 9.sp)
                        }

                        // Rig Stats
                        Column {
                            Text("Hashrate: ${if (isOverclocked) "4,210.5" else "842.1"} MH/s", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Algorithm: SHA-256 Metou", color = TextSecondary, fontSize = 11.sp)
                            Text("Power Draw: ${if (isOverclocked) "240W (Turbo)" else "115W"}", color = TextMuted, fontSize = 11.sp)
                        }

                        Column {
                            Text("Efficiency: 99.8%", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Difficulty: 14.8 T", color = TextSecondary, fontSize = 11.sp)
                            Text("Pool: METOU Global", color = PrimaryLight, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Controls: Turbo Boost & Claim
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isOverclocked = true
                                overclockTimer = 60
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_overclock"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOverclocked) RomanticRed.copy(alpha = 0.6f) else RomanticRed
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = "Turbo", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("5x Turbo Boost", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                claimedToWalletCount += totalMinedCoins
                                totalMinedCoins = 0.0
                                showClaimSuccess = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_claim_wallet"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Claim", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Claim Coins", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showClaimSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎉 Successfully transferred ${String.format(Locale.US, "%.8f", claimedToWalletCount)} MTC to your METOU Main Wallet!",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Wallet Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("METOU Vault Wallet", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${String.format(Locale.US, "%.8f", claimedToWalletCount + 25.50000000)} MTC",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text("≈ \$${String.format(Locale.US, "%.2f", (claimedToWalletCount + 25.5) * 14.80)} USD", color = SuccessGreen, fontSize = 11.sp)
                    }

                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Transfer", tint = PrimaryLight)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.QrCode, contentDescription = "Receive", tint = PrimaryLight)
                        }
                    }
                }
            }
        }

        // Live Blockchain Blocks Stream
        item {
            Text(
                text = "LIVE BLOCKCHAIN HASH LEDGER",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }

        items(recentBlocks) { block ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tag, "Block", tint = TertiaryCyan, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Block #${block.blockNumber}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(block.hash, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(block.reward, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(block.timeAgo, color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
