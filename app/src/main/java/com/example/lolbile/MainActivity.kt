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
import android.content.Context
import android.credentials.GetCredentialException
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import java.security.SecureRandom
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
import okhttp3.internal.userAgent
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

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

        LoginButton(email, password, navController)

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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create account", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(24.dp))

        RegisterButton(username, email, password, navController)

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun signIn(request: GetCredentialRequest, context: Context){
    val credentialManager = CredentialManager.create(context)
    delay(2000)
    try {
        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            UserSession.userName = googleIdTokenCredential.displayName
            UserSession.userPhotoUrl = googleIdTokenCredential.profilePictureUri?.toString()
            val idToken = googleIdTokenCredential.idToken
            loginWithToken(idToken)
        } else {
            Log.e("LOGIN_DEBUG", "Credential type not supported: ${credential.type}")
        }
    } catch (e: GetCredentialException) {
        Log.e("LOGIN_DEBUG", "Credential Manager Error: ${e.message}")
    } catch (e: Exception) {
        Log.e("LOGIN_DEBUG", "Generic error", e)
    }
}

// BUTTONS DEFINITIONS
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ButtonUI(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId = "887270428298-g7ook4kj6hb33egp9s7te9lau92kppdv.apps.googleusercontent.com"

    val onClick: () -> Unit = {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientId)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        coroutineScope.launch {
            signIn(request, context)
            navController.navigate("home") {
                popUpTo("login") {
                    inclusive = true
                }
            }
        }
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

suspend fun loginWithToken(googleToken: String){
    val json = "{\"token\":\"${googleToken}\"}"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toRequestBody(mediaType)
    val url = "http://10.0.2.2:8080/api/login"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()
    val response = client.newCall(request).await()
    try {
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val jsonData = response.body!!.string()
        val jsonObj = JSONObject(jsonData)
        val message = jsonObj.getString("message")
        when (message) {
            "login_ok" -> {
                // tutto ok (jwt token)
                UserSession.appAuthToken = jsonObj.getString("token")
                Log.i("TOKEN", "This is the token: ${UserSession.appAuthToken}")
            }
        }
    } catch (e: Exception) {
        Log.e("SERVER_ERROR", "Server error", e)
    } finally {
        response.close();
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RegisterButton(username: String, email: String, password: String, navController: NavController){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val username = username;
    val email = email;
    val password = password

    val onClick: () -> Unit = {

        coroutineScope.launch {
            var success = registerUser(username,email,password)
            if(success)
            {
                success = loginPass(email,password);
                if(success)
                {
                    navController.navigate("home") {
                        popUpTo("login") {
                            inclusive = true
                }
            }

                }
            }
        }
    }


    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.width(8.dp))
        Text("Register", color = Color.White)
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun LoginButton(email: String, password: String, navController: NavController){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val email = email;
    val password = password

    val onClick: () -> Unit = {

        coroutineScope.launch {
            val success = loginPass(email,password);
            if(success) {
                navController.navigate("home") {
                    popUpTo("login") {
                        inclusive = true
                    }
                }

            }
        }
    }


    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.width(8.dp))
        Text("Login", color = Color.White)
    }
}

suspend fun registerUser(username: String, email: String, password: String): Boolean{
    val json = "{\"email\":\"${email}\",\"username\":\"${username}\",\"password\":\"${password}\"}"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toRequestBody(mediaType)
    var success = false;
    val url = "http://10.0.2.2:8080/api/register"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()
    val response = client.newCall(request).await()
    try {
        if (!response.isSuccessful){
            if(response.code == 400)
            {
                val jsonData = response.body!!.string()
                val jsonObj = JSONObject(jsonData)
                val message = jsonObj.getString("message")
                when(message){
                    "All fields are required" -> {
                        Log.e("SERVER_ERROR", message)
                    }
                    "User already exists" ->
                    {
                        Log.e("SERVER_ERROR", message)
                    }
                }
            }
            else
            {
                throw IOException("Unexpected code $response")
            }
        }
        else {
            val jsonData = response.body!!.string()
            val jsonObj = JSONObject(jsonData)
            val message = jsonObj.getString("message")
            when (message) {
                "Successfuly created account" -> {
                    // tutto ok (jwt token)
                    success = true;
                }
            }
        }

    } catch (e: Exception) {
        Log.e("SERVER_ERROR", "Server error", e)
    }
    finally {
        response.close();
    }
    return success;
}

suspend fun loginPass(email: String,password: String): Boolean{
    val json = "{\"email\":\"${email}\",\"password\":\"${password}\"}"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toRequestBody(mediaType)
    var success = false;
    val url = "http://10.0.2.2:8080/api/login"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()
    val response = client.newCall(request).await()
    try {
        if (!response.isSuccessful){
            if(response.code == 400)
            {
                val jsonData = response.body!!.string()
                val jsonObj = JSONObject(jsonData)
                val message = jsonObj.getString("message")
                if(message == "Invalid Credentials")
                {
                    Log.e("SERVER_ERROR", message)
                }
            }
            else
            {
                throw IOException("Unexpected code $response")
            }
        }
        else{
            val jsonData = response.body!!.string()
            val jsonObj = JSONObject(jsonData)
            val message = jsonObj.getString("message")
            when (message) {
                "login_ok" -> {
                    // tutto ok (jwt token)
                    UserSession.userName = jsonObj.getString("username")
                    UserSession.appAuthToken = jsonObj.getString("token")
                    Log.i("TOKEN", "This is the token: ${UserSession.appAuthToken}")
                    success = true;
                }
            }
        }
    } catch (e: Exception) {
        Log.e("SERVER_ERROR", "Server error", e)
    }
    finally {
        response.close();
    }
    return success;
}

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
        composable(
            route = "player/{playerName}",
            arguments = listOf(navArgument("playerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            PlayerScreen(navController, playerName)
        }
    }
}

@Composable
fun TopLayout(
    navController: NavController,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val userName = UserSession.userName
    val userPhoto = UserSession.userPhotoUrl
    val isLogged = userName != null
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isHome = currentRoute == "home"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (!isHome) navController.navigate("home")
                },
                enabled = !isHome
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (isHome)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("LoLbile", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    if (userPhoto != null) {
                        AsyncImage(
                            model = userPhoto,
                            contentDescription = "Profile",
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (isLogged) {
                        DropdownMenuItem(
                            text = { Text(userName, fontWeight = FontWeight.Bold) },
                            onClick = {},
                            enabled = false
                        )
                        HorizontalDivider()
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

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { Text("Search players...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    Button(
                        onClick = { onSearch(searchText) },
                        modifier = Modifier.padding(end = 8.dp).height(36.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) { Text("Go") }
                }
            },
            keyboardActions = KeyboardActions(
                onSearch = {
                    val encoded = Uri.encode(searchText.trim())
                    navController.navigate("player/$encoded")
                }
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    val tabs = listOf("Dashboard", "Leaderboard (top 20)", "Matches", "Champion rotations")
    val isLogged = UserSession.userName != null
    Scaffold(
        topBar = {
            TopLayout(
                navController = navController,
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onSearch = {
                    val encoded = Uri.encode(it.trim())
                    navController.navigate("player/$encoded")
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
                0 -> Dashboard(isLogged)
                1 -> Leaderboard()
                2 -> Matches(isLogged)
                3 -> ChampionRotations(isLogged)
            }
        }
    }
}

@Composable
fun PlayerScreen(navController: NavController, playerName: String) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    val tabs = listOf("Dashboard", "Leaderboard (top 20)", "Matches", "Champion rotations")
    Scaffold(
        topBar = {
            TopLayout(
                navController = navController,
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onSearch = {
                    val encoded = Uri.encode(it.trim())
                    navController.navigate("player/$encoded")
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$playerName — ${tabs[selectedTab]}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isHome = currentRoute == "home"
    TopAppBar(
        title = {
            Text(
                "LoLbile",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (!isHome) {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                },
                enabled = !isHome
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (isHome)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}


fun restoreGoogleSession(context: Context) {
    val account = GoogleSignIn.getLastSignedInAccount(context)
    account?.let {
        UserSession.userName = it.displayName
        UserSession.userPhotoUrl = it.photoUrl?.toString()
    }
}

@Composable
fun Dashboard(isLogged: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLogged) {
            Text("Dashboard", fontSize = 22.sp)
        }
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "As an unauthenticated user you can only search players.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )
            }
        }
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
fun Matches(isLogged: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLogged) {
            Text("Matches", fontSize = 22.sp)
        }
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "As an unauthenticated user you can only search players.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun ChampionRotations(isLogged: Boolean){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLogged) {
            Text("Champion rotations", fontSize = 22.sp)
        }
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "As an unauthenticated user you can only search players.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
