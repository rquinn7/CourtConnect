package com.example.courtconnect.screens.ui

import android.R.attr.shadowColor
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.courtconnect.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect


@Composable
fun HomeScreen() {

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

    Scaffold(
        bottomBar = {
            CourtConnectBottomBar()
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )

        {

            // displays logo
            Image(
                painter = painterResource(id = R.drawable.logo_bg_remove),
                contentDescription = "CourtConnect Logo",
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .size(150.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Welcome back to the Court, $firstName!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ready to get back out there and meet some new opponents or maybe some new friends",
                    fontSize = 16.sp,
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))


            SportCard(
                title = "🎾 Tennis",
                description = "Find tennis courts near you",
                onClick = {
                    // Navigate to Tennis page
                }
            )

            Spacer(modifier = Modifier.height(6.dp))


            SportCard(
                title = "🏀 Basketball",
                description = "Find basketball courts near you",
                onClick = {
                    // Navigate to Basketball page
                }
            )

            Spacer(modifier = Modifier.height(6.dp))


            SportCard(
                title = "🥒🏸 Pickleball",
                description = "Find Pickleball courts near you",
                onClick = {
                    // Navigate to Pickleball page
                }
            )

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
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4A7000)
        )

    )
    {
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
            }
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
    }
}