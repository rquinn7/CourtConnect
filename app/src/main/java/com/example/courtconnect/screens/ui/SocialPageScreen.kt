// R. Quinn - Final Edit - November 11, 2026
package com.example.courtconnect.screens.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchByTextRequest

// https://medium.com/@manishkumar_75473/deep-dive-into-kotlin-data-classes-for-android-engineers-f31dfd39a18b
// used similarly to how they are used in Python where you can use this to create
// objects for each post or comment, which contains multiple different data points
data class SocialPost(
    val id: String,
    val authorId: String,
    val username: String,
    val sport: String,
    val message: String,
    val createdAt: Timestamp?,
    val courtId: String? = null,
    val courtName: String? = null
)

data class SocialComment(
    val id: String,
    val authorId: String,
    val username: String,
    val message: String,
    val createdAt: Timestamp?
)

// in order to use the exposeddropdownmenus need to opt in to experimental
// material3 api classes
// https://developer.android.com/develop/ui/compose/designsystems/material3#experimental-apis
@OptIn(ExperimentalMaterial3Api::class)
// create the social page screen which includes the posts and commenting on posts
@Composable
fun SocialPageScreen(
    onHomeClick: () -> Unit,
    onTennisClick: () -> Unit,
    onBasketballClick: () -> Unit,
    onPickleballClick: () -> Unit
) {
    // used to hold the message within the post from the user
    var postMessage by remember { mutableStateOf("") }
    // used to hold the value for the selected sport which is displayed in the post
    var selectedSport by remember { mutableStateOf("Basketball") }
    // boolean used to hold whether the given sport menu is expanded or not
    var expandedSportsMenu by remember { mutableStateOf(false) }

    // Whether the user wants to create a post about a specific court
    var courtPostBoolean by remember { mutableStateOf(false) }

    // List of nearby courts for the selected sport
    var nearbyCourts by remember {
        mutableStateOf<List<Place>>(emptyList())
    }

    // The court selected by the user
    var selectedCourt by remember {
        mutableStateOf<Place?>(null)
    }

    // Controls whether the court dropdown is open
    var courtMenuStatus by remember {
        mutableStateOf(false)
    }

    // Tracks whether nearby courts are loading
    var courtLoadingStatus by remember {
        mutableStateOf(false)
    }

    // Holds any errors when searching for nearby courts
    var courtError by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val placesClient = remember {
        Places.createClient(context)
    }



    // boolean used to hold the value whether the post is in process of posting or not
    var postingStatus by remember { mutableStateOf(false) }
    // holds the value of the postError, if there was an error when posting the given post
    var postError by remember { mutableStateOf("") }
    // used to display to the user that the post was successfully posted
    var postSuccess by remember { mutableStateOf("") }

    // used for firebase authorization to make sure account is in firebase
    // also the db is used to access the firestore database which holds all users,
    // accounts, posts, etc...
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // list which holds the posts
    var posts by remember { mutableStateOf<List<SocialPost>>(emptyList()) }
    // boolean which displays whether post are loading or not
    var loadingPostsStatus by remember { mutableStateOf(true) }
    // value used to display errors in the feed
    var feedError by remember { mutableStateOf("") }

    // used to select and pass which post is to be deleted
    var postToDelete by remember {
        mutableStateOf<SocialPost?>(null)
    }

    // boolean used to hold whether post is in process of deleting or not
    var isDeleting by remember {
        mutableStateOf(false)
    }

    // holds message which would be used for displaying delete errors
    var deleteError by remember {
        mutableStateOf("")
    }

    @SuppressLint("MissingPermission")
    LaunchedEffect(courtPostBoolean, selectedSport) {

        nearbyCourts = emptyList()
        selectedCourt = null
        courtError = ""
        courtMenuStatus = false

        if (courtPostBoolean) {

            val hasFinePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarsePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasFinePermission && !hasCoarsePermission) {
                courtError = "Location permission is required to find nearby courts."
            } else {

                courtLoadingStatus = true

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->

                        if (location == null) {
                            courtError = "Unable to determine your location."
                            courtLoadingStatus = false
                        } else {

                            val currentLocation =
                                com.google.android.gms.maps.model.LatLng(
                                    location.latitude,
                                    location.longitude
                                )

                            val searchTerm = when (selectedSport) {
                                "Basketball" -> "basketball court"
                                "Tennis" -> "tennis court"
                                "Pickleball" -> "pickleball court"
                                else -> ""
                            }

                            val placeFields = listOf(
                                Place.Field.ID,
                                Place.Field.DISPLAY_NAME,
                                Place.Field.LOCATION
                            )

                            val searchArea = CircularBounds.newInstance(
                                currentLocation,
                                16000.0
                            )

                            val request = SearchByTextRequest.builder(
                                searchTerm,
                                placeFields
                            )
                                .setLocationBias(searchArea)
                                .setMaxResultCount(20)
                                .build()

                            placesClient.searchByText(request)
                                .addOnSuccessListener { response ->
                                    nearbyCourts = response.places
                                    courtLoadingStatus = false
                                }
                                .addOnFailureListener { error ->
                                    courtError = error.localizedMessage
                                        ?: "Unable to find nearby courts."
                                    courtLoadingStatus = false
                                }
                        }
                    }
                    .addOnFailureListener { error ->
                        courtError = error.localizedMessage
                            ?: "Unable to get your location."
                        courtLoadingStatus = false
                    }
            }
        } else {
            courtLoadingStatus = false
        }
    }

    // https://kotlinlang.org/docs/collection-transformations.html
    // https://firebase.google.com/docs/firestore/query-data/order-limit-data
    // https://firebase.google.com/docs/firestore/query-data/listen
    // https://developer.android.com/develop/ui/compose/side-effects
    // used a combination of the sources above and AI in order to build the process
    // of creating posts, sending them to the database, deleting the posts
    // and also displaying the posts
    DisposableEffect(db) {
        val listener = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    feedError = error.localizedMessage
                        ?: "Failed to load community posts."
                    loadingPostsStatus = false
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
                            createdAt = document.getTimestamp("createdAt"),
                            courtId = document.getString("courtId"),
                            courtName = document.getString("courtName")
                        )
                    }

                    feedError = ""
                    loadingPostsStatus = false
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
                text = "What's happening at your courts!",
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
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5722),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF5B9808)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // https://developer.android.com/reference/kotlin/androidx/compose/material/ExposedDropdownMenuBoxScope
                    // used to ask the user which sport they would like to make the post for
                    ExposedDropdownMenuBox(
                        expanded = expandedSportsMenu,
                        onExpandedChange = {
                            expandedSportsMenu = it
                        }
                    ) {
                        // text field which will be filled in with the selected sport
                        OutlinedTextField(
                            value = selectedSport,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sport") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedSportsMenu
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF5722),
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color(0xFFFF5722)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedSportsMenu,
                            onDismissRequest = {
                                expandedSportsMenu = false
                            },
                            containerColor = Color(0xFFFFF0EB),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 4.dp
                        ) {
                            listOf("Basketball", "Tennis", "Pickleball").forEach { sport ->

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sport,
                                            color = Color.Black
                                        )
                                    },
                                    onClick = {
                                        selectedSport = sport
                                        expandedSportsMenu = false
                                    },
                                    modifier = Modifier
                                        .background(
                                            if (selectedSport == sport) {
                                                Color(0xFFFFD5C9)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = courtPostBoolean,
                            onCheckedChange = { courtPostBoolean = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF5B9808)
                            )
                        )

                        Text("Choose a specific court")
                    }

                    if (courtPostBoolean) {

                        Spacer(modifier = Modifier.height(8.dp))

                        if (courtLoadingStatus) {
                            CircularProgressIndicator(
                                color = Color(0xFF5B9808)
                            )
                        }

                        if (courtError.isNotEmpty()) {
                            Text(
                                text = courtError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }

                        if (!courtLoadingStatus && courtError.isEmpty()) {

                            if (nearbyCourts.isEmpty()) {
                                Text(
                                    text = "No nearby courts found.",
                                    color = Color.Gray
                                )
                            } else {

                                ExposedDropdownMenuBox(
                                    expanded = courtMenuStatus,
                                    onExpandedChange = {
                                        courtMenuStatus = it
                                    }
                                ) {

                                    OutlinedTextField(
                                        value = selectedCourt?.displayName ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Nearby Court") },
                                        placeholder = { Text("Select a court") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = courtMenuStatus
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(
                                                ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                            ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF5722),
                                            unfocusedBorderColor = Color.Gray,
                                            focusedLabelColor = Color(0xFFFF5722)
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = courtMenuStatus,
                                        onDismissRequest = {
                                            courtMenuStatus = false
                                        },
                                        containerColor = Color(0xFFFFF0EB)
                                    ) {

                                        nearbyCourts.forEach { court ->

                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        court.displayName ?: "Unknown Court"
                                                    )
                                                },
                                                onClick = {
                                                    selectedCourt = court
                                                    courtMenuStatus = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            // gets the current user before making the post
                            val user = auth.currentUser
                            // retrieves the post message and removes any white space
                            val message = postMessage.trim()
                            // gets the selected sport from the dropdown menu
                            val sport = selectedSport

                            // clears any old post errors or successful post messages
                            postError = ""
                            postSuccess = ""

                            // if no user then send this error
                            if (user == null) {
                                postError = "You must be logged in to post."
                                // if no message on the post send this error
                            } else if (message.isBlank()) {
                                postError = "Please enter a message."
                            } else if (courtPostBoolean && selectedCourt?.id == null) {
                                postError = "Please select a nearby court."
                            } else {
                                postingStatus = true

                                // Find the username belonging to the logged-in user
                                db.collection("users")
                                    .document(user.uid)
                                    .get()
                                    .addOnSuccessListener { userDocument ->

                                        val username = userDocument.getString("username")

                                        if (username.isNullOrBlank()) {
                                            postError = "Could not find your username."
                                            postingStatus = false
                                        } else {
                                            // pull all the data which will be sent to firebase
                                            val post = hashMapOf(
                                                "authorId" to user.uid,
                                                "username" to username,
                                                "sport" to sport,
                                                "message" to message,
                                                "createdAt" to FieldValue.serverTimestamp()
                                            )
                                            if (courtPostBoolean) {
                                                selectedCourt?.let { court ->
                                                    post["courtId"] = court.id ?: ""
                                                    post["courtName"] = court.displayName ?: "Unknown Court"
                                                }
                                            }

                                            // create the new post in firebase
                                            db.collection("posts")
                                                .add(post)
                                                .addOnSuccessListener {
                                                    postMessage = ""
                                                    postSuccess = "Your post was published!"
                                                    postingStatus = false
                                                    courtPostBoolean = false
                                                    selectedCourt = null
                                                    nearbyCourts = emptyList()
                                                }
                                                .addOnFailureListener { exception ->
                                                    postError =
                                                        exception.localizedMessage
                                                            ?: "Failed to publish post."
                                                    postingStatus = false
                                                }
                                        }
                                    }
                                    // if no account can be loaded then display error
                                    .addOnFailureListener { exception ->
                                        postError =
                                            exception.localizedMessage
                                                ?: "Failed to load your account."
                                        postingStatus = false
                                    }
                            }
                        },
                        // only enable the button if there is a post and if it not over 500 words
                        enabled = postMessage.isNotBlank() &&
                                postMessage.length <= 500 &&
                                !postingStatus,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B9808)
                        )
                    ) {
                        Text(if (postingStatus) "Posting..." else "Post")
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

            if (deleteError.isNotEmpty()) {
                Text(
                    text = deleteError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // different text which is only shown based on certain conditions
            // such as loading posts, errors obtaining the feed or no posts to be shown
            when {
                loadingPostsStatus -> {
                    CircularProgressIndicator(
                        color = Color(0xFF5B9808)
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

                // if the conditions above are not met, then display the posts from other users
                // or the current user's posts
                else -> {
                    posts.forEach { post ->

                        // obtain the currentUserId to ensure the given user can only delete their
                        // own posts
                        val currentUserId = auth.currentUser?.uid

                        SocialPostCard(
                            post = post,
                            onDeleteClick = if (
                                currentUserId != null &&
                                post.authorId == currentUserId
                            ) {
                                {
                                    // if the current user has a post, allow them to delete it
                                    // if they want
                                    postToDelete = post
                                }
                            } else {
                                null
                            },
                            showCommentsButton = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

            }

        }
    }


    // if the user has any posts given them option to delete
    if (postToDelete != null) {

        // https://developer.android.com/reference/android/app/AlertDialog
        // create a pop-up window which asks the user if they want to delete selected post
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    postToDelete = null
                }
            },

            title = {
                Text("Delete Post?")
            },

            text = {
                Text(
                    "Are you sure you want to delete this post? " +
                            "This action cannot be undone."
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        val post = postToDelete
                        val currentUserId = auth.currentUser?.uid

                        if (
                            post != null &&
                            currentUserId != null &&
                            post.authorId == currentUserId
                        ) {
                            isDeleting = true
                            deleteError = ""

                            db.collection("posts")
                                .document(post.id)
                                .delete()
                                .addOnSuccessListener {
                                    postToDelete = null
                                    isDeleting = false
                                }
                                .addOnFailureListener { exception ->
                                    deleteError =
                                        exception.localizedMessage
                                            ?: "Failed to delete post."
                                    isDeleting = false
                                    postToDelete = null
                                }
                        }
                    }
                ) {
                    Text(
                        text = if (isDeleting) {
                            "Deleting..."
                        } else {
                            "Delete"
                        },
                        color = Color.Red
                    )
                }
            },

            // if the user accidentally clicked the delete button or changed their mind
            // allow them to go back so they don't delete the post
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        postToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// function/composable which is used to create the social post cards
// this holds the post itself, comments button and delete button
@Composable
fun SocialPostCard(
    post: SocialPost,
    onDeleteClick: (() -> Unit)? = null,
    showCommentsButton: Boolean = false
) {

    // used to display the expanded comments
    var commentsExpanded by remember(post.id) {
        mutableStateOf(false)
    }

    // labels for which sport the post is referring to
    val sportList = when (post.sport) {
        "Basketball" -> "🏀 Basketball"
        "Tennis" -> "🎾 Tennis"
        "Pickleball" -> "🏓 Pickleball"
        else -> post.sport
    }

    // holds the formatted date, in order to display when the post was actually posted
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
                text = sportList,
                fontSize = 13.sp,
                color = Color(0xFF4A7000)
            )

            if (!post.courtName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "📍 ${post.courtName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5B9808)
                )
            }

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

            // as long as the post is the users or there are comments display the buttons at the
            // bottom of the post
            if (showCommentsButton || onDeleteClick != null) {

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (showCommentsButton) {
                        TextButton(
                            onClick = {
                                commentsExpanded = !commentsExpanded
                            }
                        ) {
                            Text(
                                text = if (commentsExpanded) {
                                    "Hide Comments"
                                } else {
                                    "💬 Comments"
                                },
                                color = Color(0xFF5B9808)
                            )
                        }
                    }

                    // if the post is the user's allow them to delete it
                    if (onDeleteClick != null) {
                        TextButton(
                            onClick = onDeleteClick
                        ) {
                            Text(
                                text = "Delete Post",
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            // if the comments button is pressed, show the comments section
            if (showCommentsButton && commentsExpanded) {

                Spacer(modifier = Modifier.height(8.dp))

                CommentsSection(postId = post.id)
            }
        }
    }
}

// function used to create and show the comments section
@Composable
fun CommentsSection(postId: String) {
    // need to use firebase to obtain the comments for the posts
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // list which holds the comments
    var comments by remember(postId) {
        mutableStateOf<List<SocialComment>>(emptyList())
    }

    // obtains the comment text
    var commentText by remember(postId) {
        mutableStateOf("")
    }

    // keeps track of when the comment is being posted
    var isSubmitting by remember {
        mutableStateOf(false)
    }

    // used if there is an error when posting the comment
    var commentError by remember {
        mutableStateOf("")
    }

    // display to the user if the comments are loading
    var commentLoadingStatus by remember {
        mutableStateOf(true)
    }

    // used to keep track of the comments and tell the path where they go in firestore
    val commentsLocation = remember(db, postId) {
        db.collection("posts")
            .document(postId)
            .collection("comments")
    }

    // works very similarly to the code above for creating a post
    // keeps track of the information for the comment, including userid,
    // message and when it was created in order to then be stored in the firestore database
    DisposableEffect(commentsLocation) {

        val listener = commentsLocation
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    commentError = error.localizedMessage
                        ?: "Unable to load comments."
                    commentLoadingStatus = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    comments = snapshot.documents.map { document ->
                        SocialComment(
                            id = document.id,
                            authorId = document.getString("authorId") ?: "",
                            username = document.getString("username") ?: "Unknown",
                            message = document.getString("message") ?: "",
                            createdAt = document.getTimestamp("createdAt")
                        )
                    }

                    commentLoadingStatus = false
                }
            }

        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Comments",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (commentLoadingStatus) {
            CircularProgressIndicator(
                color = Color(0xFF5B9808)
            )
        } else if (comments.isEmpty()) {
            Text(
                text = "No comments yet.",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        // go through the list of comments and display each comment
        comments.forEach { comment ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {

                Text(
                    text = "@${comment.username}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    text = comment.message,
                    fontSize = 14.sp
                )

                // the author of the given comment can only delete it
                if (comment.authorId == auth.currentUser?.uid) {

                    TextButton(
                        onClick = {
                            commentsLocation.document(comment.id)
                                .delete()
                                .addOnFailureListener { error ->
                                    commentError =
                                        error.localizedMessage
                                            ?: "Unable to delete comment."
                                }
                        }
                    ) {
                        Text(
                            text = "Delete Comment",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            HorizontalDivider()
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = commentText,
            onValueChange = {
                if (it.length <= 300) {
                    commentText = it
                }
            },
            label = {
                Text("Write a comment")
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5B9808),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF5B9808),
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color(0xFF5B9808)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {

                val user = auth.currentUser
                val message = commentText.trim()

                commentError = ""

                if (user == null) {
                    commentError = "Please log in to comment."
                } else if (message.isNotBlank()) {

                    isSubmitting = true

                    db.collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener { document ->

                            val username =
                                document.getString("username")

                            if (username.isNullOrBlank()) {
                                commentError = "Username not found."
                                isSubmitting = false
                            } else {

                                val comment = hashMapOf(
                                    "authorId" to user.uid,
                                    "username" to username,
                                    "message" to message,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )

                                commentsLocation.add(comment)
                                    .addOnSuccessListener {
                                        commentText = ""
                                        isSubmitting = false
                                    }
                                    .addOnFailureListener { error ->
                                        commentError =
                                            error.localizedMessage
                                                ?: "Unable to add comment."
                                        isSubmitting = false
                                    }
                            }
                        }
                        .addOnFailureListener { error ->
                            commentError =
                                error.localizedMessage
                                    ?: "Unable to load your profile."
                            isSubmitting = false
                        }
                }
            },
            enabled = commentText.isNotBlank() && !isSubmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5B9808)
            )
        ) {
            Text(
                if (isSubmitting) "Posting..." else "Comment"
            )
        }

        if (commentError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = commentError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
    }
}