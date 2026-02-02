package com.example.lolbile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.credentials.GetCredentialException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.lolbile.ui.theme.LoLbileTheme
import android.graphics.Bitmap
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.emptyLongSet
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await
import kotlin.math.max
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.max
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.location.LocationServices
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.CoroutineScope
import okhttp3.MultipartBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.flow.first
import androidx.compose.ui.res.stringResource
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.concurrent.TimeUnit
import kotlin.collections.emptyList


val Context.dataStore by preferencesDataStore("settings")
val LANGUAGE_KEY = stringPreferencesKey("language")

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val savedLang = applicationContext.dataStore.data.first()[LANGUAGE_KEY]

            val langToApply = savedLang ?: "en"

            setAppLanguage(langToApply)

            setContent {
                LoLbileTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Navigation()
                    }
                }
            }
        }
    }
}

var hasSearched by mutableStateOf(false)

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

object SearchedPlayer {
    var userName by mutableStateOf<String?>(null)
    var soloQ by mutableStateOf<String?>(null)
    var flexQ by mutableStateOf<String?>(null)
    var profileIcon by mutableStateOf<String?>(null)
    var summonerLevel by mutableStateOf<Int?>(0)
    var gamesFetched by mutableStateOf<Boolean?>(false)
    var games by mutableStateOf<JSONArray?>(null)
    fun clear(){
        userName = null
        soloQ = null
        flexQ = null
        profileIcon = null
        summonerLevel = null
        games = null
        gamesFetched = false

    }
}

val SplashURLCache = InMemoryKache<Int, String>(maxSize = 100){
    strategy = KacheStrategy.LRU
}

data class CarouselItem(
    val id: Int,
    val url: String?,
    )


data class Match (
    val team1: List<JSONObject>,
    val team2: List<JSONObject>,
    val winner: Boolean,
)

data class Player (
    val nome: String,
    val puuid: String,
    val lp: Int
)

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
            text = stringResource(R.string.welcome),
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

        Text(stringResource(R.string.or), color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        ButtonUI(navController)

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") }
        ) {
            Text(
                text = stringResource(R.string.first_time),
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
        Text(stringResource(R.string.create_account), fontSize = 24.sp, fontWeight = FontWeight.Bold)

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
            Text(stringResource(R.string.already_have_account))
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
        Text(stringResource(R.string.login_google), color = Color.White)
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
        Text(stringResource(R.string.register), color = Color.White)
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
                    val profileImageUrl = jsonObj.getString("image")
                    if(profileImageUrl != null)
                    {
                        UserSession.userPhotoUrl = "http://34.120.96.92/$profileImageUrl"
                    }
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
fun SearchButton(player: String){
    val coroutineScope = rememberCoroutineScope()
    val infoList = player.split("#")
    var riotID = ""
    var tag = ""
    if(infoList.size < 2){
        riotID = infoList[0]
        tag = "EUW"
    }
    else{
        riotID = infoList[0]
        tag = infoList[1]
    }
    val onClick: () -> Unit = {

        coroutineScope.launch {
            searchPlayer(riotID, tag);
        }
    }


    Button(
        onClick = onClick,
    ) {
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.search), color = Color.White)
    }
}

suspend fun searchPlayer(riotID: String, tag: String): Boolean{
    val url = "http://10.0.2.2:8080/api/summoner/$riotID/$tag"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()
    val response = client.newCall(request).await()
    try {
        hasSearched = true
        if (response.code == 404)
        {
            Log.d("SEARCH STATUS", "Player not found")
            SearchedPlayer.clear()
            return false
        }

        Log.d("SEARCH STATUS","clearing player information")
        SearchedPlayer.clear()
        Log.d("SEARCH STATUS","The value of gamesFetched is now: ${SearchedPlayer.gamesFetched}")
        val jsonData = response.body!!.string()
        val jsonObj = JSONObject(jsonData)
        val summoner = jsonObj.getJSONObject("summoner")
        SearchedPlayer.userName = summoner.getString("nome")
        SearchedPlayer.profileIcon = summoner.getString("profile_icon_id")
        SearchedPlayer.summonerLevel = summoner.getInt("summoner_level")
        SearchedPlayer.soloQ = summoner.getString("soloq_rank")
        SearchedPlayer.flexQ = summoner.getString("flex_rank")
        SearchedPlayer.games = summoner.getJSONArray("games")
        return true

    } catch (e: Exception) {
        Log.e("SERVER_ERROR", "Server error", e)
        SearchedPlayer.clear()
        return false
    } finally {
        response.close();
    }
}

suspend fun fillGames(): List<Match> = withContext(Dispatchers.IO){
    val url = "http://10.0.2.2:8080/api/game/info"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val json = "{\"games\": ${SearchedPlayer.games.toString()}}";
    Log.d("JSON VALUE",json)
    val requestBody = json.toRequestBody(mediaType)
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()
    val response = client.newCall(request).await()
    try {
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val jsonData = response.body!!.string()
        val matches = mutableListOf<Match>()
        val jsonArr = JSONArray(jsonData)
        val arrayLength = jsonArr.length()
        for( i in 0..<arrayLength-1)
        {
            val game = jsonArr.getJSONObject(i);
            val team1 = mutableListOf<JSONObject>()
            val team2 = mutableListOf<JSONObject>()
            val team1json = game.getJSONArray("team_1")
            val team2json = game.getJSONArray("team_2")
            for(i in 0..4)
            {
                team1.add(JSONObject(team1json[i].toString()));
                team2.add(JSONObject(team2json[i].toString()));
            }
            matches.add(
                Match(
                    team1 = team1,
                    team2 = team2,
                    winner = game.getBoolean("winner")
                )
            )
        }
        SearchedPlayer.gamesFetched = true
        matches

    } catch (e: Exception) {
        Log.e("SERVER_ERROR", "Server error", e)
        SearchedPlayer.gamesFetched = false
        emptyList()
    } finally {
        response.close();
    }
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
        composable("settings") { SettingsScreen(navController) }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
            var langMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { langMenu = true }) {
                Icon(Icons.Default.Language, contentDescription = "Language")
            }
            DropdownMenu(
                expanded = langMenu,
                onDismissRequest = { langMenu = false }
            ) {

                DropdownMenuItem(
                    text = { Text("Automatic (GPS)") },
                    onClick = {
                        langMenu = false
                        detectCountry(context) { country ->
                            val lang = countryToLanguage(country)

                            setAppLanguage(lang)

                            scope.launch {
                                context.dataStore.edit { it[LANGUAGE_KEY] = lang }
                            }
                        }
                    }
                )

                DropdownMenuItem(
                    text = { Text("Italiano") },
                    onClick = {
                        scope.launch {
                            context.dataStore.edit { it[LANGUAGE_KEY] = "it" }
                        }
                        setAppLanguage("it")
                        langMenu = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("English") },
                    onClick = {
                        scope.launch {
                            context.dataStore.edit { it[LANGUAGE_KEY] = "en" }
                        }
                        setAppLanguage("en")
                        langMenu = false
                    }
                )
                /*
                DropdownMenuItem(
                    text = { Text("Français") },
                    onClick = {
                        changeLanguage("fr", context, scope)
                        langMenu = false
                    }
                )*/
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
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
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
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                menuExpanded = false
                                navController.navigate("settings")
                            }
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
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.register)) },
                            onClick = {
                                menuExpanded = false
                                navController.navigate("register")
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
            placeholder = { Text(stringResource(R.string.search_players)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    SearchButton(searchText)
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    val tabs = listOf("Dashboard", stringResource(R.string.leaderboard), "Champion rotations")
    val isFound = SearchedPlayer.userName != null
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
                0 -> Dashboard(isFound, hasSearched)
                1 -> Leaderboard()
                2 -> ChampionRotations()
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {

    val context = LocalContext.current
    var username by remember { mutableStateOf(UserSession.userName ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var showPickerDialog by remember { mutableStateOf(false) }
    val cameraTempUri = remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = cameraTempUri.value
        }
    }

    Scaffold(

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showPickerDialog) {
                AlertDialog(
                    onDismissRequest = { showPickerDialog = false },
                    title = { Text("Change profile photo") },
                    text = { Text("Choose image source") },
                    confirmButton = {
                        TextButton(onClick = {
                            showPickerDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) { Text("Gallery") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showPickerDialog = false
                            val uri = createImageUri(context)
                            cameraTempUri.value = uri
                            cameraLauncher.launch(uri)
                        }) { Text("Camera") }
                    }
                )
            }
            Box {
                val avatarModel = imageUri ?: UserSession.userPhotoUrl
                if (avatarModel != null) {
                    AsyncImage(
                        model = avatarModel,
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(140.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showPickerDialog = true }
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White)
                }
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    uploading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val newUrl = uploadProfileData(username, imageUri, context)
                        withContext(Dispatchers.Main) {
                            UserSession.userName = username
                            newUrl?.let { UserSession.userPhotoUrl = it }
                            uploading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uploading) CircularProgressIndicator(color = Color.White)
                else Text(stringResource(R.string.save_changes))
            }
        }
    }
}

fun createImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

fun uploadProfileData(
    username: String,
    imageUri: Uri?,
    context: Context
): String? {
    val client = OkHttpClient()
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("username", username)
    imageUri?.let {
        val stream = context.contentResolver.openInputStream(it)!!
        val bytes = stream.readBytes()
        builder.addFormDataPart(
            "avatar",
            "avatar.jpg",
            bytes.toRequestBody("image/jpeg".toMediaType())
        )
    }
    val request = Request.Builder()
        .url("http://10.0.2.2:8080/api/account/image")
        .addHeader("Authorization", "Bearer ${UserSession.appAuthToken}")
        .post(builder.build())
        .build()

    val response = client.newCall(request).execute()

    if (!response.isSuccessful) return null

    val json = JSONObject(response.body!!.string())
    return json.getString("avatarUrl")
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
fun Dashboard(isFound: Boolean, hasSearched: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if(hasSearched) {
            if (isFound) {
                var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
                var loading by remember { mutableStateOf(true) }

                suspend fun loadData() {
                    matches = fillGames()
                }

                LaunchedEffect(SearchedPlayer.gamesFetched)
                {
                    loadData()
                    loading = false
                }

                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {

                    val hexagon = remember {
                        RoundedPolygon(
                            6,
                            rounding = CornerRounding(0.2f)
                        )
                    }

                    val clip = remember(hexagon) {
                        RoundedPolygonShape(polygon = hexagon)
                    }

                    Column() {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column() {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(top = 6.dp, start = 6.dp),
                                ) {
                                    AsyncImage(
                                        model = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/${SearchedPlayer.profileIcon}.jpg",
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                this.shadowElevation = 6.dp.toPx()
                                                this.shape = clip
                                                this.clip = true
                                                this.ambientShadowColor = Color.Black
                                                this.spotShadowColor = Color.Black
                                            }
                                            .size(200.dp)
                                    )
                                    Text(
                                        text = SearchedPlayer.summonerLevel.toString(),
                                        modifier = Modifier.padding(top = 180.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 30.sp,

                                        )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 5.dp),

                                ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                ) {
                                    Text(

                                        text = SearchedPlayer.userName.toString(),
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 20.sp,
                                    )
                                }
                                Row()
                                {
                                    Column() {
                                        Text(
                                            text = "Solo Q Rank:"
                                        )
                                        Text(
                                            text = SearchedPlayer.soloQ.toString(),
                                        )
                                    }
                                }
                                Row()
                                {
                                    Column() {
                                        Text(
                                            text = "Flex Q Rank:"
                                        )
                                        Text(
                                            text = SearchedPlayer.flexQ.toString(),
                                        )
                                    }
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth())
                        {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(matches) { match ->
                                    var color = Color(255, 128, 128)
                                    val playerName = SearchedPlayer.userName
                                    val teamWon = match.winner
                                    if (teamWon) {
                                        val team2 = match.team2
                                        for (i in 0..4) {
                                            val player = team2[i]
                                            if (player.getString("playerId")
                                                    .lowercase() == playerName.toString()
                                            ) {
                                                color = Color(119, 145, 236)
                                                break
                                            }
                                        }
                                    } else {
                                        val team1 = match.team1
                                        for (i in 0..4) {
                                            val player = team1[i]
                                            if (player.getString("playerId")
                                                    .lowercase() == playerName.toString()
                                            ) {
                                                color = Color(119, 145, 236)
                                                break
                                            }
                                        }
                                    }

                                    MatchCard(match, color)
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
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
                        text = stringResource(R.string.not_found),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        lineHeight = 24.sp
                    )
                }
            }
        }
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
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
                    text = stringResource(R.string.search_player),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Leaderboard() {
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    suspend fun loadData() {
        players = fetchLeaderboard()
    }

    LaunchedEffect(Unit) {
        loadData()
        loading = false
    }

    val refreshScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshScope.launch {
                refreshing = true
                loadData()
                refreshing = false
            }
        }
    )

    Box(
        Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(players) { index, player ->
                    PlayerCard(player, index + 1)
                }
            }
        }

        PullRefreshIndicator(
            refreshing,
            pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

suspend fun fetchLeaderboard(): List<Player> = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/summoner/leaderboard")
            .get()
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) return@withContext emptyList()

        val jsonString = response.body!!.string()
        val rootObj = JSONObject(jsonString)
        val playersArray = rootObj.getJSONArray("players")

        val players = mutableListOf<Player>()

        for (i in 0 until playersArray.length()) {
            val obj = playersArray.getJSONObject(i)
            players.add(
                Player(
                    nome = obj.getString("nome"),
                    puuid = obj.getString("puuid"),
                    lp = obj.getInt("lp")
                )
            )
        }

        players

    } catch (e: Exception) {
        Log.e("API_ERROR", "Leaderboard error", e)
        emptyList()
    }
}

@Composable
fun PlayerCard(player: Player, position: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "#$position",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.width(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(player.nome, fontWeight = FontWeight.Bold)
            }

            Text(
                "${player.lp} LP",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MatchCard(match: Match, color: Color){
    Card(
        colors = CardDefaults.cardColors(color),
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        elevation = CardDefaults.cardElevation(),

    ) {
        Column() {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Blue Side Team"
                    )
                }
                Column(modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Red Side Team"
                    )
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth()
            )
            {
                Column(modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(start = 5.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                        )
                    {

                        Text(
                            text = "${match.team1[0].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team1[1].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team1[2].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team1[3].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team1[4].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                }
                VerticalDivider(
                    modifier = Modifier.padding(bottom = 3.dp),
                    color = mixColors(color, 0.6f),
                    thickness = 3.dp
                )
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp)
                    ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team2[0].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team2[1].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team2[2].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team2[3].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${match.team2[4].getString("playerId")}",
                            fontSize = 12.sp
                        )
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionRotations() {

    var items by remember { mutableStateOf<List<CarouselItem>>(emptyList())}
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        items = getChampsCache()
        loading = false
    }
    if (loading)
    {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    else {
        HorizontalCenteredHeroCarousel(
            state = rememberCarouselState() { items.count() },
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            itemSpacing = 10.dp
        ) { i ->
            val item = items[i]
            AsyncImage(
                model = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/${item.url}",
                contentDescription = "Champion Icon",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

suspend fun getFreeChampionIds(): List<Int>{
    val url = "http://10.0.2.2:8080/api/champion/free-rotation"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()
    val response = client.newCall(request).await()
    try {
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val jsonString = response.body!!.string()
        val rootObj = JSONObject(jsonString)
        val championList = rootObj.getJSONArray("champion_array")
        val size = championList.length()
        val ids = mutableListOf<Int>()
        for (i in 0 until size)
        {
            ids.add(championList[i].toString().toInt())
        }
        return ids
    } catch (e: Exception) {
        Log.e("SERVER_ERROR", "Server error", e)
        return emptyList()
    } finally {
        response.close();
    }
}

fun RoundedPolygon.getBounds() = calculateBounds().let { Rect(it[0], it[1], it[2], it[3]) }
class RoundedPolygonShape(
    private val polygon: RoundedPolygon,
    private var matrix: Matrix = Matrix()
) : Shape {
    private var path = Path()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        path.rewind()
        path = polygon.toPath().asComposePath()
        matrix.reset()
        val bounds = polygon.getBounds()
        val maxDimension = max(bounds.width, bounds.height)
        matrix.scale(size.width / maxDimension, size.height / maxDimension)
        matrix.translate(-bounds.left, -bounds.top)

        path.transform(matrix)
        return Outline.Generic(path)
    }
}

fun mixColors(col1: Color, factor: Float): Color {

    val alpha = col1.alpha
    val r = col1.red * factor
    val g = col1.green * factor
    val b = col1.blue * factor


    return Color(r, g, b,alpha)
}
@SuppressLint("MissingPermission")
fun detectCountry(
    context: Context,
    onResult: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val countryCode = addresses?.firstOrNull()?.countryCode ?: "UK"
            onResult(countryCode)
        } else {
            onResult("UK")
        }
    }
}

fun countryToLanguage(country: String): String {
    return when (country) {
        "IT" -> "it"
        "FR" -> "fr"
        "DE" -> "de"
        "ES" -> "es"
        else -> "en"
    }
}

fun setAppLanguage(language: String) {
    val locales = LocaleListCompat.forLanguageTags(language)
    AppCompatDelegate.setApplicationLocales(locales)
}

suspend fun getChampionLoadScreenPath(championId: Int): String {
    val url = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champions/$championId.json"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()
    val response = client.newCall(request).await()
    try {
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        val jsonData = response.body!!.string()
        val jsonObj = JSONObject(jsonData)
        val skinsArray = jsonObj.getJSONArray("skins")
        val baseSkin = skinsArray.getJSONObject(0)
        val fullPath = baseSkin.getString("loadScreenPath")
        val sliced = fullPath.substring(21).lowercase()
        SplashURLCache.put(championId,sliced)
        return sliced
    } catch (e: Exception) {
        Log.e("SERVER_ERROR", "Server error", e)
    } finally {
        response.close();
    }
    return ""
}

suspend fun getChampsCache(): List<CarouselItem>
{
    val ids = getFreeChampionIds()

    val itemList = mutableListOf<CarouselItem>()

    val kacheKeys = SplashURLCache.getKeys()
    if(kacheKeys.isEmpty()) {
        ids.forEach {
            SplashURLCache.put(it, getChampionLoadScreenPath(it))
        }
    }
    SplashURLCache.getKeys().forEach {
        itemList.add(CarouselItem(it, SplashURLCache.get(it)))
    }
    return itemList
}