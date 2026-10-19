package com.apon.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.AsyncImage

val AponMidnight = Color(0xFF080A1A)
val AponSurfaceDark = Color(0xFF101328)
val AponSurfaceCard = Color(0xFF161A36)
val AponCyan = Color(0xFF00D2FF)
val AponMagenta = Color(0xFFE024C3)
val AponCoral = Color(0xFFFF5277)
val AponWhite = Color(0xFFF6F8FF)
val AponMuted = Color(0xFF8E95B8)

data class PostModel(val id: String, val author: String, val text: String, val img: String?, val likes: Int, val isLiked: Boolean = false)
data class ChatMsg(val id: String, val text: String, val isMe: Boolean)

object AppState {
    var loggedIn by mutableStateOf<String?>("you")
    val posts = mutableStateListOf(
        PostModel("1", "Amina Rahman", "Loving the official APON look and feel! Connect Your World. 🌐✨", "https://picsum.photos/seed/sunset/800/600", 36),
        PostModel("2", "Farhan Ahmed", "The new branding is live! The launcher icon looks stunning.", null, 68)
    )
    val msgs = mutableStateListOf(
        ChatMsg("1", "Hey! Have you seen the new APON logo in action?", false),
        ChatMsg("2", "Yes, it looks completely custom and modern!", true)
    )
    fun toggleLike(id: String) {
        val i = posts.indexOfFirst { it.id == id }
        if (i != -1) {
            val p = posts[i]
            posts[i] = p.copy(isLiked = !p.isLiked, likes = if (!p.isLiked) p.likes + 1 else p.likes - 1)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = AponCyan, secondary = AponMagenta, background = AponMidnight,
                    surface = AponSurfaceDark, surfaceVariant = AponSurfaceCard, onBackground = AponWhite, onSurface = AponWhite
                )
            ) {
                var isAuth by remember { mutableStateOf(AppState.loggedIn != null) }
                if (!isAuth) {
                    AuthScreen { isAuth = true }
                } else {
                    MainAppScaffold { isAuth = false }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(onDone: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(AponMidnight).padding(20.dp), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = AponSurfaceDark), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(70.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(AponCyan, AponMagenta))), Alignment.Center) {
                    Text("APON", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Spacer(Modifier.height(14.dp))
                Text("Welcome to APON", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AponWhite)
                Text("Connect Your World", fontSize = 12.sp, color = AponCyan)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = AponCyan)) {
                    Text("Log In", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavItem("feed", "Home", Icons.Filled.Home)
    object Create : NavItem("create", "Create", Icons.Filled.Add)
    object Chat : NavItem("chat", "Chat", Icons.Filled.Email)
    object Profile : NavItem("profile", "Profile", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(onLogout: () -> Unit) {
    val nav = rememberNavController()
    val items = listOf(NavItem.Home, NavItem.Create, NavItem.Chat, NavItem.Profile)
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = AponSurfaceDark) {
                val back by nav.currentBackStackEntryAsState()
                val cur = back?.destination?.route
                items.forEach { itm ->
                    NavigationBarItem(
                        icon = { Icon(itm.icon, contentDescription = itm.title) },
                        label = { Text(itm.title) },
                        selected = cur == itm.route,
                        onClick = {
                            nav.navigate(itm.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = NavItem.Home.route, Modifier.padding(pad)) {
            composable(NavItem.Home.route) { FeedScreen() }
            composable(NavItem.Create.route) {
                var txt by remember { mutableStateOf("") }
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    OutlinedTextField(value = txt, onValueChange = { txt = it }, placeholder = { Text("What's on your mind?") }, modifier = Modifier.fillMaxWidth().weight(1f))
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { if (txt.isNotBlank()) { AppState.posts.add(0, PostModel("${System.currentTimeMillis()}", "You", txt, null, 0)); nav.popBackStack() } }, colors = ButtonDefaults.buttonColors(containerColor = AponCyan)) {
                        Text("Post", color = Color.Black)
                    }
                }
            }
            composable(NavItem.Chat.route) {
                var input by remember { mutableStateOf("") }
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AppState.msgs) { m ->
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.isMe) Alignment.End else Alignment.Start) {
                                Surface(shape = RoundedCornerShape(12.dp), color = if (m.isMe) AponCyan else AponSurfaceCard) {
                                    Text(m.text, Modifier.padding(10.dp), color = if (m.isMe) Color.Black else AponWhite)
                                }
                            }
                        }
                    }
                    Row(Modifier.padding(12.dp).fillMaxWidth()) {
                        OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Message...") })
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { if (input.isNotBlank()) { AppState.msgs.add(ChatMsg("${System.currentTimeMillis()}", input, true)); input = "" } }) {
                            Icon(Icons.Filled.Send, contentDescription = null, tint = AponCyan)
                        }
                    }
                }
            }
            composable(NavItem.Profile.route) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    AsyncImage("https://picsum.photos/seed/you/200", null, Modifier.size(80.dp).clip(CircleShape).border(2.dp, AponCyan, CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(10.dp))
                    Text("Rayhan Kabir", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AponWhite)
                    Text("@you", color = AponMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = AponCoral), modifier = Modifier.fillMaxWidth()) {
                        Text("Log Out", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APON", fontWeight = FontWeight.Black, color = AponWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AponSurfaceDark)
            )
        }
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad)) {
            items(AppState.posts, key = { it.id }) { post ->
                Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AponSurfaceDark)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(post.author, fontWeight = FontWeight.Bold, color = AponWhite)
                        Spacer(Modifier.height(6.dp))
                        Text(post.text, color = AponWhite)
                        post.img?.let { img ->
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(img, null, Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { AppState.toggleLike(post.id) }) {
