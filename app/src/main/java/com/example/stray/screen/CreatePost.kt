package com.example.stray.screen

import com.example.stray.database.FirebaseDataManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.stray.Screen
import com.example.stray.StrayImage
import com.example.stray.TopImage
import com.example.stray.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun CreatePostScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // com.example.getiproject.database.FirebaseDataManager().createPost(post)를 사용하여 새로운 게시글 생성
    val firebaseDataManager = FirebaseDataManager()

    // 예를 들어, FirebaseAuth.getInstance().currentUser를 사용하여 현재 로그인한 사용자 정보를 가져올 수 있습니다.
    val currentUser = FirebaseAuth.getInstance().currentUser
    val author = currentUser?.displayName ?: "Unknown"

    // 현재 시간을 가져와 timestamp로 사용
    val formattedTimestamp = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    ).format(System.currentTimeMillis())

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val getMultipleContents =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
            uris?.let {
                // Replace the selected image URIs with the new list
                selectedImageUris = it
            }
        }
    TopImage(Modifier)
    Column {
        StrayImage()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, top = 90.dp, bottom = 60.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("제목을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF7F8F9),
                        textColor = Color.Black,
                        focusedBorderColor = Color.Transparent, // 포커스된 경우의 테두리 색상
                        unfocusedBorderColor = Color.Transparent, // 포커스가 해제된 경우의 테두리 색상
                    ),
                    shape = RectangleShape
                )

                // 내용 입력칸
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
//                    label = { Text("내용") },
                    placeholder = { Text("내용을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .height(200.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF7F8F9),
                        textColor = Color.Black,
                            focusedBorderColor = Color.Transparent, // 포커스된 경우의 테두리 색상
                        unfocusedBorderColor = Color.Transparent, // 포커스가 해제된 경우의 테두리 색상
                    ),
                    shape = RectangleShape
                )
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            // Use the activity result API to get content (images) from the gallery
                            getMultipleContents.launch("image/*")
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .padding(8.dp),
//                        .align(Alignment.End), // 오른쪽 정렬 적용

                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3C9FF))
                    ) {
                        Text(text = "이미지 선택")
                    }

                }
                // 이미지 선택 버튼

                selectedImageUris.forEach { uri ->
                    Image(
                        painter = rememberImagePainter(data = uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                            .clip(shape = RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.BottomCenter) {
        Button(
            onClick = {
                // Your save logic here, whether images are selected or not
                val imageUrls = mutableListOf<String>()

                // Upload each selected image
                selectedImageUris.forEachIndexed { index, uri ->
                    val imageName = "${UUID.randomUUID()}_$index.jpg"
                    val storageRef =
                        FirebaseStorage.getInstance().reference.child("images/$imageName")

                    storageRef.putFile(uri)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                imageUrls.add(imageUrl.toString())

                                if (imageUrls.size == selectedImageUris.size) {
                                    val newPost = Post(
                                        postId = "",
                                        title = title,
                                        content = content,
                                        author = author,
                                        imageUrls = imageUrls,
                                        timestamp = formattedTimestamp
                                    )

                                    // Create the post in Firebase
                                    firebaseDataManager.createPost(newPost)
                                    navController.navigate(Screen.CommunityHome.route)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->

                        }
                }

                // If no images are selected, create a new post without image URLs
                if (selectedImageUris.isEmpty()) {
                    val newPost = Post(
                        postId = "",
                        title = title,
                        content = content,
                        author = author,
                        imageUrls = emptyList(),
                        timestamp = formattedTimestamp
                    )

                    // Create the post in Firebase
                    firebaseDataManager.createPost(newPost)
                }
                navController.navigate(Screen.CommunityHome.route)

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 120.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3C9FF))

        ) {
            Text(text = "저장")
        }

    }
}

