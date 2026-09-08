@file:Suppress("DEPRECATION")

package com.example.courtconnect.screens.ui

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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

//@Preview(showSystemUi = true)
@Composable
fun LoginScreen(
    onCreateAccountClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember() { mutableStateOf("") }
    var password by remember() { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            Image(
                painter = painterResource(id = R.drawable.logo_bg_remove),
                contentDescription = "CourtConnect Logo",
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .size(300.dp)
            )

            OutlinedTextField(
                modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5B9808),
                    focusedLabelColor = Color(0xFF5B9808)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )


            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5B9808),
                    focusedLabelColor = Color(0xFF5B9808)
                ),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Show password"
                        )
                    }
                }

            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    // login logic will go here later
                },
                enabled = Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9808)
                )
            ) {
                Text("Login")
            }

            Spacer(Modifier.height(12.dp))


            ClickableText(
                text = AnnotatedString("Create an Account"),
                style = TextStyle(
                    color = Color(0xFFFF5722)
                ),
                onClick = { offset ->
                    Log.d("ClickableText", "$offset -th character is clicked.")
                    onCreateAccountClick()
                }
            )

            Spacer(Modifier.height(20.dp))

            ClickableText(
                text = AnnotatedString("Forgot Password"),
                style = TextStyle(
                    color = Color(0xFFFF5722)
                ),
                onClick = { offset ->
                    Log.d("ClickableText", "$offset -th character is clicked.")
                    onForgotPasswordClick()
                }
            )

            Spacer(Modifier.height(50.dp))

        }
    }

