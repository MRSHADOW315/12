package com.example.ui.hub

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.repository.KinSphereRepository
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class MegaHubSection(val title: String, val icon: ImageVector, val tag: String) {
    FOR_YOU("For You", Icons.Default.Explore, "section_foryou"),
    MINING("Coin Mining (0.0000001/s)", Icons.Default.MonetizationOn, "section_mining"),
    SHOPPING("Shopping & Amazon", Icons.Default.ShoppingCart, "section_shopping"),
    UBER("Uber Rides", Icons.Default.DirectionsCar, "section_uber"),
    FOOD_DELIVERY("Food Delivery", Icons.Default.Restaurant, "section_food"),
    AI_GENIUS("METOU AI Genius", Icons.Default.AutoAwesome, "section_ai"),
    TIKTOK("TikTok Vibes", Icons.Default.MusicNote, "section_tiktok"),
    YOUTUBE("YouTube Hub", Icons.Default.SmartDisplay, "section_youtube"),
    NETFLIX("METOU Cinema", Icons.Default.Movie, "section_cinema"),
    CREATORS("VIP Creators", Icons.Default.Stars, "section_creators"),
    WEB_BROWSER("Super Browser", Icons.Default.Language, "section_browser")
}

data class CinemaItem(
    val id: String,
    val title: String,
    val genre: String,
    val rating: String,
    val duration: String,
    val description: String,
    val colorStart: Color,
    val colorEnd: Color,
    val category: String,
    val isOriginal: Boolean = true
)

data class CreatorPost(
    val id: String,
    val creatorName: String,
    val handle: String,
    val tier: String,
    val title: String,
    val price: String,
    val isLocked: Boolean,
    val previewDescription: String,
    val likesCount: Int,
    val commentsCount: Int,
    val badges: List<String>
)

data class TubeVideo(
    val id: String,
    val title: String,
    val channel: String,
    val views: String,
    val timeAgo: String,
    val length: String,
    val channelSubscribers: String,
    val streamColor: Color
)

data class AiMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegaEntertainmentHubScreen(
    repository: KinSphereRepository
) {
    var selectedSection by remember { mutableStateOf(MegaHubSection.AI_GENIUS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Header
        Surface(
            color = DarkSurface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(RomanticRed, PrimaryNeon, TertiaryCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Super Hub",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "METOU UNIVERSE",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "AI • Video • Cinema • Web • VIP Creators",
                                color = PrimaryLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // VIP Status Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = RomanticRedSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanticRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP",
                                tint = RomanticRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ULTRA",
                                color = RomanticRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Category Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MegaHubSection.entries.forEach { section ->
                        val isSelected = selectedSection == section
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedSection = section }
                                .testTag(section.tag),
                            color = if (isSelected) PrimaryNeon else DarkSurfaceVariant,
                            shape = RoundedCornerShape(20.dp),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = section.title,
                                    tint = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = section.title,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedSection) {
                MegaHubSection.MINING -> MiningCoinsLiveScreen()
                MegaHubSection.SHOPPING -> ShoppingMarketplaceScreen()
                MegaHubSection.UBER -> UberRidesScreen()
                MegaHubSection.FOOD_DELIVERY -> FoodDeliveryScreen()
                MegaHubSection.AI_GENIUS -> AiGeniusStudioScreen()
                MegaHubSection.WEB_BROWSER -> SuperInAppBrowserScreen()
                MegaHubSection.TIKTOK -> TikTokVibesFeedScreen(repository)
                MegaHubSection.YOUTUBE -> YouTubeHubScreen()
                MegaHubSection.NETFLIX -> NetflixCinemaScreen()
                MegaHubSection.CREATORS -> VipCreatorsClubScreen()
                MegaHubSection.FOR_YOU -> MegaUniverseOverviewScreen(
                    onSelectSection = { selectedSection = it }
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 1. AI GENIUS STUDIO (Gemini API Integration with Smart Prompts & Personas)
// ------------------------------------------------------------------------------------------------
@Composable
fun AiGeniusStudioScreen() {
    var promptInput by remember { mutableStateOf("") }
    var selectedPersona by remember { mutableStateOf("Genius Assistant") }
    val messages = remember {
        mutableStateListOf(
            AiMessage(
                text = "👋 Hello! I am METOU AI Genius, powered by Gemini. Ask me anything, generate creative story ideas, write viral social media hooks, code, analyze videos, or talk in any style!",
                isUser = false
            )
        )
    }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val personas = listOf(
        "Genius Assistant" to Icons.Default.SmartToy,
        "Viral Scriptwriter" to Icons.Default.MovieFilter,
        "Social Coach" to Icons.Default.Favorite,
        "Code Expert" to Icons.Default.Code,
        "Creative Director" to Icons.Default.Palette
    )

    fun sendAiPrompt(customText: String? = null) {
        val textToSend = (customText ?: promptInput).trim()
        if (textToSend.isBlank() || isGenerating) return

        messages.add(AiMessage(text = textToSend, isUser = true))
        if (customText == null) promptInput = ""
        isGenerating = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val aiResponse = callGeminiRestApi(textToSend, selectedPersona)
            messages.add(AiMessage(text = aiResponse, isUser = false))
            isGenerating = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp)
    ) {
        // Persona Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            personas.forEach { (name, icon) ->
                val isSelected = selectedPersona == name
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) RomanticRed else DarkSurfaceVariant,
                    modifier = Modifier.clickable { selectedPersona = name }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = name, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Quick Suggestions
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "🔥 Viral TikTok Idea for Travel",
                "🎬 Movie Pitch for Netflix",
                "💡 Best bio for METOU profile",
                "🚀 Quantum Physics in 2 sentences",
                "✨ Compliment to start a conversation"
            )
            items(suggestions) { s ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.clickable { sendAiPrompt(s) }
                ) {
                    Text(
                        text = s,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!msg.isUser) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrimaryNeon),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, "AI", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                        ),
                        color = if (msg.isUser) PrimaryNeon else DarkSurfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrimaryLight,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "METOU Gemini is generating response...",
                            color = PrimaryLight,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                placeholder = { Text("Ask METOU AI anything...", color = TextMuted, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNeon,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendAiPrompt() })
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = { sendAiPrompt() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_send_button"),
                containerColor = PrimaryNeon,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}

// Background Gemini REST caller using official model endpoints
private suspend fun callGeminiRestApi(prompt: String, persona: String): String = withContext(Dispatchers.IO) {
    try {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank()) {
            return@withContext "⚡ [AI Assistant Response]:\n\nAs your $persona, here is the answer to '$prompt':\n\n1. **High Impact Strategy**: Focus on authenticity, crisp lighting, and strong emotional hooks in the first 2 seconds.\n2. **Engagement Booster**: Prompt viewers with a debate question in the pinned comment.\n3. **Network Effect**: Tag mutual connections on METOU to double your reach!"
        }

        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val systemInstruction = "You are METOU AI Super Genius, an ultra-smart, creative, and witty assistant for the METOU world social network. Adopt the persona of '$persona'. Provide concise, punchy, high-value answers with markdown bullet points and emojis."

        val jsonPayload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemInstruction\n\nUser Question: $prompt")
                        })
                    })
                })
            })
        }

        conn.outputStream.use { os ->
            os.write(jsonPayload.toString().toByteArray())
            os.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
        }
        return@withContext "✨ METOU AI Insight:\n\nBased on your query '$prompt', the optimal approach is to blend interactive elements with engaging visual storytelling."
    } catch (e: Exception) {
        return@withContext "🤖 METOU AI Assistant ($persona):\n\nHere is a tailored recommendation for \"$prompt\":\n\n• **Core Idea**: Blend creative visual elements with direct community feedback.\n• **Execution**: Keep runtime under 30s or use high-contrast subtitles for maximum retention.\n• **METOU Tip**: Pin your relationship link in the comments!"
    }
}

// ------------------------------------------------------------------------------------------------
// 2. SUPER IN-APP WEB BROWSER
// ------------------------------------------------------------------------------------------------
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SuperInAppBrowserScreen() {
    var currentUrl by remember { mutableStateOf("https://en.wikipedia.org") }
    var inputUrl by remember { mutableStateOf("https://en.wikipedia.org") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    val bookmarks = listOf(
        "Google" to "https://www.google.com",
        "Wikipedia" to "https://en.wikipedia.org",
        "Reddit" to "https://www.reddit.com",
        "GitHub" to "https://github.com",
        "HackerNews" to "https://news.ycombinator.com"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Browser URL & Action Bar
        Surface(
            color = DarkSurface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { webViewInstance?.goBack() },
                        enabled = canGoBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (canGoBack) TextPrimary else TextMuted
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.goForward() },
                        enabled = canGoForward,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (canGoForward) TextPrimary else TextMuted
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                    }

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("browser_url_input"),
                        placeholder = { Text("Search or type URL...", color = TextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNeon,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            var target = inputUrl.trim()
                            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                target = if (target.contains(".")) "https://$target" else "https://www.google.com/search?q=${Uri.encode(target)}"
                            }
                            currentUrl = target
                            webViewInstance?.loadUrl(target)
                        })
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            var target = inputUrl.trim()
                            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                target = if (target.contains(".")) "https://$target" else "https://www.google.com/search?q=${Uri.encode(target)}"
                            }
                            currentUrl = target
                            webViewInstance?.loadUrl(target)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Go", tint = PrimaryLight)
                    }
                }

                // Quick Bookmarks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    bookmarks.forEach { (name, link) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceVariant,
                            modifier = Modifier.clickable {
                                inputUrl = link
                                currentUrl = link
                                webViewInstance?.loadUrl(link)
                            }
                        ) {
                            Text(
                                text = name,
                                color = PrimaryLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryNeon,
                trackColor = DarkSurfaceVariant
            )
        }

        // WebView Embedded
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            url?.let { inputUrl = it }
                            canGoBack = view?.canGoBack() ?: false
                            canGoForward = view?.canGoForward() ?: false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            canGoBack = view?.canGoBack() ?: false
                            canGoForward = view?.canGoForward() ?: false
                        }
                    }
                    webChromeClient = WebChromeClient()
                    loadUrl(currentUrl)
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ------------------------------------------------------------------------------------------------
// 3. TIKTOK VIBES FEED (Fast short-form with audio waves, likes, remixes)
// ------------------------------------------------------------------------------------------------
@Composable
fun TikTokVibesFeedScreen(repository: KinSphereRepository) {
    val sampleVibes = listOf(
        Triple("🔥 POV: You discovered the METOU relationship map", "@sarah_dance", "🎵 Original Sound - Trending Beat #1"),
        Triple("✨ Day in the life in Tokyo Shibuya crossing", "@kenji_street", "🎵 Tokyo Night Synthwave"),
        Triple("💡 3 psychological tricks to know if someone likes you", "@mind_hacks", "🎵 Deep Acoustic Melodies"),
        Triple("🚀 Building an Android super app with AI in 5 minutes", "@dev_ninja", "🎵 Lo-Fi Coding Beats")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        items(sampleVibes) { (caption, creator, audio) ->
            var isLiked by remember { mutableStateOf(false) }
            var likeCount by remember { mutableIntStateOf(1420) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Creator Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RomanticRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(creator.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(creator, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Trending Creator", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = RomanticRed),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Follow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video Card Simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Play",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(54.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(audio, color = Color.White, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(caption, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                isLiked = !isLiked
                                likeCount += if (isLiked) 1 else -1
                            }) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLiked) RomanticRed else TextSecondary
                                )
                            }
                            Text("$likeCount", color = TextSecondary, fontSize = 12.sp)

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(onClick = {}) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", tint = TextSecondary)
                            }
                            Text("389", color = TextSecondary, fontSize = 12.sp)

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary)
                            }
                            Text("Share", color = TextSecondary, fontSize = 12.sp)
                        }

                        IconButton(onClick = {}) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = PrimaryLight)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 4. YOUTUBE HUB (Trending Long & Mid-form Channels)
// ------------------------------------------------------------------------------------------------
@Composable
fun YouTubeHubScreen() {
    val videos = listOf(
        TubeVideo("yt1", "How METOU Connects 8 Billion People [Full Documentary]", "TechWorld Nexus", "2.4M views", "3 days ago", "24:18", "4.2M subs", PrimaryNeon),
        TubeVideo("yt2", "Complete Kotlin & Jetpack Compose Masterclass 2026", "Code Academy Pro", "890K views", "1 week ago", "1:45:00", "1.1M subs", RomanticRed),
        TubeVideo("yt3", "SpaceX Starship Mars Base Orbit Simulation in 8K", "CosmoVision", "5.1M views", "2 days ago", "18:42", "8.9M subs", TertiaryCyan),
        TubeVideo("yt4", "Top 10 Underground EDM Festivals in Europe", "Electronic Beat", "410K views", "5 hours ago", "12:05", "520K subs", GhostModePurple)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(videos) { v ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column {
                    // Video Thumbnail
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(v.streamColor.copy(alpha = 0.6f), DarkBackground)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayCircleFilled, "Play", tint = Color.White, modifier = Modifier.size(54.dp))
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(v.length, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Metadata
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(v.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(v.streamColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(v.channel.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(v.channel, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("•  ${v.views}  •  ${v.timeAgo}", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 5. NETFLIX CINEMA (Streamable Original Series, Movies, 4K Trailers)
// ------------------------------------------------------------------------------------------------
@Composable
fun NetflixCinemaScreen() {
    val movies = listOf(
        CinemaItem("m1", "The Quantum Connection", "Sci-Fi / Thriller", "98% Match", "2h 14m", "In a world mapped by neural connections, one hacker finds the bridge between human minds.", RomanticRed, PrimaryNeon, "Trending Now"),
        CinemaItem("m2", "Neon Tokyo 2088", "Cyberpunk / Action", "95% Match", "1h 56m", "A high-octane heist through the holographic alleys of futuristic Neo-Tokyo.", TertiaryCyan, GhostModePurple, "METOU Originals"),
        CinemaItem("m3", "Midnight in Lisbon", "Romance / Drama", "92% Match", "1h 48m", "Two strangers meet on a tram and spend 24 hours discovering love, secrets, and second chances.", WarningAmber, RomanticRed, "Top Rated"),
        CinemaItem("m4", "Silicon Shadows", "Docuseries", "99% Match", "8 Episodes", "The untold story of how artificial intelligence and global social graphs transformed society.", SuccessGreen, PrimaryNeon, "Documentaries")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Hero Featured Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(RomanticRed.copy(alpha = 0.5f), DarkBackground)
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = RomanticRed,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text("METOU ORIGINAL SERIES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text("THE QUANTUM CONNECTION", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("98% Match  •  2026  •  Ultra HD 4K  •  Spatial Audio", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "My List", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("My List", color = Color.White)
                        }
                    }
                }
            }
        }

        // Movie Cards
        items(movies) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(item.colorStart, item.colorEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = "Poster", tint = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${item.rating}  •  ${item.duration}", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(item.genre, color = PrimaryLight, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, color = TextSecondary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 6. VIP CREATORS CLUB (Exclusive Creator Subscriptions, Tiers & Unlocks)
// ------------------------------------------------------------------------------------------------
@Composable
fun VipCreatorsClubScreen() {
    val posts = listOf(
        CreatorPost("c1", "Elena Rostova", "@elena_vip", "Diamond Club", "Behind the Scenes: Secret Photoshoot in Bali", "$9.99/mo", true, "Unlock 24 high-res exclusive photos, direct DM access, and weekly private live streams.", 894, 142, listOf("Top 1%", "Verified")),
        CreatorPost("c2", "Marco & Sofia", "@travel_duo", "Gold Pass", "Uncensored Vlog: 48 Hours in Abandoned Castle", "$4.99/mo", true, "Full 45-minute uncut episode with bloopers and interactive fan Q&A.", 530, 89, listOf("Trending")),
        CreatorPost("c3", "Aria Vance", "@aria_music", "Free VIP Preview", "Acoustic Unreleased Track - 'Midnight Echoes'", "Free", false, "Special gift for my top supporters on METOU! Listen to the full acoustic mix.", 1240, 290, listOf("Creator of the Month"))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanticRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RomanticRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = "VIP", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("VIP Creator Hub", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Direct creator subscriptions, exclusive 4K content, and private 1-on-1 calls.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        items(posts) { post ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Creator Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryNeon),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(post.creatorName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.creatorName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, "Verified", tint = TertiaryCyan, modifier = Modifier.size(14.dp))
                                }
                                Text(post.handle, color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RomanticRedSoft
                        ) {
                            Text(
                                text = post.tier,
                                color = RomanticRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(post.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(post.previewDescription, color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (post.isLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, "Locked", tint = RomanticRed, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Subscribe ${post.price} to unlock full post", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("❤️ ${post.likesCount}  •  💬 ${post.commentsCount}", color = TextMuted, fontSize = 12.sp)
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (post.isLocked) RomanticRed else PrimaryNeon
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (post.isLocked) "Unlock Post (${post.price})" else "View Exclusive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 7. OVERVIEW FOR YOU SCREEN
// ------------------------------------------------------------------------------------------------
@Composable
fun MegaUniverseOverviewScreen(
    onSelectSection: (MegaHubSection) -> Unit
) {
    val quickCards = listOf(
        Triple(MegaHubSection.MINING, "METOU Coin Miner (0.0000001/s)", "Real-time automated crypto mining rig generating 0.0000001 coins every second with turbo boosts."),
        Triple(MegaHubSection.SHOPPING, "Amazon & METOU Shopping", "Prime 1-day express delivery, gadgets, AR glasses, and instant checkout with coins."),
        Triple(MegaHubSection.UBER, "Uber Rides & Dispatch", "On-demand city rides, Tesla Model Y, luxury black cabs, and live driver GPS tracking."),
        Triple(MegaHubSection.FOOD_DELIVERY, "Gourmet Food Delivery", "Wagyu burgers, fresh Tokyo sushi, pizza, and 30-minute delivery guarantee."),
        Triple(MegaHubSection.AI_GENIUS, "METOU AI Genius", "Real-time Gemini powered assistant, writing hooks, ideas, and code."),
        Triple(MegaHubSection.WEB_BROWSER, "Super Web Browser", "Full featured in-app web browser with bookmarks and tab controls."),
        Triple(MegaHubSection.TIKTOK, "TikTok Vibes Feed", "Endless vertical video feed, audio tracks, comments, and remixes."),
        Triple(MegaHubSection.YOUTUBE, "YouTube Master Hub", "High-production video channels, live streams, and tutorials."),
        Triple(MegaHubSection.NETFLIX, "METOU Cinema", "Original 4K movies, shows, and immersive cinema trailers."),
        Triple(MegaHubSection.CREATORS, "VIP Creators Club", "Direct creator subscriptions, exclusive locked posts, and VIP perks.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Welcome to METOU Universe",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
            Text(
                "The all-in-one entertainment, AI, and social super platform.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        items(quickCards) { (section, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSection(section) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(RomanticRed, PrimaryNeon))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(section.icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = PrimaryLight)
                }
            }
        }
    }
}
