package com.apon.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

// --- OFFICIAL APON BRAND COLOR PALETTE ---
val AponMidnight = Color(0xFF080A1A)       // Deep space background
val AponSurfaceDark = Color(0xFF101328)     // Card surface
val AponSurfaceCard = Color(0xFF161A36)     // Inner card surface
val AponCyan = Color(0xFF00D2FF)            // Logo cyan accent
val AponMagenta = Color(0xFFE024C3)         // Logo magenta accent
val AponCoral = Color(0xFFFF5277)           // Accent highlight
val AponWhite = Color(0xFFF6F8FF)           // Primary text
val AponMuted = Color(0xFF8E95B8)           // Secondary text

// --- DATA MODELS ---
data class PostModel(
    val id: String,
    val author: String,
    val authorHandle: String,
    val avatarUrl: String,
    val text: String,
    val imageUrl: String? = null,
    val timeAgo: String,
    val likesCount: Int,
    val isLiked: Boolean = false,
    val commentsCount: Int
)

data class StoryModel(val id: String, val username: String, val avatarUrl: String, val hasUnseen: Boolean)
data class ChatMessage(val id: String, val senderName: String, val text: String, val isFromMe: Boolean, val timestamp: String)

// --- IN-MEMORY INTERACTIVE STATE ---
object AppState {
    var loggedInUser by mutableStateOf<String?>("you")
    var userDisplayName by mutableStateOf("Rayhan Kabir")
    var userBio by mutableStateOf("Connecting the world with APON. 🚀")

    val stories = mutableStateListOf(
        StoryModel("s0", "Your Story", "https://picsum.photos/seed/you/200", false),
        StoryModel("s1", "Amina", "https://picsum.photos/seed/amina/200", true),
        StoryModel("s2", "Zubair", "https://picsum.photos/seed/zubair/200", true),
        StoryModel("s3", "Tania", "https://picsum.photos/seed/tania/200", true)
    )

    val posts = mutableStateListOf(
        PostModel(
            id = "p1",
            author = "Amina Rahman",
            authorHandle = "@amina_r",
            avatarUrl = "https://picsum.photos/seed/amina/200",
            text = "Loving the official APON look and feel! Connect Your World. 🌐✨",
            imageUrl = "https://picsum.photos/seed/sunset/800/600",
            timeAgo = "1h ago",
            likesCount = 36,
            commentsCount = 5
        ),
        PostModel(
            id = "p2",
            author = "Farhan Ahmed",
            authorHandle = "@farhan_dev",
            avatarUrl = "https://picsum.photos/seed/farhan/200",
            text = "The new branding is live! The launcher icon looks stunning on the home screen.",
            imageUrl = null,
            timeAgo = "2h ago",
            likesCount = 68,
            commentsCount = 12
        )
    )

    val messages = mutableStateListOf(
        ChatMessage("m1", "Amina", "Hey! Have you seen the new APON logo in action?", false, "10:14 AM"),
        ChatMessage("m2", "You", "Yes, it looks completely custom and modern!", true, "10:16 AM")
    )

    fun toggleLike(postId: String) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val p = posts[index]
            val liked = !p.isLiked
            posts[index] = p.copy(isLiked = liked, likesCount = if (liked) p.likesCount + 1 else p.likesCount - 1)
        }
    }

    fun addPost(text: String, imageUrl: String?) {
        posts.add(
            0,
            PostModel(
                id = "p_${System.currentTimeMillis()}",
                author = userDisplayName,
                authorHandle = "@$loggedInUser",
                avatarUrl = "https://picsum.photos/seed/you/200",
                text = text,
                imageUrl = imageUrl,
                timeAgo = "Just now",
                likesCount = 0,
                commentsCount = 0
            )
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        messages.add(ChatMessage("m_${System.currentTimeMillis()}", "You", text.trim(), true, "Now"))
    }
}

// --- OFFICIAL LOGO COMPONENT ---
@Composable
fun OfficialAponLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_apon_logo),
        contentDescription = "APON Logo",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f)),
        contentScale = ContentScale.Fit
    )
}

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = AponCyan,
                    secondary = AponMagenta,
                    background = AponMidnight,
                    surface = AponSurfaceDark,
                    surfaceVariant = AponSurfaceCard,
                    onBackground = AponWhite,
                    onSurface = AponWhite
                )
            ) {
                var showSplash by remember { mutableStateOf(true) }
                var isAuthenticated by remember { mutableStateOf(AppState.loggedInUser != null) }

                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else if (!isAuthenticated) {
                    AuthScreen(onLoginSuccess = { isAuthenticated = true })
                } else {
                    MainAppScaffold(onLogout = { isAuthenticated = false })
                }
            }
        }
    }
}

// --- SPLASH SCREEN WITH OFFICIAL BRANDING ---
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(1400)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AponMidnight, Color(0xFF04050D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = AponCyan,
                        ambientColor = AponMagenta
                    )
            ) {
                OfficialAponLogo(size = 140.dp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "APON",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = AponWhite
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(AponCyan)
                )
                Text(
                    text = "  Connect Your World  ",
                    fontSize = 13.sp,
                    color = AponMuted,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(AponMagenta)
                )
            }
        }
    }
}

// --- AUTH SCREEN WITH OFFICIAL LOGO ---
@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AponMidnight)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = AponSurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OfficialAponLogo(size = 80.dp)

                Spacer(Modifier.height(14.dp))

                Text(
                    text = if (isRegister) "Join APON" else "Welcome to APON",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AponWhite
                )

                Text(
                    text = "Connect Your World",
                    fontSize = 12.sp,
                    color = AponCyan,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email or Username") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AponCyan,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AponCyan,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = {
                        AppState.loggedInUser = if (email.isBlank()) "user" else email.substringBefore("@")
                        onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AponCyan)
                ) {
                    Text(
                        text = if (isRegister) "Create Account" else "Log In",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = { isRegister = !isRegister }) {
                    Text(
                        text = if (isRegister) "Already on APON? Sign In" else "New to APON? Register",
                        color = AponMuted
                    )
                }
            }
        }
    }
}

// --- MASTER NAVIGATION CONTAINER ---
sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavItem("feed", "Home", Icons.Filled.Home)
    object Create : NavItem("create", "Create", Icons.Filled.AddCircleOutline)
    object Chat : NavItem("chat", "Chat", Icons.Filled.ChatBubbleOutline)
    object Profile : NavItem("profile", "Profile", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navItems = listOf(NavItem.Home, NavItem.Create, NavItem.Chat, NavItem.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = AponSurfaceDark) {
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route

                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AponCyan,
                            selectedTextColor = AponCyan,
                            indicatorColor = AponSurfaceCard
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavItem.Home.route) { FeedScreen() }
            composable(NavItem.Create.route) {
                CreatePostScreen(onDone = {
                    navController.navigate(NavItem.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                })
            }
            composable(NavItem.Chat.route) { ChatScreen() }
            composable(NavItem.Profile.route) { ProfileScreen(onLogout = onLogout) }
        }
    }
}

// --- HOME FEED WITH LOGO IN TOP BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OfficialAponLogo(size = 32.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "APON",
                            fontWeight = FontWeight.Black,
                            color = AponWhite,
                            letterSpacing = 1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AponSurfaceDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(AppState.stories) { s ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(64.dp)
                        ) {
                            AsyncImage(
                                model = s.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (s.hasUnseen) AponMagenta else Color.Gray,
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(s.username, fontSize = 11.sp, maxLines = 1, color = AponWhite)
                        }
                    }
                }
            }
            items(AppState.posts, key = { it.id }) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AponSurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = post.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(post.author, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AponWhite)
                                Text("${post.authorHandle} • ${post.timeAgo}", fontSize = 11.sp, color = AponMuted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(post.text, fontSize = 14.sp, color = AponWhite)
                        post.imageUrl?.let { img ->
                            Spacer(Modifier.height(10.dp))
                            AsyncImage(
                                model = img,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { AppState.toggleLike(post.id) }) {
                                Icon(
                                    if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (post.isLiked) AponCoral else Color.White
                                )
                            }
                            Text("${post.likesCount}", fontSize = 13.sp, color = AponWhite)
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("${post.commentsCount}", fontSize = 13.sp, color = AponWhite)
                        }
                    }
                }
            }
        }
    }
}

// --- CREATE POST SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onDone: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var attachImage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Post", color = AponWhite) },
                actions = {
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                AppState.addPost(
                                    text,
                                    if (attachImage) "https://picsum.photos/seed/${System.currentTimeMillis()}/800/600" else null
                                )
                                onDone()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AponCyan),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Post", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AponSurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("What's on your mind? Connect with APON...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { attachImage = !attachImage }) {
                Text(if (attachImage) "Photo Attached ✓" else "Attach Sample Photo", color = AponCyan)
            }
        }
    }
}

// --- CHAT SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", color = AponWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AponSurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppState.messages, key = { it.id }) { msg ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (msg.isFromMe) AponCyan else AponSurfaceCard,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                msg.text,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                color = if (msg.isFromMe) Color.Black else AponWhite
                            )
                        }
                    }
                }
            }
            Surface(
                color = AponSurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            AppState.sendMessage(input)
                            input = ""
                        }
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = AponCyan)
                    }
                }
            }
        }
    }
}

// --- PROFILE SCREEN ---
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AponMidnight)
            .padding(16.dp)
    ) {
        AsyncImage(
            model = "https://picsum.photos/seed/you/200",
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, AponCyan, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(12.dp))
        Text(AppState.userDisplayName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AponWhite)
        Text("@${AppState.loggedInUser}", color = AponMuted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Text(AppState.userBio, fontSize = 14.sp, color = AponWhite)
        Spacer(Modifier.height(24.dp))
        Divider(color = Color(0x22FFFFFF))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = AponCoral),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out", color = Color.White)
        }
    }
}
