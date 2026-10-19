
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

private const val TWO_HOURS_MS = 2L * 60 * 60 * 1000

@Composable
fun CourtCheckInSection(courtId: String) {

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    val userId = auth.currentUser?.uid

    val playersRef = remember(db, courtId) {
        db.collection("courtCheckIns")
            .document(courtId)
            .collection("players")
    }

    var checkIns by remember(courtId) {
        mutableStateOf<Map<String, Timestamp>>(emptyMap())
    }

    var currentTime by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    var isSaving by remember(courtId) {
        mutableStateOf(false)
    }

    var errorMessage by remember(courtId) {
        mutableStateOf("")
    }

    // Update the current time every minute.
    // This allows expired check-ins to stop counting.
    LaunchedEffect(courtId) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    // Listen for check-in changes in Firebase.
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

    // Exclude check-ins that have expired.
    val activeCheckIns = checkIns.filterValues { expiresAt ->
        expiresAt.toDate().time > currentTime
    }

    val playerCount = activeCheckIns.size

    // Check whether the current user is checked in.
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

                            // Remove the user's check-in.
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

                            // Check-in expires after two hours.
                            val expiresAt = Timestamp(
                                Date(
                                    System.currentTimeMillis() +
                                            TWO_HOURS_MS
                                )
                            )

                            val data = hashMapOf(
                                "userId" to userId,
                                "checkedInAt" to
                                        FieldValue.serverTimestamp(),
                                "expiresAt" to expiresAt
                            )

                            // Save or replace the user's check-in.
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
