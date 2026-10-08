package com.example.courtconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.courtconnect.screens.ui.BasketballScreen
import com.example.courtconnect.screens.ui.CreateAccountScreen
import com.example.courtconnect.screens.ui.ForgotPasswordScreen
import com.example.courtconnect.screens.ui.HomeScreenWithFirebase
import com.example.courtconnect.screens.ui.LoginScreen
import com.example.courtconnect.screens.ui.PickleBallScreen
import com.example.courtconnect.screens.ui.TennisScreen
import com.example.courtconnect.ui.theme.CourtConnectTheme
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

                // https://developer.android.com/guide/navigation/backstack/multi-back-stacks
                fun navigateToTab(route: String) {
                    navController.navigate(route) {
                        popUpTo("home") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

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
                            onHomeClick = {},
                            onBasketballClick = {
                                navigateToTab("basketball")
                            },
                            onTennisClick = {
                                navigateToTab("tennis")
                            },
                            onPickleballClick = {
                                navigateToTab("pickleball")
                            }
                        )
                    }
                    composable("basketball") {
                        BasketballScreen(
                            onHomeClick = {
                                navigateToTab("home")
                            },
                            onTennisClick = {
                                navigateToTab("tennis")
                            },
                            onBasketballClick = {},
                            onPickleballClick = {
                                navigateToTab("pickleball")
                            }
                        )
                    }

                    composable("tennis") {
                        TennisScreen(
                            onHomeClick = {
                                navigateToTab("home")
                            },
                            onTennisClick = {},
                            onBasketballClick = {
                                navigateToTab("basketball")
                            },
                            onPickleballClick = {
                                navigateToTab("pickleball")
                            }
                        )
                    }

                    composable("pickleball") {
                        PickleBallScreen(
                            onHomeClick = {
                                navigateToTab("home")
                            },
                            onTennisClick = {
                                navigateToTab("tennis")
                            },
                            onBasketballClick = {
                                navigateToTab("basketball")
                            },
                            onPickleballClick = {}
                        )
                    }
                }
            }
        }
    }
}