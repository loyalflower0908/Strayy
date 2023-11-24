package com.example.stray.screen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.stray.BarState
import com.example.stray.MarkerFormat
import com.example.stray.R
import com.example.stray.Screen
import com.example.stray.StrayImage
import com.example.stray.TopImage
import com.example.stray.loadImageAsBitmap
import com.example.stray.parseJson
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapType
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.NaverMapConstants
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

val strangeCat = listOf(R.drawable.strange, R.drawable.strange2, R.drawable.strange3, R.drawable.strange4, R.drawable.strange5)

@OptIn(ExperimentalNaverMapApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SuccessLogin(navController: NavController, dot: Painter, db:FirebaseFirestore, temp:Painter, context: Context, maxNum:Int) {
    var popUpIndex by remember { mutableStateOf(0) }
    var nearPage by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var catPower by remember { mutableStateOf(0) }
    var strangePage by remember { mutableStateOf(false) }
    var requestReverse by remember { mutableStateOf("") }
    var rGeoResult by remember { mutableStateOf(mutableListOf<String>()) }

    var markerList by remember {
        mutableStateOf(mutableListOf<MarkerFormat?>())
    }
    var tempUri by remember {
        mutableStateOf("")
    }
    var markerSwitch by remember {
        mutableStateOf(false)
    }
    val resources = context.resources
    var progress by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit){
        val scheduleDoc = db.collection("멍냥이").get().await()
        for(document in scheduleDoc.documents){
            val storageRef = FirebaseStorage.getInstance().reference.child("images/${document.id}")
            storageRef.downloadUrl.addOnSuccessListener { uri->
                tempUri = uri.toString()
            }.addOnFailureListener{
                tempUri = "Error"
            }.addOnCompleteListener{
                markerList.add(MarkerFormat(
                    img = tempUri,
                    dogcat = document.data?.get("분류").toString() ?:"오류",
                    latitude = document.data?.get("위도").toString().toDouble() ?: 37.532600,
                    longitude = document.data?.get("경도").toString().toDouble() ?: 127.024612,
                    species = document.data?.get("종").toString() ?:"오류",
                    time = document.data?.get("발견시간").toString() ?: "오류"))
                requestReverse = "${document.data?.get("경도").toString().toDouble() ?: 126.967218}, ${document.data?.get("위도").toString().toDouble() ?: 37.560362}"
                val job = CoroutineScope(Dispatchers.IO).launch {
                    val rgLatLng = requestReverse
                    val clientId = "your_clientId"
                    val clientSecret = "your_client_Secret"
                    val url = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=$rgLatLng&sourcecrs=epsg:4326&orders=addr&output=json"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId)
                    connection.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret)

                    val responseCode = connection.responseCode

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        var line: String?
                        val response = StringBuilder()

                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }

                        reader.close()

                        withContext(Dispatchers.Main) {
                            val result = response.toString()
                            rGeoResult.add(parseJson(result))
                        }
                    } else {
                        // Handle other response codes if needed
                    }
                }
                markerSwitch = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        TopImage(Modifier = Modifier.align(Alignment.TopCenter))
        StrayImage()
        Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = "account", tint = Color.White, modifier = Modifier
            .padding(8.dp, 16.dp)
            .size(48.dp)
            .clickable {
                navController.navigate(Screen.UserInfo.route)
            })
        Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "account", tint = Color.White, modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp, 16.dp)
            .size(40.dp))
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(text = "전체 유기동물 수: ${maxNum}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier
                .align(Alignment.Start)
                .padding(20.dp, 0.dp))
            Spacer(modifier = Modifier.height(64.dp))
            Text(text = "내 근처 유기동물", fontSize = 18.sp, color = Color(0xff020715), fontWeight = FontWeight.ExtraBold, modifier = Modifier
                .align(Alignment.Start)
                .padding(((screenWidth - 300) / 2).dp, 0.dp))
            Spacer(modifier = Modifier.size(0.dp, 12.dp))
            Row(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .horizontalScroll(scrollState)) {
                repeat(5) {
                    Spacer(modifier = Modifier.size(((screenWidth-338)/2).dp, 0.dp))
                    Box(
                        modifier = Modifier
                            .width(338.dp)
                            .height(183.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(percent = 20),
                                spotColor = Color.Black,
                                ambientColor = Color.Black
                            )
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(size = 20.dp)
                            )
                            .clip(RoundedCornerShape(percent = 20))
                            .align(Alignment.CenterVertically)
                            .clickable {
                                popUpIndex = it
                                nearPage = !nearPage
                            }
                    ) {
                        if(markerSwitch && markerList.size>5) {
                            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                            val coroutineScope = rememberCoroutineScope()
                            if(markerList[it]?.img != null && markerList[it]?.img != "Error"){
                                coroutineScope.launch {
                                    loadImageAsBitmap(markerList[it]!!.img) { loadedBitmap ->
                                        bitmap = loadedBitmap
                                    }
                                }
                            }
                            Row(modifier = Modifier.align(Alignment.Center)) {
                                Image(
                                    bitmap = bitmap?.asImageBitmap() ?: BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.malti
                                    ).asImageBitmap(),
                                    contentDescription = "animal",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(160.dp, 108.dp)
                                        .clip(
                                            RoundedCornerShape(10)
                                        )
                                        .border(1.dp, Color.Black, shape = RoundedCornerShape(10))
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    Text(
                                        text = "종: ${markerList[it]?.species ?: "오류"}\n발견날짜 : ${markerList[it]?.time?.split(" ")
                                            ?.get(1) ?: "XX월"}${markerList[it]?.time?.split(" ")
                                            ?.get(2) ?: "XX일"}\n발견위치 : ${rGeoResult[it].split(' ')[2]}",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.58.sp,
                                            fontWeight = FontWeight(600),
                                            color = Color.Black,
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(((screenWidth-338)/2).dp, 0.dp))
                }
            }
            Spacer(modifier = Modifier.size(0.dp, 16.dp))
            DottedProgressBar(progress = progress, dot = dot, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.size(0.dp, 36.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())){
                for(cat in strangeCat){
                    Spacer(modifier = Modifier.width(24.dp))
                    Box(
                        modifier = Modifier
                            .width(216.dp)
                            .height(303.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(percent = 20),
                                spotColor = Color.Black,
                                ambientColor = Color.Black
                            )
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(size = 20.dp)
                            )
                            .clip(RoundedCornerShape(percent = 20))
                            .clickable {
                                strangePage = !strangePage
                                catPower = cat
                            }
                            .align(Alignment.CenterVertically)
                        ){
                        Column(modifier = Modifier
                            .align(
                                Alignment.TopCenter
                            )
                            .padding(top = 16.dp)) {
                            Image(painter = painterResource(id = cat), contentDescription = "cat!", contentScale = ContentScale.Crop, modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(160.dp, 160.dp)
                                .clip(RoundedCornerShape(20))
                                .border(2.dp, Color(0xff020715), shape = RoundedCornerShape(20)))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(text = "구출된 고양이가\n당신을 기다려요.", fontSize = 20.sp, lineHeight = 32.sp, color = Color(0xff020715), textAlign = TextAlign.Center, modifier = Modifier.align(
                                Alignment.CenterHorizontally
                            ))
                        }
                    }

                }
                Spacer(modifier = Modifier.width(24.dp))
            }

        }
    }
    LaunchedEffect(scrollState.value) {
        val currentScroll = scrollState.value
        if(progress == 0){
            if(currentScroll > scrollState.viewportSize/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.viewportSize)
                }
                if(currentScroll in scrollState.viewportSize-80..scrollState.viewportSize+80){
                    progress = 1
                }
            }
        }else if(progress == 1){
            if(currentScroll > scrollState.viewportSize*5/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.viewportSize*2)
                }
                if(currentScroll in scrollState.viewportSize*2-80 .. scrollState.viewportSize*2+80){
                    progress = 2
                }
            }else if(currentScroll < scrollState.viewportSize*3/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(0)
                }
                if(currentScroll <= 80){
                    progress = 0
                }
            }
        }else if(progress == 2){
            if(currentScroll > scrollState.viewportSize*9/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.viewportSize*3)
                }
                if(currentScroll in scrollState.viewportSize*3-80..scrollState.viewportSize*3+80){
                    progress = 3
                }
            }else if(currentScroll < scrollState.viewportSize*7/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.viewportSize)
                }
                if(currentScroll in scrollState.viewportSize-80 .. scrollState.viewportSize+80){
                    progress = 1
                }
            }
        }else if(progress == 3){
            if(currentScroll > scrollState.viewportSize*13/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.viewportSize*4)
                }
                if(currentScroll in scrollState.viewportSize*4-80 .. scrollState.viewportSize*4+80){
                    progress = 4
                }
            }else if(currentScroll < scrollState.viewportSize*11/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.viewportSize*2)
                }
                if(currentScroll in scrollState.viewportSize*2-80 .. scrollState.viewportSize*2+80){
                    progress = 2
                }
            }
        }else if(progress == 4){
            if(currentScroll < scrollState.viewportSize*15/4){
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.viewportSize*3)
                }
                if(currentScroll in scrollState.viewportSize*3-80 .. scrollState.viewportSize*3+80){
                    progress = 3
                }
            }
        }
    }
    if(strangePage){
        Dialog(onDismissRequest = { strangePage = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = Color(0xffeeffff), modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight - 40.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 90.dp)){
                        Image(painter = painterResource(id = catPower), contentDescription = "power", contentScale = ContentScale.Crop, modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(300.dp, 300.dp)
                            .clip(RoundedCornerShape(20))
                            .border(2.dp, Color(0xff020715), shape = RoundedCornerShape(20)))
                        Spacer(modifier = Modifier.height(48.dp))
                        Text(text = "구출된 고양이가\n당신을 기다려요.\n입양을 위해서\n연락주세요.", fontSize = 24.sp, lineHeight = 40.sp, color = Color(0xff020715), textAlign = TextAlign.Center, modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        ))
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(onClick = {  }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text(text = "입양하기", fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
    if(nearPage){
        Dialog(onDismissRequest = { nearPage = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
            val coroutineScope = rememberCoroutineScope()
            if(markerList[popUpIndex]?.img != null && markerList[popUpIndex]?.img != "Error"){
                coroutineScope.launch {
                    loadImageAsBitmap(markerList[popUpIndex]!!.img) { loadedBitmap ->
                        bitmap = loadedBitmap
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = Color.White, modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight - 40.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    Column {
                        Spacer(modifier = Modifier.size(0.dp, 80.dp))
                        Image(bitmap = if(bitmap !=null) bitmap!!.asImageBitmap() else BitmapFactory.decodeResource(
                            resources,
                            R.drawable.malti
                        ).asImageBitmap(), contentDescription = "InfoAnimal", contentScale = ContentScale.Crop, modifier = Modifier
                            .size(240.dp, 240.dp)
                            .align(
                                Alignment.CenterHorizontally
                            )
                            .clip(RoundedCornerShape(10))
                            .border(3.dp, Color.Black, RoundedCornerShape(10)))
                        Spacer(modifier = Modifier.size(0.dp, 64.dp))
                        Text(text = "종: ${ markerList[popUpIndex]!!.species }", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(
                            Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.size(0.dp, 40.dp))
                        Text(text = "발견 시간: ${markerList[popUpIndex]!!.time}", fontSize = 16.sp, modifier = Modifier.align(
                            Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.size(0.dp, 40.dp))
                        val seoul = LatLng(if (markerList[popUpIndex]?.latitude != null) markerList[popUpIndex]!!.latitude else 37.532600, if (markerList[popUpIndex]?.longitude != null) markerList[popUpIndex]!!.longitude else 127.024612)
                        val cameraPositionState: CameraPositionState = rememberCameraPositionState {
                            // 카메라 초기 위치를 설정합니다.
                            position = CameraPosition(seoul, 18.0)
                        }
                        Divider()
                        NaverMap(locationSource = rememberFusedLocationSource(isCompassEnabled = true)
                            , cameraPositionState = cameraPositionState
                            , properties = MapProperties(mapType = MapType.Basic, maxZoom = NaverMapConstants.MaxZoom, minZoom = 5.0, locationTrackingMode = LocationTrackingMode.None)
                            , uiSettings = MapUiSettings(isIndoorLevelPickerEnabled = true, isLocationButtonEnabled = false, isCompassEnabled = false, isLogoClickEnabled = false),
                            modifier = Modifier
                                .fillMaxSize()
                        ){
                            Marker(
                                state = MarkerState(
                                    position = seoul
                                ),
                                captionText = "종: ${if (markerList[popUpIndex]?.species != null) markerList[popUpIndex]!!.species else "Error"}",
                                icon = if (bitmap != null) OverlayImage.fromBitmap(
                                    bitmap!!
                                ) else OverlayImage.fromBitmap(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.malti
                                    )
                                ),
                                width = 40.dp,
                                height = 40.dp,
                                subCaptionText = "발견시간: ${if(markerList[popUpIndex]?.time != null) markerList[popUpIndex]!!.time else "정보 없음"}",
                                subCaptionColor = Color(0xffb640ff),
                                minZoom = 15.0
                            )
                        }
                    }
                }
            }
        }
    }
    var adDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit){
        adDialog = true
    }
    if(adDialog){
        Dialog(onDismissRequest = { adDialog = false}) {
            Image(painter = painterResource(id = R.drawable.ad), contentDescription = "ad",
                modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun DottedProgressBar(progress: Int, dot: Painter, modifier: Modifier) {
    val spacing = 4.dp
    val totalDots = 5
    val dotSize = 20.dp
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .height(dotSize)
    ) {
        repeat(totalDots) { index ->
            val colorChange by animateColorAsState(
                targetValue = if (index == progress) Color(0xff3D5BF6) else Color.LightGray,
                animationSpec = tween(durationMillis = 250), label = ""
            )
            Icon(
                painter = dot,
                contentDescription = null,
                modifier = Modifier
                    .size(dotSize)
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = spacing)
                    .background(Color.Transparent),
                tint = colorChange
            )
        }
    }
}
