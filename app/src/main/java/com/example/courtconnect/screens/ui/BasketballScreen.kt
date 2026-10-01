package com.example.courtconnect.screens.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.courtconnect.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState

import android.annotation.SuppressLint
import android.util.Log

import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun BasketballScreen() {

    var basketballCourts by remember {
        mutableStateOf<List<Place>>(emptyList())
    }

    val cameraPositionState = rememberCameraPositionState()

    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasLocationPermission = granted
        }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val placesClient = remember {
        Places.createClient(context)
    }

    @SuppressLint("MissingPermission")
    LaunchedEffect(hasLocationPermission) {

        if (hasLocationPermission) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {

                        val userLocation = LatLng(
                            location.latitude,
                            location.longitude
                        )

                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(
                                userLocation,
                                13f
                            )

                        val placeFields = listOf(
                            Place.Field.ID,
                            Place.Field.DISPLAY_NAME,
                            Place.Field.LOCATION,
                            Place.Field.FORMATTED_ADDRESS,
                            Place.Field.RATING
                        )

                        val searchArea = CircularBounds.newInstance(
                            userLocation,
                            16000.0
                        )

                        val request = SearchByTextRequest.builder(
                            "basketball court",
                            placeFields
                        )
                            .setLocationBias(searchArea)
                            .setMaxResultCount(20)
                            .build()

                        placesClient.searchByText(request)
                            .addOnSuccessListener { response ->

                                basketballCourts = response.places

                            }
                            .addOnFailureListener { exception ->

                                Log.e(
                                    "CourtConnect",
                                    "Court search failed",
                                    exception
                                )
                            }
                    }
                }
        }
    }


    Scaffold(
        topBar = {
            LogoTopApp()
        },
        bottomBar = {
            SportsNavigationBottom()
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {

            // Basketball page content will go here

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Ready to shoot some Hoops?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 35.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Check the map below to find courts near you?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = TextAlign.Center
            )

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                )
            ) {

                basketballCourts.forEach { court ->

                    court.location?.let { courtLocation ->

                        Marker(
                            state = rememberUpdatedMarkerState(
                                position = courtLocation
                            ),
                            title = court.displayName ?: "Basketball Court",
                            snippet = buildString {
                                court.formattedAddress?.let {
                                    append(it)
                                }

                                court.rating?.let {
                                    if (isNotEmpty()) append("\n")
                                    append("⭐ $it")
                                }
                            }
                        )
                    }
                }
            }
            Text(
                text = "Nearby Basketball Courts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 12.dp,
                    bottom = 8.dp
                ),
                textAlign = TextAlign.Center
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(basketballCourts) { court ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            )
                    ) {

                        Text(
                            text = court.displayName ?: "Basketball Court",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        court.rating?.let { rating ->
                            Text(
                                text = "⭐ $rating",
                                fontSize = 14.sp
                            )
                        }

                        court.formattedAddress?.let { address ->
                            Text(
                                text = address,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

    }
}

