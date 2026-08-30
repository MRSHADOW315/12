package com.example.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ShopProduct(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val originalPrice: Double,
    val rating: Double,
    val reviewsCount: Int,
    val isPrime: Boolean,
    val badge: String,
    val colorGradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingMarketplaceScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Deals") }
    var cartItemsCount by remember { mutableIntStateOf(2) }
    var cartTotal by remember { mutableDoubleStateOf(169.98) }
    var showCartDialog by remember { mutableStateOf(false) }
    var checkoutCelebration by remember { mutableStateOf(false) }

    val categories = listOf("All Deals", "Electronics", "Fashion", "Gaming Gear", "METOU Originals", "Smart Home")

    val products = remember {
        listOf(
            ShopProduct(
                id = "p1",
                title = "METOU Neural AR Smart Glasses (2026 Pro)",
                category = "Electronics",
                price = 299.99,
                originalPrice = 399.99,
                rating = 4.9,
                reviewsCount = 1420,
                isPrime = true,
                badge = "Save 25%",
                colorGradient = listOf(RomanticRed, PrimaryNeon)
            ),
            ShopProduct(
                id = "p2",
                title = "Cyberpunk RGB Mechanical Gaming Keyboard",
                category = "Gaming Gear",
                price = 89.99,
                originalPrice = 119.99,
                rating = 4.8,
                reviewsCount = 850,
                isPrime = true,
                badge = "Amazon Choice",
                colorGradient = listOf(TertiaryCyan, GhostModePurple)
            ),
            ShopProduct(
                id = "p3",
                title = "Spatial Audio Wireless Noise-Cancelling Headphones",
                category = "Electronics",
                price = 149.99,
                originalPrice = 199.99,
                rating = 4.7,
                reviewsCount = 3120,
                isPrime = true,
                badge = "Prime 1-Day",
                colorGradient = listOf(PrimaryNeon, WarningAmber)
            ),
            ShopProduct(
                id = "p4",
                title = "METOU Holographic Reflective Streetwear Hoodie",
                category = "Fashion",
                price = 64.99,
                originalPrice = 79.99,
                rating = 4.9,
                reviewsCount = 540,
                isPrime = false,
                badge = "Limited Edition",
                colorGradient = listOf(RomanticRed, DarkSurfaceVariant)
            ),
            ShopProduct(
                id = "p5",
                title = "Ultra-Fast 100W GaN Desktop Charger 6-Port",
                category = "Smart Home",
                price = 39.99,
                originalPrice = 49.99,
                rating = 4.6,
                reviewsCount = 920,
                isPrime = true,
                badge = "Best Seller",
                colorGradient = listOf(TertiaryCyan, PrimaryNeon)
            ),
            ShopProduct(
                id = "p6",
                title = "4K OLED Curved Portable Monitor 144Hz",
                category = "Gaming Gear",
                price = 219.99,
                originalPrice = 279.99,
                rating = 4.8,
                reviewsCount = 760,
                isPrime = true,
                badge = "Deal of the Day",
                colorGradient = listOf(GhostModePurple, RomanticRed)
            )
        )
    }

    val filteredProducts = products.filter {
        (selectedCategory == "All Deals" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Amazon / METOU Shop Search Bar & Cart
        Surface(
            color = DarkSurface,
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("shop_search_input"),
                        placeholder = { Text("Search Amazon & METOU Store...", color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, "Search", tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNeon,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Shopping Cart with badge
                    Box(
                        modifier = Modifier
                            .clickable { showCartDialog = true }
                            .padding(4.dp)
                    ) {
                        IconButton(onClick = { showCartDialog = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = PrimaryLight)
                        }
                        if (cartItemsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(RomanticRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$cartItemsCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) WarningAmber else DarkSurfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Product Catalog
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Prime Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, "Prime", tint = WarningAmber, modifier = Modifier.size(18.dp))
                                Text("METOU PRIME EXPRESS", color = WarningAmber, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Free Same-Day Delivery on 10,000+ items with METOU Coins", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(filteredProducts) { prod ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        // Product Image Placeholder
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(prod.colorGradient)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = prod.title,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(36.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                                    .background(RomanticRed, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(prod.badge, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Product Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "Rating", tint = WarningAmber, modifier = Modifier.size(14.dp))
                                Text(" ${prod.rating} ", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("(${prod.reviewsCount})", color = TextMuted, fontSize = 11.sp)
                                if (prod.isPrime) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = PrimaryNeon.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("PRIME", color = PrimaryLight, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$${prod.price}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$${prod.originalPrice}", color = TextMuted, fontSize = 12.sp, textDecoration = TextDecoration.LineThrough)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        cartItemsCount++
                                        cartTotal += prod.price
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, "Add", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to Cart", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        checkoutCelebration = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber)
                                ) {
                                    Text("Buy Now", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Checkout Celebration Dialog
    if (checkoutCelebration) {
        AlertDialog(
            onDismissRequest = { checkoutCelebration = false },
            title = { Text("🎉 Order Placed Successfully!", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your package has been dispatched via METOU Prime Express! Tracking details and coin cashback have been credited to your account.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { checkoutCelebration = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("Track Order", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Cart Dialog
    if (showCartDialog) {
        AlertDialog(
            onDismissRequest = { showCartDialog = false },
            title = { Text("🛍️ Your Shopping Cart ($cartItemsCount items)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Subtotal: $${String.format("%.2f", cartTotal)}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Prime Shipping: FREE ($0.00)", color = SuccessGreen, fontSize = 12.sp)
                    Text("Pay with: METOU Coins or Credit Card", color = PrimaryLight, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Items will be delivered in 1-2 business days with live GPS tracking.", color = TextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCartDialog = false
                        checkoutCelebration = true
                        cartItemsCount = 0
                        cartTotal = 0.0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                ) {
                    Text("Proceed to Checkout", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCartDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
