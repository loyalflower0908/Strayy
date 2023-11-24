package com.example.stray

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.annotations.SerializedName
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
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
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// 이미지 요청을 담을 데이터 클래스
data class ImageRequest(@SerializedName("img") val base64Image: String)

// Retrofit Interface 정의
interface ApiService {
    @POST("isimage/")
    fun postImage(@Body request: String): Call<String>
}

class ImageCache {
    private val cache = mutableMapOf<String, Bitmap>()

    fun getBitmap(url: String): Bitmap? {
        return cache[url]
    }

    fun putBitmap(url: String, bitmap: Bitmap) {
        cache[url] = bitmap
    }
}

val imageCache = ImageCache()


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(context: Context, db: FirebaseFirestore){
    val resources = context.resources
    var reverseGeoSwitch by remember { mutableStateOf(false) }
    var requestReverse by remember { mutableStateOf("") }
    var rGeoResult by remember { mutableStateOf("") }
    var geoCodeSwitch by remember {
        mutableStateOf(false)
    }
    var geoResult by remember { mutableStateOf("") }
    var locationGeo by remember{
        mutableStateOf("")
    }
    var locationSelect = remember {
        mutableStateMapOf(
            "서울" to false,
            "경기" to false,
            "인천" to false,
            "부산" to false,
            "대구" to false,
            "광주" to false,
            "대전" to false,
            "울산" to false,
            "경남" to false,
            "경북" to false,
            "충남" to false,
            "충북" to false,
            "전남" to false,
            "전북" to false,
            "강원" to false,
            "제주" to false,
            "세종" to false
        )
    }
    var satellite by remember {
        mutableStateOf(false)
    }
    var areaSearch by remember{
        mutableStateOf("")
    }
    var searchText by remember {
        mutableStateOf("")
    }
    var searchText2 by remember {
        mutableStateOf("")
    }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var speciesPage by remember { mutableStateOf(false) }
    var speciesSetting by remember { mutableStateOf("") }
    val dogCatMap = remember {
        mutableStateMapOf("강아지" to false, "고양이" to false)
    }
    var areaSet by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(false) }
    var selected2 by remember { mutableStateOf(false) }
    val seoul = LatLng(37.532600, 127.024612)
    var geoLatLng by remember{
        mutableStateOf(seoul)
    }
    var markerList by remember {
        mutableStateOf(mutableListOf<MarkerFormat?>())
    }
    var tempUri by remember {
        mutableStateOf("")
    }
    var markerSwitch by remember {
        mutableStateOf(false)
    }
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
                markerSwitch = true
            }
        }
    }
    LaunchedEffect(geoCodeSwitch) {
        // Use the coroutine scope to launch a background thread
        val job = CoroutineScope(Dispatchers.IO).launch {
            val address = locationGeo
            val clientId = "your_client_id"
            val clientSecret = "your_client_secret"
            val apiUrl = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode"
            val encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString())
            val url = "$apiUrl?query=$encodedAddress"

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
                    geoResult = response.toString()
                }
            } else {
                // Handle other response codes if needed
            }
        }
        //job.cancel()
        geoCodeSwitch = false
    }
    LaunchedEffect(reverseGeoSwitch) {
        // Use the coroutine scope to launch a background thread
        val job = CoroutineScope(Dispatchers.IO).launch {
            val rgLatLng = requestReverse
            val clientId = "your_client_id"
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
                    rGeoResult = response.toString()
                    rGeoResult = parseJson(rGeoResult)
                }
            } else {
                // Handle other response codes if needed
            }
        }
        //job.cancel()
        reverseGeoSwitch = false
    }
    val regex = Regex("""\"x\":\"([^\"]+)\",\"y\":\"([^\"]+)\"""")
    val matchResult = regex.find(geoResult)
    if(matchResult!=null){
        geoLatLng =  LatLng(matchResult.groupValues[2].toDouble(), matchResult.groupValues[1].toDouble())
    }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        // 카메라 초기 위치를 설정합니다.
        position = CameraPosition(seoul, 15.0)
    }
    var infoPage by remember { mutableStateOf(false) }
    var infoPageReady by remember { mutableStateOf(
        InfoPageData(
            img = null,
            text = "",
            species = ""
        ))
    }
    val markerClickListener = { marker: Marker ->
        // 클릭할 때마다 boolean 값 토글
        infoPage = !infoPage
        requestReverse = "${marker.position.longitude}, ${marker.position.latitude}"
        reverseGeoSwitch = true
        infoPageReady = InfoPageData(
            species = marker.captionText,
            text = marker.subCaptionText,
            img = markerList[marker.subCaptionText.split('/')[1].toInt()]?.img ?: "Error"
        )
        infoPage
    }
    Box(Modifier.fillMaxSize()) {
        NaverMap(locationSource = rememberFusedLocationSource(isCompassEnabled = true)
            , cameraPositionState = cameraPositionState
            , properties = MapProperties(mapType = if(!satellite) MapType.Basic else MapType.Satellite, maxZoom = NaverMapConstants.MaxZoom, minZoom = 5.0, locationTrackingMode = LocationTrackingMode.Follow)
            , uiSettings = MapUiSettings(isIndoorLevelPickerEnabled = true, isLocationButtonEnabled = true, isCompassEnabled = false, isLogoClickEnabled = false),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .height(screenHeight - 80.dp)
        ){
            if(markerSwitch) {
                for((index, markerData) in markerList.withIndex()) {
                    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                    val coroutineScope = rememberCoroutineScope()
                    if(markerData?.img != null && markerData.img != "Error"){
                        coroutineScope.launch {
                            loadImageAsBitmap(markerData.img) { loadedBitmap ->
                                bitmap = loadedBitmap
                            }
                        }
                    }
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                if (markerData?.latitude != null) markerData.latitude else 37.532600,
                                if (markerData?.longitude != null) markerData.longitude else 127.024612
                            )
                        ),
                        captionText = "종: ${if (markerData?.species != null) markerData.species else "Error"}",
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
                        subCaptionText = "발견시간: ${if(markerData?.time != null) markerData.time else "정보 없음"} /$index",
                        subCaptionColor = Color(0xffb640ff),
                        visible = if(speciesSetting == ""){
                            if(selected==false && selected2==false) true else if(markerData?.dogcat == "강아지") selected else !selected
                        } else speciesSetting == (markerData?.species ?: "Error"),
                        minZoom = 15.0,
                        onClick = markerClickListener
                    )
                }
            }
            if(geoLatLng != seoul){
                LaunchedEffect(key1 = geoLatLng){
                    cameraPositionState.move(CameraUpdate.toCameraPosition(CameraPosition(geoLatLng, 15.0)))
                }
            }
        }
        if(infoPage){
            Dialog(onDismissRequest = { infoPage = !infoPage }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                val coroutineScope = rememberCoroutineScope()
                if(infoPageReady?.img != null && infoPageReady.img != "Error"){
                    coroutineScope.launch {
                        loadImageAsBitmap(infoPageReady.img!!) { loadedBitmap ->
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
                                .size(300.dp, 300.dp)
                                .align(
                                    Alignment.CenterHorizontally
                                ).clip(RoundedCornerShape(10))
                                .border(3.dp, Color.Black, RoundedCornerShape(10)))
                            Spacer(modifier = Modifier.size(0.dp, 120.dp))
                            Text(text = infoPageReady.species, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(
                                Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.size(0.dp, 40.dp))
                            Text(text = infoPageReady.text.split('/')[0], fontSize = 16.sp, modifier = Modifier.align(
                                Alignment.CenterHorizontally))
                            Text(text = "발견 위치지: ${rGeoResult}", fontSize = 16.sp, modifier = Modifier.align(
                                Alignment.CenterHorizontally))
                        }
                    }
                }
            }
        }
        Column {
            Row(modifier = Modifier.padding(20.dp, 16.dp)) {
                AssistChip(onClick = { areaSet = !areaSet }, label = { Text("지역") }, leadingIcon = { Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "location button",
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
                }, colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(0.8f), labelColor = Color.Black), modifier = Modifier.align(
                    Alignment.CenterVertically))
                Spacer(modifier = Modifier.size(24.dp, 0.dp))
                AssistChip(onClick = { speciesPage = !speciesPage }, label = { Text("종 설정") }, leadingIcon = { Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "location button",
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
                }, colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(0.8f), labelColor = Color.Black), modifier = Modifier.align(
                    Alignment.CenterVertically))
                Spacer(modifier = Modifier.size(20.dp, 0.dp))
                FilterChip(
                    onClick = {
                        selected = !selected
                        selected2 = false
                    },
                    label = {
                        Text("개")
                    },
                    selected = selected,
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }, colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(0.8f), labelColor = Color.Black), modifier = Modifier.align(
                        Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.size(20.dp, 0.dp))
                FilterChip(
                    onClick = {
                        selected = false
                        selected2 = !selected2
                    },
                    label = {
                        Text("고양이")
                    },
                    selected = selected2,
                    leadingIcon = if (selected2) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }, colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(0.8f), labelColor = Color.Black), modifier = Modifier.align(
                        Alignment.CenterVertically)
                )
            }
            if(markerSwitch){
                Box(modifier = Modifier
                    .size(200.dp, 32.dp)
                    .padding(20.dp, 0.dp)
                    .clip(RoundedCornerShape(20))
                    .background(Color.White.copy(0.8f))
                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(20))){
                    Row(modifier = Modifier.align(Alignment.Center)) {
                        Box(modifier = Modifier
                            .size(60.dp, 32.dp)
                            .clickable { satellite = false }
                            .padding(8.dp, 0.dp)){
                            Text(text = "Map", fontSize = 12.sp, color = Color.Black, fontWeight = if(!satellite) FontWeight.Bold else FontWeight.Normal,modifier = Modifier.align(
                                Alignment.Center))
                        }
                        Divider(modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(1.dp, 10.dp))
                        Box(modifier = Modifier
                            .size(90.dp, 32.dp)
                            .clickable { satellite = true }
                            .padding(8.dp, 0.dp)){
                            Text(text = "Satellite", fontSize = 12.sp, color = Color.Black, fontWeight = if(satellite) FontWeight.Bold else FontWeight.Normal,modifier = Modifier.align(
                                Alignment.Center))
                        }
                    }
                }
                if(speciesSetting != ""){
                    FilterChip(
                        onClick = {
                            speciesSetting = ""
                        },
                        label = {
                            Text("분류: $speciesSetting")
                        },
                        selected = true,
                        leadingIcon = if (speciesSetting!="") {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Done icon",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }, colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(0.8f), labelColor = Color.Black), modifier = Modifier.padding(20.dp, 0.dp)
                    )
                }
                Spacer(modifier = Modifier.size(0.dp, 24.dp))
            }
        }
        if(areaSet){
            Dialog(onDismissRequest = { areaSet = !areaSet }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(modifier = Modifier.fillMaxSize()){
                    Surface(color = Color.White, modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight - 40.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)){
                                Text(text = "지역설정", fontSize = 16.sp, modifier = Modifier
                                    .align(Alignment.Center),
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold)
                                IconButton(onClick = { areaSet = false }, modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(16.dp, 0.dp)) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = "close",
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100))
                                            .background(Color.LightGray),
                                        tint = Color.Black)
                                }
                            }
                            Divider(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.size(0.dp, 24.dp))
                            TextField(
                                value = searchText,
                                onValueChange = { if(searchText.length != 20) searchText = it else searchText = it.dropLast(1) },
                                label = { Text("지역검색") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White, textColor = Color.Black),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .width(screenWidth - 40.dp)
                                    .border(width = 1.dp, color = Color.Black, shape = TextFieldDefaults.filledShape),
                                singleLine = true
                            )
                            Row(modifier = Modifier
                                .height(56.dp)
                                .horizontalScroll(
                                    rememberScrollState()
                                )){
                                Spacer(modifier = Modifier.size(24.dp, 0.dp))
                                if(searchText != ""){
                                    for(key in locationInfo.keys){
                                        if(" " in searchText){
                                            if(key == searchText.split(" ")[0] && locationInfo[key] != null){
                                                for(value in locationInfo[key]!!){
                                                    if(searchText.split(" ")[1] in value){
                                                        SuggestionChip(
                                                            onClick = {
                                                                locationGeo = "${
                                                                    locationInfo[key]?.get(
                                                                        0
                                                                    )
                                                                } $value"
                                                                searchText = ""
                                                                geoCodeSwitch = true
                                                                areaSet = false
                                                            },
                                                            label = { Text(text = value, color = Color.Black) },
                                                            modifier = Modifier.align(Alignment.CenterVertically))
                                                        Spacer(modifier = Modifier.size(16.dp, 0.dp))
                                                    }
                                                }
                                            }
                                        }else{
                                            if(searchText in key){
                                                SuggestionChip(
                                                    onClick = {
                                                        searchText = "$key "
                                                    },
                                                    label = { Text(text = key, color = Color.Black) },
                                                    modifier = Modifier.align(Alignment.CenterVertically))
                                                Spacer(modifier = Modifier.size(16.dp, 0.dp))
                                            }
                                        }
                                    }
                                }
                            }
//                                            Spacer(modifier = Modifier.size(0.dp, 56.dp))
                            Divider(thickness = 1.dp,modifier = Modifier.fillMaxWidth())
                            Row {
                                Box(modifier = Modifier
                                    .width(screenWidth / 8 * 3 - 1.dp)
                                    .height(48.dp)
                                ){
                                    Text(text = "시/도", modifier = Modifier.align(
                                        Alignment.Center
                                    ))
                                }
                                Divider(modifier = Modifier
                                    .size(1.dp, 24.dp)
                                    .align(Alignment.CenterVertically))
                                Box(modifier = Modifier
                                    .width(screenWidth / 8 * 5)
                                    .height(48.dp)){
                                    Text(text = "시/구/군", modifier = Modifier.align(
                                        Alignment.Center
                                    ))
                                }
                            }
                            Divider()
                            Row {
                                Column(modifier = Modifier
                                    .width(screenWidth / 8 * 3 - 1.dp)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())){
                                    for(key in locationInfo.keys){
                                        Box(modifier = Modifier
                                            .width(screenWidth / 8 * 3 - 1.dp)
                                            .height(48.dp)
                                            .clickable {
                                                areaSearch = key
                                                for (k in locationSelect.keys) {
                                                    locationSelect[k] = false
                                                }
                                                locationSelect[key] = true
                                            }
                                            .drawBehind { // 여기에 원하는 그리기 작업을 수행합니다.
                                                if (locationSelect[key]!!) {
                                                    val topLeft = Offset(0f, 0f)
                                                    val topRight =
                                                        Offset(size.width, 0f)
                                                    val bottomLeft =
                                                        Offset(0f, size.height)
                                                    val bottomRight =
                                                        Offset(
                                                            size.width,
                                                            size.height
                                                        )
                                                    drawLine(
                                                        Color.Black,
                                                        topLeft,
                                                        topRight,
                                                        1f
                                                    )
                                                    drawLine(
                                                        Color.Black,
                                                        bottomLeft,
                                                        bottomRight,
                                                        1f
                                                    )
                                                } else {
                                                    val topRight =
                                                        Offset(size.width, 0f)
                                                    val bottomRight =
                                                        Offset(
                                                            size.width,
                                                            size.height
                                                        )
                                                    drawLine(
                                                        Color.Black,
                                                        topRight,
                                                        bottomRight,
                                                        1f
                                                    )
                                                }
                                            }
                                            .background(if (locationSelect[key]!!) Color.LightGray.copy(0.8f) else Color.Transparent)
                                        ){
                                            Text(text = key, modifier = Modifier
                                                .align(
                                                    Alignment.Center
                                                ))
                                        }
                                    }
                                }
                                Column(modifier = Modifier
                                    .width(screenWidth / 8 * 5)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())){
                                    if(locationInfo[areaSearch]!=null){
                                        Box(modifier = Modifier
                                            .width(screenWidth / 8 * 5)
                                            .height(48.dp)
                                            .clickable {
                                                locationGeo = "${
                                                    locationInfo[areaSearch]?.get(
                                                        0
                                                    )
                                                }"
                                                geoCodeSwitch = true
                                                areaSet = false
                                            }){
                                            Text(text = "전체", modifier = Modifier
                                                .align(
                                                    Alignment.Center
                                                ))
                                        }
                                        for(value in locationInfo[areaSearch]!!.subList(1, locationInfo[areaSearch]!!.size)){
                                            Box(modifier = Modifier
                                                .width(screenWidth / 8 * 5)
                                                .height(48.dp)
                                                .clickable {
                                                    locationGeo = "${
                                                        locationInfo[areaSearch]?.get(
                                                            0
                                                        )
                                                    } ${value}"
                                                    geoCodeSwitch = true
                                                    areaSet = false
                                                }){
                                                Text(text = value, modifier = Modifier
                                                    .align(
                                                        Alignment.Center
                                                    ))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(speciesPage){
            Dialog(onDismissRequest = { speciesPage = !speciesPage }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(modifier = Modifier.fillMaxSize()){
                    Surface(color = Color.White, modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight - 40.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)){
                                Text(text = "종 설정", fontSize = 16.sp, modifier = Modifier
                                    .align(Alignment.Center),
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold)
                                IconButton(onClick = { speciesPage = false }, modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(16.dp, 0.dp)) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = "close",
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100))
                                            .background(Color.LightGray),
                                        tint = Color.Black)
                                }
                            }
                            Divider(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.size(0.dp, 24.dp))
                            TextField(
                                value = searchText2,
                                onValueChange = { if(searchText2.length != 20) searchText2 = it else searchText2 = it.dropLast(1) },
                                label = { Text("종 검색") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White, textColor = Color.Black),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .width(screenWidth - 40.dp)
                                    .border(width = 1.dp, color = Color.Black, shape = TextFieldDefaults.filledShape),
                                singleLine = true
                            )
                            Row(modifier = Modifier
                                .height(56.dp)
                                .horizontalScroll(
                                    rememberScrollState()
                                )){
                                Spacer(modifier = Modifier.size(24.dp, 0.dp))
                                if(searchText2 != ""){
                                    for(species in speciesList){
                                        if(searchText2 in species){
                                            SuggestionChip(
                                                onClick = {
                                                    speciesSetting = species
                                                    speciesPage = false
                                                },
                                                label = { Text(text = species, color = Color.Black) },
                                                modifier = Modifier.align(Alignment.CenterVertically))
                                            Spacer(modifier = Modifier.size(16.dp, 0.dp))
                                        }
                                    }
                                }
                            }
                            Divider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
                            Row {
                                Box(modifier = Modifier
                                    .width(screenWidth / 8 * 3 - 1.dp)
                                    .height(48.dp)
                                ){
                                    Text(text = "견/묘", modifier = Modifier.align(
                                        Alignment.Center
                                    ))
                                }
                                Divider(modifier = Modifier
                                    .size(1.dp, 24.dp)
                                    .align(Alignment.CenterVertically))
                                Box(modifier = Modifier
                                    .width(screenWidth / 8 * 5)
                                    .height(48.dp)){
                                    Text(text = "종 찾기", modifier = Modifier.align(
                                        Alignment.Center
                                    ))
                                }
                            }
                            Divider()
                            Row {
                                Column(modifier = Modifier
                                    .width(screenWidth / 8 * 3 - 1.dp)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())){
                                    for(key in dogCatMap.keys) {
                                        Box(modifier = Modifier
                                            .width(screenWidth / 8 * 3 - 1.dp)
                                            .height(48.dp)
                                            .clickable {
                                                for (k in dogCatMap.keys) {
                                                    dogCatMap[k] = false
                                                }
                                                dogCatMap[key] = true
                                            }
                                            .drawBehind { // 여기에 원하는 그리기 작업을 수행합니다.
                                                if (dogCatMap[key]!!) {
                                                    val topLeft = Offset(0f, 0f)
                                                    val topRight =
                                                        Offset(size.width, 0f)
                                                    val bottomLeft =
                                                        Offset(0f, size.height)
                                                    val bottomRight =
                                                        Offset(
                                                            size.width,
                                                            size.height
                                                        )
                                                    drawLine(
                                                        Color.Black,
                                                        topLeft,
                                                        topRight,
                                                        1f
                                                    )
                                                    drawLine(
                                                        Color.Black,
                                                        bottomLeft,
                                                        bottomRight,
                                                        1f
                                                    )
                                                } else {
                                                    val topRight =
                                                        Offset(size.width, 0f)
                                                    val bottomLeft =
                                                        Offset(0f, size.height)
                                                    val bottomRight =
                                                        Offset(
                                                            size.width,
                                                            size.height
                                                        )
                                                    drawLine(
                                                        Color.Black,
                                                        topRight,
                                                        bottomRight,
                                                        1f
                                                    )
                                                    drawLine(
                                                        Color.Black,
                                                        bottomLeft,
                                                        bottomRight,
                                                        1f
                                                    )
                                                }
                                            }
                                            .background(if (dogCatMap[key]!!) Color.LightGray.copy(0.8f) else Color.Transparent)
                                        ){
                                            Text(text = key, modifier = Modifier
                                                .align(
                                                    Alignment.Center
                                                ))
                                        }
                                    }
                                }
                                Column(modifier = Modifier
                                    .width(screenWidth / 8 * 5)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())){
                                    if(dogCatMap.get("고양이") == true){
                                        for(index in 0 until 12) {
                                            Box(modifier = Modifier
                                                .width(screenWidth / 8 * 5)
                                                .height(48.dp)
                                                .clickable {
                                                    speciesSetting = speciesList[index]
                                                    speciesPage = false
                                                }){
                                                Text(text = speciesList[index], modifier = Modifier
                                                    .align(
                                                        Alignment.Center
                                                    ))
                                            }
                                        }
                                    }else if(dogCatMap.get("강아지") == true){
                                        for(index in 12..36){
                                            Box(modifier = Modifier
                                                .width(screenWidth / 8 * 5)
                                                .height(48.dp)
                                                .clickable {
                                                    speciesSetting = speciesList[index]
                                                    speciesPage = false
                                                }){
                                                Text(text = speciesList[index], modifier = Modifier
                                                    .align(
                                                        Alignment.Center
                                                    ))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getCurrentLocation(context: Context, requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            return null
        }
    }

    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    return location
}

suspend fun loadImageAsBitmap(
    imageUrl: String,
    onBitmapLoaded: (Bitmap) -> Unit
) {
    val cachedBitmap = imageCache.getBitmap(imageUrl)
    if (cachedBitmap != null) {
        // 이미지가 캐시에 있으면 캐시된 이미지를 사용
        onBitmapLoaded(cachedBitmap)
    } else {
        try {
            withContext(Dispatchers.IO) {
                val urlConnection = URL(imageUrl).openConnection() as HttpURLConnection
                urlConnection.connect()

                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = urlConnection.inputStream
                    val loadedBitmap = BitmapFactory.decodeStream(inputStream)

                    // 이미지를 비트맵으로 변환 후 캐시에 저장
                    imageCache.putBitmap(imageUrl, loadedBitmap)

                    withContext(Dispatchers.Main) {
                        onBitmapLoaded(loadedBitmap)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun parseJson(jsonString: String): String {
    val jsonObject = JSONObject(jsonString)
    val resultsArray = jsonObject.getJSONArray("results")

    val areaNames = StringBuilder()

    for (i in 0 until resultsArray.length()) {
        val resultObject = resultsArray.getJSONObject(i)
        val regionObject = resultObject.getJSONObject("region")

        val area1 = regionObject.getJSONObject("area1").getString("name")
        val area2 = regionObject.getJSONObject("area2").getString("name")
        val area3 = regionObject.getJSONObject("area3").getString("name")
        val area4 = regionObject.getJSONObject("area4").getString("name")

        areaNames.append("$area1 $area2 $area3")
    }

    return areaNames.toString().trim()
}