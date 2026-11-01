package com.example.courtconnect.screens.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.courtconnect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.firestore.Query

@Composable
fun HomeScreenWithFirebase(
    onHomeClick: () -> Unit,
    onBasketballClick: () -> Unit,
    onTennisClick: () -> Unit,
    onPickleballClick: () -> Unit,
    onProfileClick: () -> Unit,
    onConnectClick: () -> Unit
) {
    // holds the first name at top of screen
    var firstName by remember { mutableStateOf("") }

    // firebase database and authentication in order to pull the name of the user
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // list used when pulling in the recent posts to be shown on the homescreen
    var recentPosts by remember {
        mutableStateOf<List<SocialPost>>(emptyList())
    }

    // boolean used to know if the recent posts are loading
    var recentPostLoadingStatus by remember {
        mutableStateOf(true)
    }

    // used to hold the recent posts error
    var recentPostsError by remember {
        mutableStateOf("")
    }


    // create disposable effect which is used to pull the recent posts
    // this will also notify if there was an error when gathering the posts from firestore
    DisposableEffect(db) {

        val listener = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(3)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    recentPostsError = error.localizedMessage
                        ?: "Unable to load recent posts."
                    recentPostLoadingStatus = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    recentPosts = snapshot.documents.map { document ->
                        SocialPost(
                            id = document.id,
                            authorId = document.getString("authorId") ?: "",
                            username = document.getString("username") ?: "Unknown",
                            sport = document.getString("sport") ?: "",
                            message = document.getString("message") ?: "",
                            createdAt = document.getTimestamp("createdAt")
                        )
                    }

                    recentPostsError = ""
                    recentPostLoadingStatus = false
                }
            }

        onDispose {
            listener.remove()
        }
    }


    // launched effect which is used to get the current user when the app is opened to the home page
    LaunchedEffect(Unit) {

        val user = auth.currentUser

        if (user != null) {

            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->

                    firstName = document.getString("firstName") ?: ""
                }
        }
    }

    HomeScreen(
        firstName = firstName,
        onHomeClick = onHomeClick,
        onBasketballClick = onBasketballClick,
        onTennisClick = onTennisClick,
        onPickleballClick = onPickleballClick,
        onProfileClick = onProfileClick,
        onConnectClick = onConnectClick,
        recentPosts = recentPosts,
        recentPostLoadingStatus = recentPostLoadingStatus,
        recentPostsError = recentPostsError
    )
}


// HOME SCREEN UI
// Does not directly use Firebase, so Preview can display it
@Composable
fun HomeScreen(
    firstName: String,
    onHomeClick: () -> Unit,
    onBasketballClick: () -> Unit,
    onTennisClick: () -> Unit,
    onPickleballClick: () -> Unit,
    onProfileClick: () -> Unit,
    onConnectClick: () -> Unit,
    recentPosts: List<SocialPost> = emptyList(),
    recentPostLoadingStatus: Boolean = false,
    recentPostsError: String = ""
) {

    // https://developer.android.com/develop/ui/compose/quick-guides/content/create-scaffold
    // use a scaffold in order to hold the court connect logo at top and the navigation between
    // sports pages at the bottom
    Scaffold(
        topBar = {
            Box {
                LogoTopApp()

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = 24.dp, end = 12.dp)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "My Account",
                        tint = Color(0xFF5B9808),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        bottomBar = {
            SportsNavigationBottom(
                selectedRoute = "home",
                onHomeClick = onHomeClick,
                onTennisClick = onTennisClick,
                onBasketballClick = onBasketballClick,
                onPickleballClick = onPickleballClick,
                onConnectClick = onConnectClick
            )
        }
    ) { // inner padding which refers to the inner dimensions of the scaffold
        innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {

                Text(
                    text = "Welcome back to the Court, $firstName!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Ready to get back out there and meet some new opponents or maybe some new friends",
                    fontSize = 16.sp,
                    modifier = Modifier.wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            //
            // Tennis card
            SportsCardCreate(
                title = "🎾 Tennis",
                description = "Find tennis courts near you",
                onClick = onTennisClick
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Basketball card
            SportsCardCreate(
                title = "🏀 Basketball",
                description = "Find basketball courts near you",
                onClick = onBasketballClick
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Pickleball card
            SportsCardCreate(
                title = "🥒🏸 Pickleball",
                description = "Find pickleball courts near you",
                onClick = onPickleballClick
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

                Text(
                    text = "Community",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "See what players are up to",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )


                when {
                    recentPostLoadingStatus -> {
                        Text(
                            text = "Loading recent posts...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    recentPostsError.isNotEmpty() -> {
                        Text(
                            text = recentPostsError,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }

                    recentPosts.isEmpty() -> {
                        Text(
                            text = "No community posts yet.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    else -> {
                        recentPosts.forEach { post ->
                            SocialPostCard(post = post)

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onConnectClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "See All Posts →",
                        color = Color(0xFF5B9808),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }
    }
}


// https://developer.android.com/develop/ui/compose/components/card
// create the cards in the center of the screen which can be used to navigate to the sports pages
@Composable
fun SportsCardCreate(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    // create a generic card, which can then be used to hold the information regarding each
    // sport
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(100.dp),


        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4A7000)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),

            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "$description →",
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

// https://developer.android.com/develop/ui/compose/components/navigation-bar
// navigation bar which is used to move between the different sports pages
// this remains at bottom of screen through all home screen pages once logged in

@Composable
fun SportsNavigationBottom(
    selectedRoute: String,
    onHomeClick: () -> Unit,
    onTennisClick: () -> Unit,
    onBasketballClick: () -> Unit,
    onPickleballClick: () -> Unit,
    onConnectClick: () -> Unit
) {

    Column {

        HorizontalDivider(
            thickness = 2.dp,
            color = Color(0xFF4A7000)
        )

        NavigationBar(
            containerColor = Color.White
        ) {

            // Home navigation
            NavigationBarItem(
                selected = selectedRoute == "home",
                onClick = onHomeClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home"
                    )
                },
                label = {
                    Text("Home")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF6A9F01),
                    indicatorColor = Color(0xFF6A9F01)
                )
            )

            // Tennis navigation
            NavigationBarItem(
                selected = selectedRoute == "tennis",
                onClick = onTennisClick,
                icon = {
                    Text("🎾")
                },
                label = {
                    Text("Tennis")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF6A9F01),
                    indicatorColor = Color(0xFF6A9F01)
                )
            )

            // Basketball navigation
            NavigationBarItem(
                selected = selectedRoute == "basketball",
                onClick = onBasketballClick,
                icon = {
                    Text("🏀")
                },
                label = {
                    Text("Basketball")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF6A9F01),
                    indicatorColor = Color(0xFF6A9F01)
                )
            )

            // Pickleball navigation
            NavigationBarItem(
                selected = selectedRoute == "pickleball",
                onClick = onPickleballClick,
                icon = {
                    Text("🏓")
                },
                label = {
                    Text("Pickleball")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF4A7000),
                    indicatorColor = Color(0xFF4A7000)
                )
            )

            // Connect navigation - will be implemented later
            NavigationBarItem(
                selected = selectedRoute == "connect",
                onClick = onConnectClick,
                icon = {
                    Text("🛜")
                },
                label = {
                    Text("Connect")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF4A7000),
                    indicatorColor = Color(0xFF4A7000)
                )
            )
        }
    }
}


// used to hold the logo at the top of all the screens, this aspect remains at top of page for entire
// time while logged in
@Composable
fun LogoTopApp() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        Image(
            painter = painterResource(id = R.drawable.rezised_logo),
            contentDescription = "CourtConnect Logo",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(85.dp)
        )

        HorizontalDivider(
            thickness = 2.dp,
            color = Color(0xFF4A7000)
        )
    }
}
