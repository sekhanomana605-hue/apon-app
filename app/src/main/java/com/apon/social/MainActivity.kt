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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
val AponMidnight = Color(0xFF080A1A)
val AponSurfaceDark = Color(0xFF101328)
val AponSurfaceCard = Color(0xFF161A36)
val AponCyan = Color(0xFF00D2FF)
val AponMagenta = Color(0xFFE024C3)
val AponCoral = Color(0xFFFF5277)
val AponWhite = Color(0xFFF6F8FF)
val AponMuted = Color(0xFF8E95B8)

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

// --- IN-MEMORY STATE ENGINE ---
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
        ChatMessage("m1", "Amina", "Hey! Have yo
