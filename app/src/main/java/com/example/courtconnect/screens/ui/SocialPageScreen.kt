
package com.example.courtconnect.screens.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

private val SocialGreen = Color(0xFF5B9808)
data class SocialPost(
    val id: String,
    val authorId: String,
    val username: String,
    val sport: String,
    val message: String,
    val createdAt: Timestamp?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialPageScreen(
    onHomeClick: () -> Unit,
    onTennisClick: () -> Unit,
    onBasketballClick: () -> Unit,
    onPickleballClick: () -> Unit
) {
    var postMessage by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("Basketball") }
    var sportMenuExpanded by remember { mutableStateOf(false) }

    var isPosting by remember { mutableStateOf(false) }
    var postError by remember { mutableStateOf("") }
    var postSuccess by remember { mutableStateOf("") }

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var posts by remember { mutableStateOf<List<SocialPost>>(emptyList()) }
    var isLoadingPosts by remember { mutableStateOf(true) }
    var feedError by remember { mutableStateOf("") }


    DisposableEffect(db) {
        val listener = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    feedError = error.localizedMessage
                        ?: "Failed to load community posts."
                    isLoadingPosts = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    posts = snapshot.documents.map { document ->
                        SocialPost(
                            id = document.id,
                            authorId = document.getString("authorId") ?: "",
                            username = document.getString("username") ?: "Unknown",
                            sport = document.getString("sport") ?: "",
                            message = document.getString("message") ?: "",
                            createdAt = document.getTimestamp("createdAt")
                        )
                    }

                    feedError = ""
                    isLoadingPosts = false
                }
            }

        onDispose {
            listener.remove()
        }
    }


    Scaffold(
        topBar = {
            LogoTopApp()
        },
        bottomBar = {
            SportsNavigationBottom(
                selectedRoute = "connect",
                onHomeClick = onHomeClick,
                onTennisClick = onTennisClick,
                onBasketballClick = onBasketballClick,
                onPickleballClick = onPickleballClick,
                onConnectClick = {}
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Text(
                text = "Connect",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share what's happening at your courts!",
                fontSize = 15.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Create a Post",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = postMessage,
                        onValueChange = { postMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("What's happening at your courts?")
                        },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = sportMenuExpanded,
                        onExpandedChange = {
                            sportMenuExpanded = it
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedSport,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sport") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = sportMenuExpanded
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                )
                        )

                        ExposedDropdownMenu(
                            expanded = sportMenuExpanded,
                            onDismissRequest = {
                                sportMenuExpanded = false
                            }
                        ) {
                            listOf(
                                "Basketball",
                                "Tennis",
                                "Pickleball"
                            ).forEach { sport ->
                                DropdownMenuItem(
                                    text = { Text(sport) },
                                    onClick = {
                                        selectedSport = sport
                                        sportMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Button(
                        onClick = {
                            val user = auth.currentUser
                            val message = postMessage.trim()
                            val sport = selectedSport

                            postError = ""
                            postSuccess = ""

                            if (user == null) {
                                postError = "You must be logged in to post."
                            } else if (message.isBlank()) {
                                postError = "Please enter a message."
                            } else {
                                isPosting = true

                                // Find the username belonging to the logged-in user.
                                db.collection("users")
                                    .document(user.uid)
                                    .get()
                                    .addOnSuccessListener { userDocument ->

                                        val username = userDocument.getString("username")

                                        if (username.isNullOrBlank()) {
                                            postError = "Could not find your username."
                                            isPosting = false
                                        } else {
                                            // Build the data that will be stored in Firestore.
                                            val post = hashMapOf(
                                                "authorId" to user.uid,
                                                "username" to username,
                                                "sport" to sport,
                                                "message" to message,
                                                "createdAt" to FieldValue.serverTimestamp()
                                            )

                                            // Add a new document to the posts collection.
                                            db.collection("posts")
                                                .add(post)
                                                .addOnSuccessListener {
                                                    postMessage = ""
                                                    postSuccess = "Your post was published!"
                                                    isPosting = false
                                                }
                                                .addOnFailureListener { exception ->
                                                    postError =
                                                        exception.localizedMessage
                                                            ?: "Failed to publish post."
                                                    isPosting = false
                                                }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        postError =
                                            exception.localizedMessage
                                                ?: "Failed to load your account."
                                        isPosting = false
                                    }
                            }
                        },
                        enabled = postMessage.isNotBlank() &&
                                postMessage.length <= 500 &&
                                !isPosting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SocialGreen
                        )
                    ) {
                        Text(if (isPosting) "Posting..." else "Post")
                    }
                    if (postError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = postError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }

                    if (postSuccess.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = postSuccess,
                            color = Color(0xFF4A7000),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Community Posts",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))


            when {
                isLoadingPosts -> {
                    CircularProgressIndicator(
                        color = SocialGreen
                    )
                }

                feedError.isNotEmpty() -> {
                    Text(
                        text = feedError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                posts.isEmpty() -> {
                    Text(
                        text = "No community posts yet. Be the first to post!",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                else -> {
                    posts.forEach { post ->
                        SocialPostCard(post = post)

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

        }
    }

    @Composable
    fun SocialPostCard(post: SocialPost) {

        val sportLabel = when (post.sport) {
            "Basketball" -> "🏀 Basketball"
            "Tennis" -> "🎾 Tennis"
            "Pickleball" -> "🏓 Pickleball"
            else -> post.sport
        }

        val formattedDate = remember(post.createdAt) {
            post.createdAt?.toDate()?.let { date ->
                SimpleDateFormat(
                    "MMM d, yyyy 'at' h:mm a",
                    Locale.getDefault()
                ).format(date)
            } ?: "Just now"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "@${post.username}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = sportLabel,
                    fontSize = 13.sp,
                    color = Color(0xFF4A7000)
                )

                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = post.message,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun SocialPostCard(post: SocialPost) {

    val sportLabel = when (post.sport) {
        "Basketball" -> "🏀 Basketball"
        "Tennis" -> "🎾 Tennis"
        "Pickleball" -> "🏓 Pickleball"
        else -> post.sport
    }

    val formattedDate = remember(post.createdAt) {
        post.createdAt?.toDate()?.let { date ->
            SimpleDateFormat(
                "MMM d, yyyy 'at' h:mm a",
                Locale.getDefault()
            ).format(date)
        } ?: "Just now"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "@${post.username}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = sportLabel,
                fontSize = 13.sp,
                color = Color(0xFF4A7000)
            )

            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.message,
                fontSize = 15.sp
            )
        }
    }
}

