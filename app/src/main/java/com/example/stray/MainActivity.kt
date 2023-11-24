package com.example.stray

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stray.database.FirebaseAuthenticationManager
import com.example.stray.database.FirebaseDataManager
import com.example.stray.screen.CommunityHome
import com.example.stray.screen.CreatePostScreen
import com.example.stray.screen.EditPostScreen
import com.example.stray.screen.Login
import com.example.stray.screen.PostDetail
import com.example.stray.screen.SuccessLogin
import com.example.stray.screen.UserInfo
import com.example.stray.ui.theme.StrayTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


//깃발
class MainActivity : ComponentActivity() {
    val gson = GsonBuilder().setLenient().create()
    // Retrofit 인스턴스 생성
    val retrofit = Retrofit.Builder()
        .baseUrl("your_api_url")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(OkHttpClient())
        .build()

    // Retrofit 인터페이스 사용
    val apiService = retrofit.create(ApiService::class.java)
    // 파이어베이스 로그인
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseDataManager: FirebaseDataManager // Add this line

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        firebaseDataManager = FirebaseDataManager()
        setContent {
            StrayTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val requestPermissionLauncher =
                        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                            if (isGranted) {
                                // 권한이 허용되었을 때 수행할 작업을 여기에 추가합니다.
                                // 예를 들어 위치 정보를 가져오는 코드를 실행할 수 있습니다.
                            } else {
                                // 권한이 거부되었을 때 사용자에게 메시지를 표시하거나 다른 조치를 취할 수 있습니다.
                            }
                        }
                    val db = Firebase.firestore
                    val context = LocalContext.current
                    var loginCheck by remember {
                        mutableStateOf(false)
                    }
                    var homeSelected by remember {
                        mutableStateOf(BarState.HOME)
                    }
                    val navController = rememberNavController()

                    val user: FirebaseUser? = mAuth.currentUser
                    val startDestination = remember {
                        if (user == null) {
                            loginCheck = false
                            Screen.Login.route
                        } else {
                            loginCheck = true
                            Screen.SuccessLogin.route
                        }
                    }
                    val signInIntent = googleSignInClient.signInIntent

                    val launcher =
                        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                            val data = result.data
                            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                            val exception = task.exception
                            if (task.isSuccessful) {
                                try {
                                    val account = task.getResult(ApiException::class.java)!!
                                    firebaseAuthWithGoogle(
                                        account.idToken!!,
                                        mAuth,
                                        navController = navController
                                    )
                                    loginCheck = true
                                } catch (e: Exception) {
                                    Log.d("SignIn", "로그인 실패")
                                }
                            } else {
                                Log.d("SignIn", exception.toString())
                            }
                        }
                    var maxNumber by remember { mutableStateOf(0) }
                    var resetMaxNum by remember {
                        mutableStateOf(true)
                    }
                    LaunchedEffect(resetMaxNum){
                        val querySnapshot = db.collection("멍냥이").get().await()
                        for(document in querySnapshot.documents){
                            val number = document.id.toString().last().digitToIntOrNull()
                            if (number != null && number > maxNumber) {
                                maxNumber = number
                            }
                        }
                        resetMaxNum = false
                    }
                    Box(modifier = Modifier.fillMaxSize()){
                        NavHost(navController = navController, startDestination = startDestination,
                            modifier = Modifier.fillMaxSize()) {
                            composable(Screen.Login.route) {
                                Login{
                                    launcher.launch(signInIntent)
                                }
                            }
                            composable(Screen.SuccessLogin.route) {
                                SuccessLogin(
                                    navController = navController,
                                    dot = painterResource(id = R.drawable.dot),
                                    db = db,
                                    temp = painterResource(id = R.drawable.malti),
                                    context = context,
                                    maxNum = maxNumber
                                    )
                            }
                            composable(Screen.CommunityHome.route) { CommunityHome(navController) }
                            composable(route = "${Screen.PostDetail.route}/{postId}") { backStackEntry ->
                                val postId = backStackEntry.arguments?.getString("postId")
                                if (::firebaseDataManager.isInitialized) { // Check if initialized
                                    PostDetail(navController, postId ?: "", firebaseDataManager)
                                } else {
                                    // Handle the case where firebaseDataManager is not initialized yet
                                    Log.e("MainActivity", "com.example.getiproject.database.FirebaseDataManager is not initialized")
                                }
                            }
                            composable(Screen.CreatePostScreen.route) { CreatePostScreen(navController) }
                            composable(Screen.EditPostScreen.route + "/{postId}") { backStackEntry ->
                                val postId = backStackEntry.arguments?.getString("postId")
                                val firebaseDataManager =
                                    FirebaseDataManager() // 또는 사용자 정의된 로직으로 FirebaseDataManager를 초기화

                                if (postId != null) {
                                    EditPostScreen(navController, postId, firebaseDataManager)
                                } else {
                                    Log.e("MainActivity", "postId is null")
                                }
                            }
                            composable(Screen.UserInfo.route) { UserInfo(navController
                                , onSignOutClicked = {
                                signOut(navController)
                                loginCheck = false
                            })}
                            composable("map"){
                                MapScreen(context = context, db = db)
                            }
                            composable("camera"){
                                CameraScreen(
                                    context=context, gson = gson, apiService = apiService, requestPermissionLauncher = requestPermissionLauncher, db = db
                                )
                            }
                        }
                        if(loginCheck) {
                            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                                Divider(thickness = 1.dp, color = Color.Black.copy(0.1f))
                                //하단 바 명령어////////////////////////
                                NavigationBar(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    containerColor = Color.White,
                                ) {
                                    NavigationBarItem(selected = false,
                                        onClick = {
                                            homeSelected = BarState.HOME
                                            navController.navigate(Screen.SuccessLogin.route){
                                                popUpTo(Screen.SuccessLogin.route) {
                                                    inclusive = true
                                                }
                                            }
                                                  },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Filled.Home,
                                                contentDescription = null,
                                                tint = if (homeSelected == BarState.HOME) Color(0xff2196f3) else LocalContentColor.current
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "홈",
                                                color = if (homeSelected == BarState.HOME) Color(0xff2196f3) else LocalContentColor.current
                                            )
                                        }, modifier = Modifier.align(Alignment.CenterVertically))
                                    NavigationBarItem(selected = false,
                                        onClick = {
                                            homeSelected = BarState.CAMERA
                                            navController.navigate("camera"){
                                                popUpTo("camera") {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.camera),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (homeSelected == BarState.CAMERA) Color(0xff2196f3) else LocalContentColor.current
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "등록",
                                                color = if (homeSelected == BarState.CAMERA) Color(0xff2196f3) else LocalContentColor.current
                                            )
                                        }, modifier = Modifier.align(Alignment.CenterVertically))
                                    NavigationBarItem(selected = false,
                                        onClick = {
                                            homeSelected = BarState.MAP
                                            navController.navigate("map"){
                                                popUpTo("map") {
                                                    inclusive = true
                                                }
                                            }
                                                  },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Filled.Place,
                                                contentDescription = null,
                                                tint = if (homeSelected == BarState.MAP) Color(0xff2196f3) else LocalContentColor.current
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "지도",
                                                color = if (homeSelected == BarState.MAP) Color(0xff2196f3) else LocalContentColor.current
                                            )
                                        }, modifier = Modifier.align(Alignment.CenterVertically))
                                    NavigationBarItem(selected = false,
                                        onClick = {
                                            homeSelected = BarState.COMMU
                                            navController.navigate(Screen.CommunityHome.route){
                                                popUpTo(Screen.CommunityHome.route) {
                                                    inclusive = true
                                                }
                                            }
                                                  },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Filled.Create,
                                                contentDescription = null,
                                                tint = if (homeSelected == BarState.COMMU) Color(0xff2196f3) else LocalContentColor.current
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "게시판",
                                                color = if (homeSelected == BarState.COMMU) Color(
                                                    0xff2196f3
                                                ) else LocalContentColor.current
                                            )
                                        }, modifier = Modifier.align(Alignment.CenterVertically))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun firebaseAuthWithGoogle(
        idToken: String,
        mAuth: FirebaseAuth,
        navController: NavController
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // SignIn Successful
                    val currentUser = mAuth.currentUser
                    currentUser?.let {
                        // Check if the user document already exists in Firestore
                        val db = Firebase.firestore
                        val uid = it.uid
                        val userRef = db.collection("users").document(uid)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // User document already exists, no need to save again
                                    Log.d("Firestore", "User document already exists")
                                } else {
                                    // User document doesn't exist, save the data
                                    val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
                                    val profileImageUri = googleSignInAccount?.photoUrl

                                    val user = hashMapOf(
                                        "email" to it.email,
                                        "displayName" to it.displayName,
                                        "photoUrl" to profileImageUri.toString(), // Add the profile image URL
                                        // Add other fields as needed
                                    )

                                    userRef.set(user)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "User document successfully written!")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "Error writing document", e)
                                        }
                                }
                                navController.popBackStack()
                                navController.navigate(Screen.SuccessLogin.route)
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error checking document existence", e)
                            }
                    }
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut(navController: NavController) {
        val db = Firebase.firestore
        val authManager = FirebaseAuthenticationManager()
        val currentUser = authManager.getCurrentUser()
        val uid = currentUser?.uid
        val docRef = uid?.let { db.collection("users").document(it) }

        // get the google account
        val googleSignInClient: GoogleSignInClient

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign Out of all accounts
        mAuth.signOut()
        googleSignInClient.signOut().addOnSuccessListener {
            navController.navigate(Screen.Login.route)
        }.addOnFailureListener {
            Toast.makeText(this, "로그아웃 실패", Toast.LENGTH_SHORT).show()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CameraScreen(context: Context, gson: Gson, apiService: ApiService, requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>, db: FirebaseFirestore){
    var currentDate by remember {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
        mutableStateOf(currentDateTime.format(formatter))
    }
    var uploadUri: Uri? by remember {
        mutableStateOf(null)
    }
    var maxNumber by remember { mutableStateOf(0) }
    var resetMaxNum by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(resetMaxNum){
        val querySnapshot = db.collection("멍냥이").get().await()
        for(document in querySnapshot.documents){
            val number = document.id.split('물')[1].toIntOrNull()
            if (number != null && number > maxNumber) {
                maxNumber = number
            }
        }
        resetMaxNum = false
    }
    //var url: URL? by remember{ mutableStateOf(null) }
    var scheduleFireStoreMap: HashMap<String, Any> by remember {
        mutableStateOf(hashMapOf("종" to ""))
    }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var resultText by remember { mutableStateOf("") }
    var imgJson by remember {
        mutableStateOf("")
    }
    var selectUri by remember { // 갤러리 이미지 uri 객체
        mutableStateOf<Uri?>(null)
    }
    var takenPhoto by remember { // 기본 사진 앱 비트맵 객체
        mutableStateOf<Bitmap?>(null)
    }
    val launcher = // 갤러리 이미지 런쳐
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                selectUri = uri
                takenPhoto = null
                uploadUri = uri
            }
        )
    val cameraLauncher = // 카메라 이미지 런쳐
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview(),
            onResult = { photo ->
                takenPhoto = photo
                selectUri = null
                if(photo != null){
                    uploadUri = bitmapToUri(context = context, bitmap = photo)
                }
            })
    val bitmap: Bitmap? = selectUri?.let { uriToBitmap(it, context) } ?: takenPhoto
    val resources = context.resources
    val defaultImageBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.noimage).asImageBitmap()
    var cameraOrGall by remember {
        mutableStateOf(false)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp),
    ) {
        Image(
            bitmap = bitmap?.asImageBitmap() ?: defaultImageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(20))
                .clickable { cameraOrGall = !cameraOrGall }
                .border(3.dp, if(bitmap!=null) Color.Black else Color.Transparent, RoundedCornerShape(20))
            ,
        )
        Spacer(modifier = Modifier.size(0.dp,40.dp))
        if (bitmap != null){
            Button(onClick = {
                imgJson = encodeBitmapToBase64(bitmap)
                try {
                    // API에 전송할 데이터 생성
                    val imageRequest = ImageRequest(imgJson)
                    val imageRequestJson = gson.toJson(imageRequest)
                    // Retrofit을 사용하여 API에 POST 요청 (비동기적으로 처리)
                    apiService.postImage(imageRequestJson).enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                val storageRef = FirebaseStorage.getInstance().reference.child("images/유기동물${maxNumber+1}")
                                val result = response.body()
                                resultText = "$result 로 추정됩니다.\n업로드 완료!"
                                if(result != null && '/' in result){
                                    val imgValue = result.split("/")
                                    scheduleFireStoreMap = hashMapOf("종" to imgValue[0])
                                    scheduleFireStoreMap.put("분류", imgValue[1])
                                    scheduleFireStoreMap.put("발견시간", currentDate)
                                    currentLocation = getCurrentLocation(context, requestPermissionLauncher)
                                    currentLocation?.let{
                                        scheduleFireStoreMap.put("위도", it.latitude)
                                        scheduleFireStoreMap.put("경도", it.longitude)
                                    }
                                    db.collection("멍냥이")
                                        .document("유기동물${maxNumber+1}")
                                        .set(scheduleFireStoreMap)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                ContentValues.TAG,
                                                "DocumentSnapshot added with ID: ${documentReference}"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(ContentValues.TAG, "Error adding document", e)
                                        }
                                    storageRef.putFile(uploadUri!!)
                                        .addOnSuccessListener {
                                            // Get the download URL for the image
                                            storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                                val storageDB = FirebaseDatabase.getInstance("당신의스토리지링크")
                                                val postsRef = storageDB.getReference("유기동물_사진")
                                                postsRef.child("유기동물${maxNumber+1}").setValue(imageUrl.toString()).addOnCompleteListener {}
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            // Handle errors
                                            // You might want to show a Snackbar or Toast with an error message
                                        }
                                    resetMaxNum = true
                                }
                            } else {
                                resultText = "업로드 실패"
                            }
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            resultText = "업로드 실패, ${t.message}"
                        }
                    })
                }catch (e: Exception) {
                    e.printStackTrace()
                }
                //flaskSwitch = true
            }) {
                Text("등록하기")
            }
        }

        Spacer(modifier = Modifier.size(0.dp, 40.dp))
        Text(text = if(resultText != "") resultText else "", textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
    if(cameraOrGall){
        Dialog(onDismissRequest = { cameraOrGall = false }) {
            Surface(color=Color.White,
                modifier = Modifier
                    .width(300.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(25))
            ) {
                Box(modifier = Modifier.fillMaxSize()){
                    Row(modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)){
                        Box(modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(25))
                            .clickable {
                                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                cameraOrGall = false
                            }){
                            Column(modifier = Modifier.align(Alignment.Center)) {
                                Icon(painter = painterResource(id = R.drawable.gallery), contentDescription = "gallery",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.CenterHorizontally))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "갤러리", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                        Spacer(modifier = Modifier.size(16.dp,0.dp))
                        Divider(color = Color.LightGray, modifier = Modifier
                            .size(1.dp, 120.dp)
                            .align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.size(16.dp,0.dp))
                        Box(modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(25))
                            .clickable {
                                cameraLauncher.launch(null)
                                cameraOrGall = false
                            }){
                            Column(modifier = Modifier.align(Alignment.Center)) {
                                Icon(painter = painterResource(id = R.drawable.camera), contentDescription = "camera",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.CenterHorizontally))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "카메라", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    // 이미지 파일을 저장할 디렉토리 생성
    val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "images")
    imagesDir.mkdirs()

    // 파일명 생성
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val imageFile = File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",  /* suffix */
        imagesDir      /* directory */
    )

    // 이미지 파일에 비트맵 저장
    try {
        val fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    // 파일의 Uri를 반환
    return Uri.fromFile(imageFile)
}

fun uriToBitmap(uri: Uri, context: Context): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun encodeBitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StrayTheme {
    }
}