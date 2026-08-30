package com.example.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class RestaurantDish(
    val id: String,
    val restaurantName: String,
    val dishName: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val deliveryTime: String,
    val desc: String,
    val tag: String,
    val colorGradient: List<Color>
)

@Composable
fun FoodDeliveryScreen() {
    var selectedCategory by remember { mutableStateOf("All") }
    var cartItems by remember { mutableStateOf(mapOf<String, Int>()) }
    var orderPlaced by remember { mutableStateOf(false) }

    val categories = listOf("All", "Burgers", "Sushi & Asian", "Pizza", "Healthy Bowls", "Desserts & Boba")

    val dishes = listOf(
        RestaurantDish(
            "f1",
            "Truffle Burger Lab",
            "Double Wagyu Truffle Smash Burger",
            "Burgers",
            16.50,
            4.9,
            "15-20 min",
            "Double aged wagyu patties, black truffle aioli, melted gruyere on toasted brioche.",
            "Free Delivery",
            listOf(RomanticRed, WarningAmber)
        ),
        RestaurantDish(
            "f2",
            "Tokyo Nigiri Master",
            "Omakase Deluxe Sushi Box (12 pcs)",
            "Sushi & Asian",
            24.00,
            4.95,
            "20-30 min",
            "Fresh salmon, bluefin tuna, sea urchin, and wagyu nigiri with real wasabi.",
            "Top Rated",
            listOf(TertiaryCyan, PrimaryNeon)
        ),
        RestaurantDish(
            "f3",
            "Napoli Woodfire Co.",
            "Artisan Burrata & Prosciutto Pizza",
            "Pizza",
            18.20,
            4.8,
            "18-25 min",
            "San Marzano tomato base, fresh pugliese burrata, 24-month prosciutto di Parma.",
            "Popular",
            listOf(WarningAmber, RomanticRed)
        ),
        RestaurantDish(
            "f4",
            "Green Goddess Organics",
            "Avocado Crunch & Salmon Glow Bowl",
            "Healthy Bowls",
            14.90,
            4.7,
            "10-15 min",
            "Wild salmon, Hass avocado, edamame, purple quinoa, tahini ginger dressing.",
            "Superfood",
            listOf(SuccessGreen, TertiaryCyan)
        )
    )

    val totalCost = cartItems.entries.sumOf { (dishId, count) ->
        val dish = dishes.find { it.id == dishId }
        (dish?.price ?: 0.0) * count
    }
    val totalItems = cartItems.values.sum()

    val filteredDishes = if (selectedCategory == "All") dishes else dishes.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Category Filter
        Surface(color = DarkSurface) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("METOU GOURMET EATS", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Surface(shape = RoundedCornerShape(12.dp), color = SuccessGreen.copy(alpha = 0.2f)) {
                        Text("30 MIN GUARANTEE", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                            color = if (isSelected) RomanticRed else DarkSurfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Restaurant Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredDishes) { dish ->
                val quantity = cartItems[dish.id] ?: 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dish.restaurantName, color = PrimaryLight, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                Text(dish.dishName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, "Rating", tint = WarningAmber, modifier = Modifier.size(14.dp))
                                    Text(" ${dish.rating} ", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("• ⏱️ ${dish.deliveryTime}", color = TextMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = SuccessGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text(dish.tag, color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(dish.colorGradient)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Restaurant, "Food", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(30.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(dish.desc, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$${dish.price}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 17.sp)

                            if (quantity == 0) {
                                Button(
                                    onClick = { cartItems = cartItems + (dish.id to 1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RomanticRed),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Add, "Add", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (quantity <= 1) {
                                                cartItems = cartItems - dish.id
                                            } else {
                                                cartItems = cartItems + (dish.id to (quantity - 1))
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, "Less", tint = TextPrimary, modifier = Modifier.size(14.dp))
                                    }

                                    Text("$quantity", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 8.dp))

                                    IconButton(
                                        onClick = { cartItems = cartItems + (dish.id to (quantity + 1)) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "More", tint = RomanticRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Checkout Bar
        if (totalItems > 0) {
            Surface(
                color = DarkSurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("$totalItems item(s) in order", color = TextMuted, fontSize = 11.sp)
                        Text("$${String.format("%.2f", totalCost)}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }

                    Button(
                        onClick = {
                            orderPlaced = true
                            cartItems = emptyMap()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.DeliveryDining, "Deliver", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Place Food Order", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (orderPlaced) {
        AlertDialog(
            onDismissRequest = { orderPlaced = false },
            title = { Text("🛵 Food Order Dispatched!", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your gourmet food is being freshly prepared by the master chef. Estimated delivery time: 18 minutes by METOU Express Courier!",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { orderPlaced = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
    }
}
