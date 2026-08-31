package com.example.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.firebase.FirebaseManager
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD_LOOKUP,
    FORGOT_PASSWORD_OTP,
    FORGOT_PASSWORD_RESET
}

@Composable
fun AuthScreen(
    onAuthSuccess: (uid: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Form fields
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordOrPin by remember { mutableStateOf("") }
    var confirmPasswordOrPin by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    // Recovery fields
    var recoveryUid by remember { mutableStateOf("") }
    var recoveryPhone by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPasswordOrPin by remember { mutableStateOf("") }
    var phoneAuthCredential by remember { mutableStateOf<PhoneAuthCredential?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val configStatus by FirebaseManager.configStatus.collectAsState()
    val isFirebaseAvailable by FirebaseManager.isFirebaseAvailable.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Header
            Text(
                text = "METOU KinSphere",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryNeon,
                letterSpacing = 1.sp
            )
            Text(
                text = "Real Connections • Real Profiles • Live Network",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Firebase Configuration Banner if not configured
            if (configStatus !is FirebaseManager.FirebaseConfigStatus.Configured) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF24180A)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5A00D))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ Real Firebase Auth Connection Required",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFACC15),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The app is wired to real Firebase Authentication. To authenticate live users:",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Firebase project: omniverse-tqs6o\n   Package: com.shadow.metou\n2. Enable Email/Password & Phone in Firebase Authentication\n3. Enable Cloud Firestore & Storage in Firebase Console",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Tab Selector for Sign In / Sign Up
                    if (authMode == AuthMode.SIGN_IN || authMode == AuthMode.SIGN_UP) {
                        TabRow(
                            selectedTabIndex = if (authMode == AuthMode.SIGN_IN) 0 else 1,
                            containerColor = Color.Transparent,
                            contentColor = PrimaryNeon
                        ) {
                            Tab(
                                selected = authMode == AuthMode.SIGN_IN,
                                onClick = {
                                    authMode = AuthMode.SIGN_IN
                                    errorMessage = null
                                    infoMessage = null
                                },
                                text = {
                                    Text(
                                        "Sign In",
                                        fontWeight = if (authMode == AuthMode.SIGN_IN) FontWeight.Bold else FontWeight.Normal,
                                        color = if (authMode == AuthMode.SIGN_IN) PrimaryNeon else TextMuted
                                    )
                                },
                                modifier = Modifier.testTag("auth_signin_tab")
                            )
                            Tab(
                                selected = authMode == AuthMode.SIGN_UP,
                                onClick = {
                                    authMode = AuthMode.SIGN_UP
                                    errorMessage = null
                                    infoMessage = null
                                },
                                text = {
                                    Text(
                                        "Create Account",
                                        fontWeight = if (authMode == AuthMode.SIGN_UP) FontWeight.Bold else FontWeight.Normal,
                                        color = if (authMode == AuthMode.SIGN_UP) PrimaryNeon else TextMuted
                                    )
                                },
                                modifier = Modifier.testTag("auth_signup_tab")
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    when (authMode) {
                        AuthMode.SIGN_IN -> {
                            // Username Input
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it.trim() },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_username_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Password/PIN Input
                            OutlinedTextField(
                                value = passwordOrPin,
                                onValueChange = { passwordOrPin = it },
                                label = { Text("Password or PIN (min 6 chars)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle visibility",
                                            tint = TextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        AuthMode.SIGN_UP -> {
                            // Username Input
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it.trim() },
                                label = { Text("Choose Username (letters, numbers, _)") },
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_signup_username_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Full Name / Display Name
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Display Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_signup_display_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password or PIN
                            OutlinedTextField(
                                value = passwordOrPin,
                                onValueChange = { passwordOrPin = it },
                                label = { Text("Create Password or PIN") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_signup_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Confirm Password or PIN
                            OutlinedTextField(
                                value = confirmPasswordOrPin,
                                onValueChange = { confirmPasswordOrPin = it },
                                label = { Text("Confirm Password or PIN") },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = TextMuted) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_signup_confirm_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bio
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Bio (Optional)") },
                                singleLine = false,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        AuthMode.FORGOT_PASSWORD_LOOKUP -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { authMode = AuthMode.SIGN_IN }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryNeon)
                                }
                                Text("Recover Account", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                            }
                            Text(
                                "Enter your username to recover access to your account.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it.trim() },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("recovery_username_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = {
                                    if (username.isBlank()) {
                                        errorMessage = "Please enter your username first."
                                        return@TextButton
                                    }
                                    coroutineScope.launch {
                                        isLoading = true
                                        val resetRes = FirebaseManager.sendPasswordReset(username)
                                        resetRes.fold(
                                            onSuccess = {
                                                infoMessage = "Password reset email sent to account credentials."
                                                errorMessage = null
                                            },
                                            onFailure = { ex ->
                                                errorMessage = ex.localizedMessage ?: "Failed to send reset link."
                                            }
                                        )
                                        isLoading = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Send Password Reset Email", color = PrimaryNeon, fontSize = 12.sp)
                            }
                        }

                        AuthMode.FORGOT_PASSWORD_OTP -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { authMode = AuthMode.FORGOT_PASSWORD_LOOKUP }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryNeon)
                                }
                                Text("Enter SMS Code", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                            }
                            Text(
                                "SMS verification code sent to recovery phone ending in ${recoveryPhone.takeLast(4)}.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it.trim() },
                                label = { Text("6-Digit SMS Code") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("recovery_otp_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        AuthMode.FORGOT_PASSWORD_RESET -> {
                            Text("Create New Password / PIN", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                            Text(
                                "Enter your new credentials for username @$username.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            OutlinedTextField(
                                value = newPasswordOrPin,
                                onValueChange = { newPasswordOrPin = it },
                                label = { Text("New Password or PIN") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("recovery_new_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryNeon,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Error Message Display
                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    // Info Message Display
                    AnimatedVisibility(visible = infoMessage != null) {
                        Text(
                            text = infoMessage ?: "",
                            color = PrimaryNeon,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            errorMessage = null
                            infoMessage = null

                            when (authMode) {
                                AuthMode.SIGN_IN -> {
                                    if (username.isBlank() || passwordOrPin.isBlank()) {
                                        errorMessage = "Please enter both username and password/PIN."
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        isLoading = true
                                        val result = FirebaseManager.loginWithUsername(username, passwordOrPin)
                                        result.fold(
                                            onSuccess = { user -> onAuthSuccess(user.uid) },
                                            onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Sign in failed" }
                                        )
                                        isLoading = false
                                    }
                                }

                                AuthMode.SIGN_UP -> {
                                    if (username.isBlank() || passwordOrPin.isBlank()) {
                                        errorMessage = "Please enter a username and password/PIN."
                                        return@Button
                                    }
                                    if (passwordOrPin != confirmPasswordOrPin) {
                                        errorMessage = "Passwords do not match."
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        isLoading = true
                                        val result = FirebaseManager.registerWithUsername(
                                            rawUsername = username,
                                            passwordOrPin = passwordOrPin,
                                            displayName = displayName,
                                            bio = bio
                                        )
                                        result.fold(
                                            onSuccess = { user -> onAuthSuccess(user.uid) },
                                            onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Account creation failed" }
                                        )
                                        isLoading = false
                                    }
                                }

                                AuthMode.FORGOT_PASSWORD_LOOKUP -> {
                                    if (username.isBlank()) {
                                        errorMessage = "Please enter your username."
                                        return@Button
                                    }
                                    val activity = context as? Activity
                                    if (activity == null) {
                                        errorMessage = "Device context unavailable."
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isLoading = true
                                        val lookupRes = FirebaseManager.checkAccountRecoveryStatus(username)
                                        lookupRes.fold(
                                            onSuccess = { (uid, phone) ->
                                                recoveryUid = uid
                                                if (phone.isNullOrBlank()) {
                                                    errorMessage = "No recovery method is linked to this account."
                                                    isLoading = false
                                                } else {
                                                    recoveryPhone = phone
                                                    FirebaseManager.sendPhoneOtpForLinking(
                                                        activity = activity,
                                                        phoneNumber = phone,
                                                        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                                phoneAuthCredential = credential
                                                                authMode = AuthMode.FORGOT_PASSWORD_RESET
                                                                isLoading = false
                                                            }

                                                            override fun onVerificationFailed(e: FirebaseException) {
                                                                errorMessage = "SMS failed: ${e.localizedMessage}"
                                                                isLoading = false
                                                            }

                                                            override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                                                verificationId = vId
                                                                authMode = AuthMode.FORGOT_PASSWORD_OTP
                                                                isLoading = false
                                                            }
                                                        }
                                                    )
                                                }
                                            },
                                            onFailure = { ex ->
                                                errorMessage = ex.localizedMessage ?: "Failed to find account"
                                                isLoading = false
                                            }
                                        )
                                    }
                                }

                                AuthMode.FORGOT_PASSWORD_OTP -> {
                                    if (otpCode.length < 6) {
                                        errorMessage = "Please enter the 6-digit code."
                                        return@Button
                                    }
                                    val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                                    phoneAuthCredential = credential
                                    authMode = AuthMode.FORGOT_PASSWORD_RESET
                                }

                                AuthMode.FORGOT_PASSWORD_RESET -> {
                                    if (newPasswordOrPin.length < 6) {
                                        errorMessage = "New password/PIN must be at least 6 characters."
                                        return@Button
                                    }
                                    val cred = phoneAuthCredential
                                    if (cred == null) {
                                        errorMessage = "Authentication session expired. Please retry."
                                        authMode = AuthMode.FORGOT_PASSWORD_LOOKUP
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        isLoading = true
                                        val resetRes = FirebaseManager.resetPasswordWithPhoneCredential(username, cred, newPasswordOrPin)
                                        resetRes.fold(
                                            onSuccess = {
                                                infoMessage = "Password reset successfully! Please sign in."
                                                authMode = AuthMode.SIGN_IN
                                                passwordOrPin = ""
                                            },
                                            onFailure = { ex ->
                                                errorMessage = ex.localizedMessage ?: "Failed to reset password"
                                            }
                                        )
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text(
                                text = when (authMode) {
                                    AuthMode.SIGN_IN -> "Sign In"
                                    AuthMode.SIGN_UP -> "Create Permanent Account"
                                    AuthMode.FORGOT_PASSWORD_LOOKUP -> "Find Account"
                                    AuthMode.FORGOT_PASSWORD_OTP -> "Verify SMS Code"
                                    AuthMode.FORGOT_PASSWORD_RESET -> "Set New Password & Sign In"
                                },
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Forgot Password link (Sign In mode)
                    if (authMode == AuthMode.SIGN_IN) {
                        TextButton(
                            onClick = {
                                authMode = AuthMode.FORGOT_PASSWORD_LOOKUP
                                errorMessage = null
                                infoMessage = null
                            },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                                .testTag("forgot_password_button")
                        ) {
                            Text("Forgot password/PIN?", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
