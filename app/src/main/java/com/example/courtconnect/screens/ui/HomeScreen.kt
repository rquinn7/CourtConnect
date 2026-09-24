package com.example.courtconnect.screens.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.courtconnect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// REAL APP VERSION
// Gets the user's first name from Firebase
@Composable
fun HomeScreenWithFirebase() {

    var firstName by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

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
        firstName = firstName
    )
}


// HOME SCREEN UI
// Does not directly use Firebase, so Preview can display it
@Composable
fun HomeScreen(
    firstName: String
) {

    Scaffold(
        topBar = {
            CourtConnectTopBar()
        },
        bottomBar = {
            CourtConnectBottomBar()
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
                .padding(innerPadding)
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

            SportCard(
                title = "🎾 Tennis",
                description = "Find tennis courts near you",
            onClick = {
                    // Navigate to Tennis page
                }
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            SportCard(
                title = "🏀 Basketball",
                description = "Find basketball courts near you",
                onClick = {
                    // Navigate to Basketball page
                }
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            SportCard(
                title = "🥒🏸 Pickleball",
                description = "Find pickleball courts near you",
                onClick = {
                    // Navigate to Pickleball page
                }
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

                CommunityPost(
                    name = "Alex",
                    sport = "🏀 Basketball",
                    message = "Anyone looking to play basketball tonight?"
                )


                CommunityPost(
                    name = "Joe",
                    sport = "🎾 Tennis",
                    message = "Anyone looking to play tennis tonight?"
                )
            }
        }
    }
}


@Composable
fun SportCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {

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


@Composable
fun CourtConnectBottomBar() {

    Column{
    HorizontalDivider(
        thickness = 2.dp,
        color = Color(0xFF4A7000)
    )

    NavigationBar(
        containerColor = Color.White
    ) {

        NavigationBarItem(
            selected = true,
            onClick = {
                // Home navigation later
            },
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
                selectedTextColor = Color(0xFF4A7000),
                indicatorColor = Color(0xFF4A7000),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                // Tennis navigation later
            },
            icon = {
                Text("🎾")
            },
            label = {
                Text("Tennis")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                // Basketball navigation later
            },
            icon = {
                Text("🏀")
            },
            label = {
                Text("Basketball")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                // Pickleball navigation later
            },
            icon = {
                Text("🏓")
            },
            label = {
                Text("Pickleball")
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                // Social navigation later
            },
            icon = {
                Text("🛜")
            },
            label = {
                Text("Connect")
            }
        )
    }
}
}

@Composable
fun CommunityPost(
    name: String,
    sport: String,
    message: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = sport,
                fontSize = 13.sp,
                color = Color(0xFF4A7000)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = message,
                fontSize = 15.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "♡ Like     💬 Comment",
                fontSize = 13.sp,
                color = Color.Gray
            )

        }
    }
}


// PREVIEW VERSION
// Uses fake data instead of Firebase
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomeScreenPreview() {

    HomeScreen(
        firstName = "Ryan"
    )
}

@Composable
fun CourtConnectTopBar() {

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