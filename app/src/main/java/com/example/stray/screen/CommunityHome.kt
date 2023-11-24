package com.example.stray.screen

import com.example.stray.database.FirebaseDataManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import com.example.stray.R
import com.example.stray.Screen
import com.example.stray.StrayImage
import com.example.stray.TopImage
import com.example.stray.data.Post
import com.example.stray.data.SearchType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHome(navController: NavController) {
    val firebaseDataManager = FirebaseDataManager()

    // State to hold the list of posts
    var postsState by remember { mutableStateOf<List<Post>>(emptyList()) }

    // State to track the number of posts to fetch
    var numberOfPostsToFetch by remember { mutableStateOf(10) }

    // State to hold the search query
    var searchQuery by remember { mutableStateOf("") }

    // State to track the selected search type
    var searchType by remember { mutableStateOf(SearchType.TITLE) }

    var isTitleSelected by remember { mutableStateOf(true) }

//     Use Effect to fetch the initial posts when the composable is first created
    LaunchedEffect(searchQuery, searchType) {
        firebaseDataManager.postsRef
            .limitToLast(numberOfPostsToFetch)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postList = snapshot.children.mapNotNull { it.getValue(Post::class.java) }

                    // Filter posts based on search criteria locally
                    postsState = postList.filter { post ->
                        when (searchType) {
                            SearchType.TITLE -> post.title.contains(searchQuery, ignoreCase = true)
                            SearchType.CONTENT -> post.content.contains(
                                searchQuery,
                                ignoreCase = true
                            )

                            SearchType.AUTHOR -> post.author.contains(
                                searchQuery,
                                ignoreCase = true
                            )

                            SearchType.BOTH ->
                                post.title.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) || post.content.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                    Log.e("CommunityHome", "Error fetching posts: ${error.message}")
                }
            })
    }
    TopImage(Modifier)
    StrayImage()
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search input field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
//                label = { Text("검색어를 입력하세요") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, start = 30.dp, end = 30.dp)
                .background(color = Color.White, shape = RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp)),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White, // 포커스된 경우의 테두리 색상
                unfocusedBorderColor = Color.White, // 포커스가 해제된 경우의 테두리 색상
            ),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_search_24),
                    contentDescription = "검색",
                    modifier = Modifier
                        .size(24.dp) // 아이콘 크기 조절
                )
            }
        )
        Row(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            RadioGroupOptions(
                options = listOf("제목", "내용", "제목 및 내용"),
                selectedOption = if (isTitleSelected) "제목" else "내용", // Update selectedOption based on the state
                onOptionSelected = { selectedOption ->
                    searchType = when (selectedOption) {
                        "제목" -> {
                            isTitleSelected = true
                            SearchType.TITLE
                        }

                        "내용" -> {
                            isTitleSelected = false
                            SearchType.CONTENT
                        }

                        else -> SearchType.BOTH
                    }
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
        ) {
            LazyColumn {
                items(postsState.filter {
                    when (searchType) {
                        SearchType.TITLE -> it.title.contains(searchQuery, ignoreCase = true)
                        SearchType.CONTENT -> it.content.contains(
                            searchQuery,
                            ignoreCase = true
                        )

                        SearchType.AUTHOR -> it.author.contains(searchQuery, ignoreCase = true)


                        SearchType.BOTH ->
                            it.title.contains(searchQuery, ignoreCase = true) ||
                                    it.content.contains(searchQuery, ignoreCase = true)
                    }
                }.reversed()) { post ->
                    // Display each post in the LazyColumn in reverse order
                    PostItem(post = post, firebaseDataManager) {
                        // Navigate to the PostDetail screen when a post is clicked
                        navController.navigate(Screen.PostDetail.route + "/${post.postId}")
                    }
                }
                // "더보기" (more) button
                item {
                    // Display the "더보기" button at the bottom of the LazyColumn
                    Button(
                        onClick = {
                            // Increase the number of posts to fetch
                            numberOfPostsToFetch += 10
                            // Fetch additional posts from Firebase
                            firebaseDataManager.postsRef
                                .limitToLast(numberOfPostsToFetch)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val postList =
                                            snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                                        // Update the state with the new list of posts
                                        postsState = postList
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Handle error if needed
                                        Log.e(
                                            "CommunityHome",
                                            "Error fetching additional posts: ${error.message}"
                                        )
                                    }
                                })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 아래 화살표 아이콘
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_downward_24),
                                contentDescription = "더보기",
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // "더보기" 버튼 텍스트
                            Text("더보기")
                        }
                    }
                }
            }

        }
    }



    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp, 120.dp)
    ) {
        Button(
            onClick = {
                navController.navigate(Screen.CreatePostScreen.route)
            },
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3C9FF)),
                    contentPadding = PaddingValues(0.dp) // 내부 패딩
        ) {
            Icon(
                imageVector = Icons.Default.Add, // 플러스 아이콘 사용
                contentDescription = "Add" +
                        "추가" +
                        "작성",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun RadioGroupOptions(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val radioGroup = remember { mutableStateOf(selectedOption) }
    Row {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .clickable {
                        radioGroup.value = option
                        onOptionSelected(option)
                    }
            ) {
                RadioButton(
                    selected = option == radioGroup.value,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = option)
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PostItem(post: Post, firebaseDataManager: FirebaseDataManager, onPostClick: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, top = 7.dp)
                .clickable {
                    // Increment hits when a post is clicked
                    val updatedHits = post.hits + 1
                    firebaseDataManager.updatePostHits(post.postId, updatedHits)
                    onPostClick.invoke()
                }
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(percent = 10),
                    spotColor = Color.Black,
                    ambientColor = Color.Black
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(size = 20.dp)
                )
                .clip(RoundedCornerShape(percent = 10))
                .align(Alignment.CenterHorizontally)

        ) {
            Column {
                Row(
                    modifier = Modifier.padding(10.dp)
                ) {
                    if (post.imageUrls != null && post.imageUrls.isNotEmpty()) {
                        // Display the image if it exists
                        Image(
                            painter = painterResource(id = R.drawable.baseline_image_24),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(4.dp))
                        )
                    } else {
                        // Display a placeholder image (e.g., baseline_chat_24) if no image exists
                        Image(
                            painter = painterResource(id = R.drawable.baseline_chat_24),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(4.dp))
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        text = post.title
                    )
                }
                LazyRow {
                    items(post.imageUrls.orEmpty()) { imageUrl ->
                        Image(
                            painter = rememberImagePainter(
                                data = imageUrl,
                                builder = {
                                    transformations(RoundedCornersTransformation(4f))
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp) // Adjust the size as needed
                                .padding(start = 15.dp) // Add padding between images
                                .clip(shape = RoundedCornerShape(15.dp))
                        )
                    }
                }
                Text(
                    text = post.content.take(30),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .heightIn(min = 30.dp) // Limit the height to show only one line
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .fillMaxWidth()
                ) {
                    Text(fontSize = 13.sp, text = post.author, color = Color.Gray)
                    Spacer(modifier = Modifier.padding(start = 7.dp))
                    Divider(
                        modifier = Modifier
                            .height(13.dp)
                            .width(2.dp)
                    )
                    // Check if the timestamp is not empty before formatting
                    Spacer(modifier = Modifier.padding(start = 7.dp))
                    if (post.timestamp.isNotEmpty()) {
                        Text(fontSize = 13.sp, text = formatTimestamp(post.timestamp))
                    }
                    Spacer(modifier = Modifier.padding(start = 7.dp))
                    Divider(
                        modifier = Modifier
                            .height(13.dp)
                            .width(2.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 7.dp))
                    Text(fontSize = 13.sp, text = "조회: ${post.hits}")
                    // Display the number of comments
                    Spacer(modifier = Modifier.padding(start = 7.dp))
                    Divider(
                        modifier = Modifier
                            .height(13.dp)
                            .width(2.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 7.dp))
                    Text(fontSize = 13.sp, text = "댓글: ${post.comments.size}")
                }

            }

        }
    }
}

@Composable
fun formatTimestamp(timestamp: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val postDate = dateFormat.parse(timestamp)
    val currentDate = Date()

    val diffInMillis = currentDate.time - postDate.time
    val diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
    val diffInMinutes = TimeUnit.SECONDS.toMinutes(diffInSeconds)
    val diffInHours = TimeUnit.MINUTES.toHours(diffInMinutes)
    val diffInDays = TimeUnit.HOURS.toDays(diffInHours)

    return when {
        diffInMinutes < 1 -> "방금 전"
        diffInMinutes < 60 -> "$diffInMinutes 분 전"
        diffInHours < 24 -> "$diffInHours 시간 전"
        diffInDays < 7 -> "$diffInDays 일 전"
        else -> SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault()).format(postDate)
    }
}

