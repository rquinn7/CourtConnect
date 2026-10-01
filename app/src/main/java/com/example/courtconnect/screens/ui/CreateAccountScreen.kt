@file:Suppress("DEPRECATION")

package com.example.courtconnect.screens.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CreateAccountScreen(
    onLoginClick: () -> Unit
) {
    // variables used to hold first name, last name, email,
    // password, password visible, password confirmed and
    // password error
    var firstName by remember() { mutableStateOf("") }
    var lastName by remember() { mutableStateOf("") }
    var email by remember() {mutableStateOf("")}
    var password by remember() {mutableStateOf("")}
    // displays the password which is entered in the textbox
    var passwordVisible by remember { mutableStateOf(false) }
    // used to hold the confirmed password
    var passwordConfirmed by remember { mutableStateOf("") }
    // if the passwords do not match display an error
    var passwordError by remember {mutableStateOf(false)}
    // if the password does not satisfy requirements send an error
    var passwordRequirementError by remember { mutableStateOf(false) }

    // variables used for firebase
    // used to authorize account and access to firebase
    val auth = FirebaseAuth.getInstance()
    // used to connect to firestore which is database that holds all account information
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
            //.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        // display logo
        Image(
            painter = painterResource(id = R.drawable.logo_bg_remove),
            contentDescription = "CourtConnect Logo",
            modifier = Modifier
                .padding(bottom = 5.dp)
                .size(250.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Get started on CourtConnect",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
                )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create an account to find courts and connect with other players.",
                fontSize = 16.sp,
                modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),

                )
        }



        Spacer(modifier = Modifier.height(8.dp))

        // first name text field
        // https://developer.android.com/develop/ui/compose/text/user-input?textfield=state-based
        // https://developer.android.com/reference/kotlin/androidx/compose/material/OutlinedTextField.composable
        OutlinedTextField(
            modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5B9808),
                focusedLabelColor = Color(0xFF5B9808)
            )
        )

        // last name text field
        OutlinedTextField(
            modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5B9808),
                focusedLabelColor = Color(0xFF5B9808)
            )
        )

        // email text field
        OutlinedTextField(
            modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5B9808),
                focusedLabelColor = Color(0xFF5B9808)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
        Text(
            text = "Password must be at least 8 characters and include a number and special character.",
            fontSize = 16.sp,
            modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
        )
            }

        // password text field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            shape = RoundedCornerShape(25.dp),
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

        // password confirmed text field
        // used to make sure passwords match when creating account
        // if not display error, which is password error
        OutlinedTextField(
            value = passwordConfirmed,
            onValueChange = { passwordConfirmed = it },
            label = { Text("Confirm Password") },
            isError = passwordError,
            shape = RoundedCornerShape(25.dp),
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

        // boolean value used to check if the password has all the requirements needed for account
        // creation including atleast 8 characters, contains a digit and a special character
        // https://www.baeldung.com/kotlin/password-validation
        val passwordIsSecure =
            password.length >= 8 &&
                    password.any { it.isDigit() } &&
                    password.any { !it.isLetterOrDigit() }

        // if there is a password error send out a message
        if (passwordError) {
            Text(
                text = "Passwords do not match",
                color = Color.Red,
                fontSize = 14.sp
            )
        }



        Spacer(Modifier.height(10.dp))
        // button which is used to attempt to create the account
        Button(
            onClick = {

                // if the password does not satisfy the password requirements
                // display the error associated
                if (!passwordIsSecure) {
                    passwordRequirementError = true
                    passwordError = false
                }
                // if the password does not match the confirmed password
                // display the error associated
                else if (password != passwordConfirmed) {
                    passwordError = true
                    passwordRequirementError = false
                }
                // if the password satisfies all requirements move into creating the account
                // https://firebase.google.com/docs/auth/android/manage-users
                // https://firebase.google.com/docs/firestore/manage-data/add-data
                // followed and combined code from above with the help of AI in order to
                // create the accounts and store them in firestore
                else {
                    passwordError = false
                    passwordRequirementError = false

                    auth.createUserWithEmailAndPassword(
                        email,
                        password
                    ).addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            val user = auth.currentUser

                            if (user != null) {

                                val userData = hashMapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "email" to email
                                )

                                db.collection("users")
                                    .document(user.uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        onLoginClick()
                                    }

                            }

                        }
                    }
                }
            },

            // only enable the button if the email address is an email and all fields contain text
            enabled = Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    && password.isNotBlank()
                    && firstName.isNotBlank()
                    && lastName.isNotBlank()
                    && passwordConfirmed.isNotBlank(),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5B9808)
            ),

            modifier = Modifier
                .width(220.dp)
                .height(52.dp)

        ) {
            Text("Create Account")
        }

        // used when the password requirements are not met
        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            if (passwordRequirementError) {
                Text(
                    text = "Password must be at least 8 characters and include a number and special character.",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // clickable text used to go back to login page if the user remembers their password
        ClickableText(
            text = AnnotatedString("Already have an account? Login"),
            style = TextStyle(
                color = Color(0xFFFF5722)
            ),
            onClick = {
                onLoginClick()
            }
        )

    }
}

