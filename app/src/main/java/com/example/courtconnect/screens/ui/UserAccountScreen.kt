package com.example.courtconnect.screens.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun UserAccountScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // since pulling information from firebase need to link to firebase/firestore db
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // information which is pulled from the user accounts
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    // boolean used to tell if the user information data is loading or not
    var userInfoLoadingStatus by remember { mutableStateOf(true) }
    // holds if there is an error when pulling the data from firebase
    var errorMessage by remember { mutableStateOf("") }


    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://developer.android.com/develop/ui/compose/side-effects
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // used code above in combination with AI in order to pull information from firebase
    // and then be able to then display it to the user
    // the launched effect means it opens when the user clicks the button to enter this screen
    LaunchedEffect(auth.currentUser?.uid) {
        val user = auth.currentUser

        if (user == null) {
            errorMessage = "You are not logged in."
            userInfoLoadingStatus = false
        } else {
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstName = document.getString("firstName") ?: ""
                        lastName = document.getString("lastName") ?: ""
                        username = document.getString("username") ?: ""
                        email = document.getString("email")
                            ?: user.email.orEmpty()
                    } else {
                        errorMessage = "Profile information not found."
                    }
                    userInfoLoadingStatus = false
                }
                .addOnFailureListener {
                    errorMessage = "Unable to load profile."
                    userInfoLoadingStatus = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // button user to go back to home screen
            IconButton(
                onClick = onBackClick,
                modifier = Modifier

                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "My Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (userInfoLoadingStatus) {
            CircularProgressIndicator(color = Color(0xFF5B9808))
        } else if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        } else {

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        Color(0xFF5B9808).copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF5B9808),
                    modifier = Modifier.size(65.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (username.isNotBlank()) {
                    "@$username"
                } else {
                    "No username set"
                },
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$firstName $lastName".trim(),
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Account Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileInfoRow("Username", "@$username")
                    HorizontalDivider()
                    ProfileInfoRow(
                        "Name",
                        "$firstName $lastName".trim()
                    )
                    HorizontalDivider()
                    ProfileInfoRow("Email", email)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    auth.signOut()
                    onLogoutClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9808)
                )
            ) {
                Text(
                    text = "Log Out",
                    fontSize = 16.sp
                )
            }
        }
    }
}

// create a function which is used to hold all the profile information in one row
// this makes it much more clear for the user
@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
