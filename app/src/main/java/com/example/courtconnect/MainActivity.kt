package com.example.courtconnect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.courtconnect.ui.theme.CourtConnectTheme
import com.google.firebase.FirebaseApp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.courtconnect.screens.ui.LoginScreen
import com.example.courtconnect.screens.ui.CreateAccountScreen
import com.example.courtconnect.screens.ui.ForgotPasswordScreen
import com.example.courtconnect.screens.ui.HomeScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import com.example.courtconnect.screens.ui.BasketballScreen
import com.example.courtconnect.screens.ui.TennisScreen
import com.example.courtconnect.screens.ui.PickleBallScreen
import com.example.courtconnect.screens.ui.HomeScreenWithFirebase
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // used to gain access to the places API from Google which is used to find the different
        // court locations
        val apiKeyPlaces = BuildConfig.PLACES_API_KEY

        // use the API key and the context within the app in order to ensure the places API is working
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                apiKeyPlaces
            )
        }

        val placesClient = Places.createClient(this)
        setContent {
            CourtConnectTheme {

                // create nav controller used to move around from login, create account, forgot password and home
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    // navigation from login
                    // login -> create account
                    // login -> forgot password
                    // login -> home screen
                    composable("login") {
                        LoginScreen(
                            onCreateAccountClick = {
                                navController.navigate("createAccount")
                            },
                            onForgotPasswordClick = {
                                navController.navigate("forgotPassword")
                            },
                            successfulLogin = {
                                navController.navigate("home") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    // create account navigation
                    composable("createAccount") {
                        CreateAccountScreen(
                            onLoginClick = {
                                // used to go back to previous screen after clicking button on page
                                navController.popBackStack()
                            }
                        )
                    }

                    // forgot password navigation
                    composable("forgotPassword") {
                        ForgotPasswordScreen(
                            onBackToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // home screen navigation
                    // no button to return to login screen so not needed here
                    // Home screen
                    composable("home") {
                        HomeScreenWithFirebase(
                            onBasketballClick = {
                                navController.navigate("basketball")
                            },
                            onTennisClick = {
                                navController.navigate("tennis")
                            },
                            onPickleballClick = {
                                navController.navigate("pickleball")
                            }
                        )
                    }

                    // Basketball screen
                    composable("basketball") {
                        BasketballScreen()
                    }

                    // Tennis screen
                    composable("tennis") {
                        TennisScreen()
                    }

                    // Pickleball screen
                    composable("pickleball") {
                        PickleBallScreen()
                    }
                }
            }
        }
    }
}