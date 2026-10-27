
package com.example.courtconnect.screens.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun CourtCheckInSection(courtId: String) {

    // used for firebase authorization to make sure account is in firebase
    // also the db is used to access the firestore database which holds all users,
    // accounts, posts, etc...
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // used to hold the current user's id
    val userId = auth.currentUser?.uid


    // used to point to the current player and court in the firestore database
    val playersRef = remember(db, courtId) {
        db.collection("courtCheckIns")
            .document(courtId)
            .collection("players")
    }

    // used to hold the check-ins from firestore
    var checkIns by remember(courtId) {
        mutableStateOf<Map<String, Timestamp>>(emptyMap())
    }

    // used to hold the current time, which is used for checking people out automatically
    var currentTime by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    // boolean used to hold whether the check in is saving or not
    var isSaving by remember(courtId) {
        mutableStateOf(false)
    }

    // error message for displaying when checking into a court
    var errorMessage by remember(courtId) {
        mutableStateOf("")
    }

    // Update the current time every minute in order to accurately track check-ins and outs
    LaunchedEffect(courtId) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    // used to check for if a player has checked in to a court and then display it on the page
    DisposableEffect(playersRef) {

        val listener = playersRef
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    errorMessage = error.localizedMessage
                        ?: "Unable to load check-ins."
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    checkIns = snapshot.documents.mapNotNull { document ->

                        val expiresAt =
                            document.getTimestamp("expiresAt")

                        if (expiresAt != null) {
                            document.id to expiresAt
                        } else {
                            null
                        }

                    }.toMap()

                    errorMessage = ""
                }
            }

        onDispose {
            listener.remove()
        }
    }

    // used to hold active check-in totals
    val activeCheckIns = checkIns.filterValues { expiresAt ->
        expiresAt.toDate().time > currentTime
    }

    // used to hold the amount of players at a given court
    val playerCount = activeCheckIns.size

    // as long as the user has an ID and is checked in set this value
    val isCheckedIn = userId != null &&
            activeCheckIns.containsKey(userId)

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "👥 $playerCount players here",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Button(
                enabled = userId != null && !isSaving,
                onClick = {

                    if (userId != null) {

                        isSaving = true
                        errorMessage = ""

                        val userCheckIn = playersRef.document(userId)

                        if (isCheckedIn) {

                            // Remove the user's check-in when deleted
                            userCheckIn.delete()
                                .addOnSuccessListener {
                                    isSaving = false
                                }
                                .addOnFailureListener { exception ->
                                    errorMessage =
                                        exception.localizedMessage
                                            ?: "Unable to check out."
                                    isSaving = false
                                }

                        } else {

                            // remove player's check-ins after 2 hours
                            val expiresAt = Timestamp(
                                Date(
                                    System.currentTimeMillis() +
                                            2L * 60 * 60 * 1000
                                )
                            )

                            // holds the data for each check-in, holds the ID, and what time
                            // they checked-in and when it expires
                            val data = hashMapOf(
                                "userId" to userId,
                                "checkedInAt" to
                                        FieldValue.serverTimestamp(),
                                "expiresAt" to expiresAt
                            )

                            // sets the current check-in based on the information from above
                            userCheckIn.set(data)
                                .addOnSuccessListener {
                                    isSaving = false
                                }
                                .addOnFailureListener { exception ->
                                    errorMessage =
                                        exception.localizedMessage
                                            ?: "Unable to check in."
                                    isSaving = false
                                }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCheckedIn) {
                        Color(0xFF4A7000)
                    } else {
                        Color(0xFFFF5722)
                    }
                )
            ) {
                Text(
                    text = when {
                        isSaving -> "Saving..."
                        isCheckedIn -> "Check Out"
                        else -> "Check In"
                    }
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp
            )
        }
    }
}
