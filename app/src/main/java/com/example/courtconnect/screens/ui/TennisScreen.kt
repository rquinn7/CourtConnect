package com.example.courtconnect.screens.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun TennisScreen(
    onHomeClick: () -> Unit,
    onTennisClick: () -> Unit,
    onBasketballClick: () -> Unit,
    onPickleballClick: () -> Unit,
    onConnectClick: () -> Unit
) {

    // used to hold the locations of the Tennis courts
    var tennisCourts by remember {
        mutableStateOf<List<Place>>(emptyList())
    }

    // used to hold the user's location
    var userLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    // used to center the camera position on the center of the map
    val cameraPositionState = rememberCameraPositionState()

    // used to link the current Android context with the compose app features
    val context = LocalContext.current

    // ask to get location permissions from the user, during their first time running the application
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // used to launch the window which asks for the user's permission to see their location
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasLocationPermission = granted
        }

    // https://developers.google.com/android/reference/com/google/android/gms/location/LocationServices
    // need this and the code below in order to use the google locations api services
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // https://developers.google.com/maps/documentation/places/android-sdk/reference/com/google/android/libraries/places/api/Places
    val placesClient = remember {
        Places.createClient(context)
    }

    @SuppressLint("MissingPermission")
    // need to use the launched effect which is controlled by when the user
    // selects whether or not they want to allow their location to be seen
    LaunchedEffect(hasLocationPermission) {
        // need to check if the location permissions were allowed on the app
        // if they were the map will be setup
        if (hasLocationPermission) {
            // obtain the location of the device
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    // as long as there is a location
                    if (location != null) {
                        // convert the current location into coordinates the Google api can use
                        val currentLocation = LatLng(
                            location.latitude,
                            location.longitude
                        )

                        // set the user location to the current location
                        userLocation = currentLocation

                        // position the camera to show a certain radius (in this case a larger area)
                        // which can then show the courts in the area
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(
                                currentLocation,
                                13f
                            )

                        // used when telling the Google Places API what information is needed when
                        // running the API, in this case, the ID, name, location, address and rating
                        val placeFields = listOf(
                            Place.Field.ID,
                            Place.Field.DISPLAY_NAME,
                            Place.Field.LOCATION,
                            Place.Field.FORMATTED_ADDRESS,
                            Place.Field.RATING
                        )

                        // create the search area for the Tennis courts in the area
                        // this is around 10 miles
                        val searchArea = CircularBounds.newInstance(
                            currentLocation,
                            16000.0
                        )

                        // send the request/api call to find places with the name Tennis court
                        // https://developers.google.com/maps/documentation/places/android-sdk/text-search
                        val request = SearchByTextRequest.builder(
                            "tennis court",
                            placeFields
                        )
                            // sets the location to search to the search area, based on current location
                            .setLocationBias(searchArea)
                            // find 20 courts based on the given area, could be less than 20
                            .setMaxResultCount(20)
                            .build()

                        // pull the information from the API call and put it into
                        // the Tennis courts variable from earlier
                        placesClient.searchByText(request)
                            .addOnSuccessListener { response ->

                                tennisCourts = response.places

                            }
                            // used to catch ant failed searches or requests
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

    // https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/sorted-by.html
    // code below is used for sorting the lists of Tennis courts based on their location to the
    // user
    val sortedTennisCourts = remember(
        tennisCourts,
        userLocation
    ) {
        val location = userLocation

        if (location != null) {
            tennisCourts.sortedBy { court ->

                court.location?.let { courtLocation ->
                    calculateDistanceBetween(
                        location,
                        courtLocation
                    )
                } ?: Float.MAX_VALUE
            }
        } else {
            tennisCourts
        }
    }

    // create the interface with the same scaffold as before
    Scaffold(
        topBar = {
            LogoTopApp()
        },
        bottomBar = {
            SportsNavigationBottom(
                selectedRoute = "tennis",
                onHomeClick = onHomeClick,
                onTennisClick = onTennisClick,
                onBasketballClick = onBasketballClick,
                onPickleballClick = onPickleballClick,
                onConnectClick = onConnectClick
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Ready to play some Tennis?",
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

            // https://developers.google.com/maps/documentation/android-sdk/maps-compose?authuser=945769740
            // https://developers.google.com/maps/documentation/android-sdk/maps-compose#markers
            // create the map which shows the map and markers
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                )
            ) {

                tennisCourts.forEach { court ->

                    // as long as the location being found is not null
                    // then run the code below and create the markers
                    court.location?.let { courtLocation ->

                        // creates the markers on the map
                        // anywhere there is a ?: - this means if certain title is not found
                        // used generic name Tennis court instead
                        Marker(
                            state = rememberUpdatedMarkerState(
                                position = courtLocation
                            ),
                            title = court.displayName ?: "Tennis Court",
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
                text = "Nearby Tennis Courts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 12.dp,
                    bottom = 8.dp
                ),
                textAlign = TextAlign.Center
            )
            // https://developer.android.com/develop/ui/compose/lists?authuser=1&hl=en
            // use a lazy column in order to create the scrollable list of courts based on distance
            // to the user
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // use code below which is similar to running a function
                // where each court is compared based on distance and if it is
                // the shortest distance then create a card for the court
                items(sortedTennisCourts) { court ->

                    val distance = if (
                        userLocation != null &&
                        court.location != null
                    ) {
                        calculateDistanceBetween(
                            userLocation!!,
                            court.location!!
                        )
                    } else {
                        null
                    }

                    TennisCourtCard(
                        court = court,
                        distanceMiles = distance
                    )
                }
            }
        }
    }
}

// https://developer.android.com/develop/ui/compose/components/card?authuser=96
// https://developer.android.com/reference/kotlin/androidx/compose/material3/CardDefaults
// function used to create and hold the information about each court
// in the list under the map
@Composable
fun TennisCourtCard(
    court: Place,
    distanceMiles: Float?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDCDBDB)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // display the name of the court
            Text(
                text = court.displayName ?: "Tennis Court",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // display the address of the court
            court.formattedAddress?.let { address ->
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // create a seperate row which holds the rating and distance of the
            // court from the user
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                court.rating?.let { rating ->
                    Text(
                        text = "⭐ $rating",
                        fontSize = 14.sp
                    )
                }

                distanceMiles?.let { distance ->
                    Text(
                        text = "%.1f miles away".format(distance),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            court.id?.let { placeId ->
                CourtCheckInSection(courtId = placeId)
            } ?: Text(
                text = "Check-in unavailable for this court",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}



