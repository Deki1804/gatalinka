package com.gatalinka.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import com.gatalinka.app.vm.AuthViewModel
import com.gatalinka.app.auth.GoogleSignInHelper
import com.gatalinka.app.util.ErrorMessages
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val authState by viewModel.authState.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Navigiraj na success kada se korisnik uspješno registrira
    LaunchedEffect(authState) {
        if (authState is com.gatalinka.app.vm.AuthState.Authenticated) {
            onRegisterSuccess()
        }
    }
    
    // Google Sign In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            val idToken = GoogleSignInHelper.getSignInResult(result.data)
            
            if (idToken != null) {
                viewModel.signInWithGoogleIdToken(idToken)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0B2E), // Mystic Purple Deep
                        Color(0xFF2D1B4E)  // Mystic Purple Medium
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "Registracija",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFFD700)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Nazad",
                            tint = Color(0xFFEFE3D1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Logo/Title
                Text(
                    text = "Gatalinka",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Kreiraj novi nalog",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFEFE3D1).copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Error message - prikaži ili lokalnu ili ViewModel error
                (localErrorMessage ?: errorMessage)?.let { errorMsg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF5252).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMsg,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        localErrorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    label = { Text("Ime") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, null, tint = Color(0xFFFFD700))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFFEFE3D1).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                        focusedTextColor = Color(0xFFEFE3D1),
                        unfocusedTextColor = Color(0xFFEFE3D1)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        localErrorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, null, tint = Color(0xFFFFD700))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFFEFE3D1).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                        focusedTextColor = Color(0xFFEFE3D1),
                        unfocusedTextColor = Color(0xFFEFE3D1)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        localErrorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    label = { Text("Lozinka") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFFFFD700))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                if (passwordVisible) "Sakrij" else "Prikaži",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFFEFE3D1).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                        focusedTextColor = Color(0xFFEFE3D1),
                        unfocusedTextColor = Color(0xFFEFE3D1)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        localErrorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    label = { Text("Potvrdi lozinku") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFFFFD700))
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                if (confirmPasswordVisible) "Sakrij" else "Prikaži",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFFEFE3D1).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                        focusedTextColor = Color(0xFFEFE3D1),
                        unfocusedTextColor = Color(0xFFEFE3D1)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                // Register Button
                Button(
                    onClick = {
                        when {
                            name.isBlank() -> localErrorMessage = "Unesi ime"
                            email.isBlank() -> localErrorMessage = "Unesi email"
                            password.isBlank() -> localErrorMessage = "Unesi lozinku"
                            password.length < 6 -> localErrorMessage = "Lozinka mora imati najmanje 6 znakova"
                            password != confirmPassword -> localErrorMessage = "Lozinke se ne poklapaju"
                            else -> {
                                viewModel.signUpWithEmail(email, password, name)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && 
                             password.isNotBlank() && confirmPassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF1A0B2E),
                        disabledContainerColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF1A0B2E),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Registruj se",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFEFE3D1).copy(alpha = 0.3f)
                    )
                    Text(
                        "ili",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFEFE3D1).copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFEFE3D1).copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign In Button
                OutlinedButton(
                    onClick = {
                        try {
                            val webClientId = context.resources.getString(com.gatalinka.app.R.string.default_web_client_id)
                            if (webClientId == "YOUR_WEB_CLIENT_ID_HERE" || webClientId.isEmpty()) {
                                viewModel.setError("Google Sign In će biti dostupan uskoro. Za sada koristi email/lozinku - to već radi! 👍")
                                return@OutlinedButton
                            }
                            val googleSignInClient = GoogleSignInHelper.getGoogleSignInClient(context, webClientId)
                            val signInIntent = GoogleSignInHelper.getSignInIntent(googleSignInClient)
                            googleSignInLauncher.launch(signInIntent)
                        } catch (e: Exception) {
                            viewModel.setError(ErrorMessages.getErrorMessage(e))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEFE3D1)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFD700))
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "G",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Nastavi sa Google",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        "Već imaš nalog? ",
                        color = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Prijavi se",
                        color = Color(0xFFFFD700),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}

