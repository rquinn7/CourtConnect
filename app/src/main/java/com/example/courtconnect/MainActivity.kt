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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CourtConnectTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onCreateAccountClick = {
                                navController.navigate("createAccount")
                            },
                            onForgotPasswordClick = {
                                navController.navigate("forgotPassword")
                            }
                        )
                    }

                    composable("createAccount") {
                        CreateAccountScreen(
                            onLoginClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("forgotPassword") {
                        ForgotPasswordScreen(
                            onBackToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}