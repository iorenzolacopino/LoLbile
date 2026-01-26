package com.example.lolbile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.lolbile.ui.theme.LoLbileTheme
import android.content.ContentValues.TAG
import android.content.Context
import android.credentials.GetCredentialException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoLbileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    Navigation()
                }
            }
        }
    }
}

object UserSession {
    var userName by mutableStateOf<String?>(null)
    var userPhotoUrl by mutableStateOf<String?>(null)
    var appAuthToken by mutableStateOf<String?>(null)
    fun clear() {
        userName = null
        userPhotoUrl = null
        appAuthToken = null
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun LoginScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to LoLbile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            value = email,
            onValueChange = { newText ->
                email = newText
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("or", color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        ButtonUI(navController)

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") }
        ) {
            Text(
                text = "If it's your first time here, register",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline
                )
            )
        }

    }
}

@Composable
fun RegisterScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Create account", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                // backend
                // registerUser(email, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }
    }
}

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun signIn(request: GetCredentialRequest, context: Context): Boolean {
    val credentialManager = CredentialManager.create(context)
    // val failureMessage = "Sign in failed!"
    // var e: Exception? = null
    //using delay() here helps prevent NoCredentialException when the BottomSheet Flow is triggered
    //on the initial running of our app
    delay(2000)
    /*
    try {
        // The getCredential is called to request a credential from Credential Manager.
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )
        Log.i(TAG, result.toString())

        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Sign in Successful!")

    } catch (e: GetCredentialException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Failure getting credentials", e)

    } catch (e: GoogleIdTokenParsingException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Issue with parsing received GoogleIdToken", e)

    } catch (e: NoCredentialException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": No credentials found", e)
        return e

    } catch (e: GetCredentialCustomException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Issue with custom credential request", e)

    } catch (e: GetCredentialCancellationException) {
        Toast.makeText(context, ": Sign-in cancelled", Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Sign-in was cancelled", e)
    }
    return e
    */
    return try {
        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            UserSession.userName = googleIdTokenCredential.displayName
            UserSession.userPhotoUrl = googleIdTokenCredential.profilePictureUri?.toString()
            val idToken = googleIdTokenCredential.idToken
            val backendToken = sendGoogleTokenToBackend(idToken)
            if (backendToken != null) {
                UserSession.appAuthToken = backendToken
                Log.d("LOGIN_DEBUG", "Backend token saved")
                true
            } else {
                Log.e("LOGIN_DEBUG", "Backend auth failed")
                false
            }
        } else {
            Log.e("LOGIN_DEBUG", "Credential type not supported: ${credential.type}")
            false
        }
    } catch (e: GetCredentialException) {
        Log.e("LOGIN_DEBUG", "Credential Manager Error: ${e.message}")
        false
    } catch (e: Exception) {
        Log.e("LOGIN_DEBUG", "Generic error", e)
        false
    }
}

suspend fun sendGoogleTokenToBackend(idToken: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()

            val json = JSONObject()
            json.put("idToken", idToken)

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://YOUR_BACKEND/auth/google")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val responseBody = JSONObject(response.body!!.string())
                responseBody.getString("token")   // JWT del backend
            }

        } catch (e: Exception) {
            Log.e("BACKEND_AUTH", "Error", e)
            null
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ButtonUI(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId = "887270428298-g7ook4kj6hb33egp9s7te9lau92kppdv.apps.googleusercontent.com"

    val onClick: () -> Unit = {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            val success = signIn(request, context)
            if (success) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        testHTTP()
    }
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.width(8.dp))
        Text("Login with Google", color = Color.White)
    }
}

fun verifyToken() {
    val client = OkHttpClient()
    val JSON = "application/json; charset=utf-8".toMediaType()
    @Throws(IOException::class)
    fun post(url: String, json: String): String? {
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body?.string()
        }
    }
}
/*
private fun handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
    try {
        GoogleSignInAccount account = completedTask.getResult (ApiException.class);
        String idToken = account.getIdToken();
        verifyToken()
    } catch (ApiException e) {
        Log.w(TAG, "handleSignInResult:error", e);
    }
}

fun registerUser()

fun findUser()
 */

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        restoreGoogleSession(context)
    }
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Leaderboard (top 200)", "Matches", "Champion rotations")
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val account = GoogleSignIn.getLastSignedInAccount(context)
    val userName = UserSession.userName
    val userPhoto = UserSession.userPhotoUrl
    val isLogged = userName != null
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("LoLbile")
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            if (userPhoto != null) {
                                AsyncImage(
                                    model = userPhoto,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Avatar"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {

                            if (isLogged) {
                                DropdownMenuItem(
                                    text = { Text(userName!!, fontWeight = FontWeight.Bold) },
                                    onClick = {},
                                    enabled = false
                                )
                                Divider()

                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { menuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        UserSession.clear()
                                        menuExpanded = false
                                    }
                                )

                            } else {
                                DropdownMenuItem(
                                    text = { Text("Login") },
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate("login")
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> Dashboard()
                1 -> Leaderboard()
                2 -> Matches()
                3 -> ChampionRotations()
            }
        }
    }
}

fun restoreGoogleSession(context: Context) {
    val account = GoogleSignIn.getLastSignedInAccount(context)
    account?.let {
        UserSession.userName = it.displayName
        UserSession.userPhotoUrl = it.photoUrl?.toString()
    }
}

// funzione di testing
fun testHTTP() {
    val client = OkHttpClient()
    val url = "https://reqres.in/api/users?page=2"
    val request = Request.Builder().url(url).build()
    Log.d("CHIAMA_FUNZIONE", "chiamando la funzione")
    client.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }
        override fun onResponse(call: Call, response: Response) {
            Log.d("VEDIAMO", response.code.toString())
            if (response.isSuccessful) {
                Log.d("RESPONSE_BODY", response.body!!.toString())
            }
            Log.d("SUCCESSO", "response completed")
        }
    })
}


@Composable
fun Dashboard() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Dashboard", fontSize = 22.sp)
    }
}

@Composable
fun Leaderboard() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Leaderboard", fontSize = 22.sp)
    }
}

@Composable
fun Matches() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Matches", fontSize = 22.sp)
    }
}

@Composable
fun ChampionRotations(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Champion rotations", fontSize = 22.sp)
    }
}
