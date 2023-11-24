package com.example.stray.screen


import com.example.stray.database.FirebaseDataManager
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stray.R
import com.example.stray.Screen
import com.example.stray.StrayImage
import com.example.stray.data.Post
import com.example.stray.data.SearchType
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfo(navController: NavController, onSignOutClicked: () -> Unit) {
    // Firebase Authentication 인스턴스 가져오기
    val auth = Firebase.auth
    // 현재 인증된 사용자 가져오기
    val user = auth.currentUser

    // 사용자 이름을 저장할 상태(State) 정의
    var userName by remember { mutableStateOf(user?.displayName ?: "") }
    var userEditState by remember {
        mutableStateOf(false)
    }
    // 사용자 UID를 가져오기
    val userUid = user?.uid

    // State to hold the list of user-specific posts
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val firebaseDataManager = FirebaseDataManager()

    var toggleState by remember {
        mutableStateOf(false)
    }

    // State to hold the total number of posts
    var totalPostsCount by remember { mutableStateOf(0) }

    // State to hold the number of posts where the author is "아데"
    var userPostsCount by remember { mutableStateOf(0) }

    fun fetchAndUpdatePostCounts() {
        firebaseDataManager.postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allPostsCount = snapshot.childrenCount.toInt()
                totalPostsCount = allPostsCount

                val userSpecificPosts = snapshot.children
                    .mapNotNull { it.getValue(Post::class.java) }
                    .filter { it.author == userName }

                userPostsCount = userSpecificPosts.size
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
                Log.e("CommunityHome", "Error fetching posts count: ${error.message}")
            }
        })
    }

    // Use Effect to fetch the user-specific posts when the composable is first created
    LaunchedEffect(userUid) {
        userUid?.let { uid ->
            fetchAndUpdatePostCounts()
            firebaseDataManager.postsRef.orderByChild("author").equalTo(uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val postList =
                            snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                        Log.d("UserInfo", "User Posts: $postList")
                        userPosts = postList
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if needed
                        Log.e("UserInfo", "Error fetching user posts: ${error.message}")
                    }
                })
        }
    }

    // 사용자 이름 수정 함수 정의
    fun updateUserName() {
        user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build()
        )
    }

    Image(
        painter = painterResource(id = R.drawable.vector4),
        contentDescription = "상단 배경",
        contentScale = ContentScale.FillBounds, // 또는 다른 ContentScale 값 사용
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        IconButton(onClick = { userEditState = !userEditState }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit" +
                        "편집",
                tint = Color.White
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StrayImage()
        Box(
            modifier = Modifier
                .paddingFromBaseline(top = 30.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center

        ) {
            Text(
                text = userName,
                fontSize = 30.sp,
                color = Color.White, // Set the text color to white
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier.padding(top = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "게시글 수: $userPostsCount")
            Spacer(modifier = Modifier.width(10.dp))
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(15.dp),
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "댓글 수: 0")
        }
//        Row(
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//                .padding(top = 60.dp)
//        ) {
//            ToggleOption(
//                option = "내가 작성한 글",
//                isSelected = toggleState,
//                onToggle = { toggleState = !toggleState },
//                modifier = Modifier.background(color = Color.White)
//            )
//        }
        Button(onClick = { toggleState = !toggleState }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black), border = BorderStroke(1.dp, color = Color.Black), modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 60.dp)) {
            Text(text = "내가 작성한 글", fontSize = 16.sp,fontWeight = FontWeight.Bold)
        }
        if(!toggleState){
            Spacer(modifier = Modifier.size(0.dp, 48.dp))
            Button(onClick = onSignOutClicked, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White), border = BorderStroke(1.dp, color = Color.Black), modifier = Modifier
                .align(Alignment.CenterHorizontally)) {
                Text(text = "로그아웃", fontSize = 16.sp,fontWeight = FontWeight.Bold)
            }
        }
        // State to hold the list of posts
        var postsState by remember { mutableStateOf<List<Post>>(emptyList()) }

        // State to track the number of posts to fetch
        var numberOfPostsToFetch by remember { mutableStateOf(10) }


        // State to track the selected search type
        var searchType by remember { mutableStateOf(SearchType.AUTHOR) }

//     Use Effect to fetch the initial posts when the composable is first created
        LaunchedEffect(userName, searchType) {
            firebaseDataManager.postsRef
                .limitToLast(numberOfPostsToFetch)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val postList =
                            snapshot.children.mapNotNull { it.getValue(Post::class.java) }

                        // Filter posts based on search criteria locally
                        postsState = postList.filter { post ->
                            when (searchType) {
                                SearchType.AUTHOR -> post.author.contains(
                                    userName,
                                    ignoreCase = true
                                )

                                else -> false // Exclude other search types
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if needed
                        Log.e("CommunityHome", "Error fetching posts: ${error.message}")
                    }
                })
        }
        if (toggleState) {
            Box {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                    ) {
                        LazyColumn {
                            items(postsState.filter {
                                when (searchType) {

                                    SearchType.AUTHOR -> it.author.contains(
                                        userName,
                                        ignoreCase = true
                                    )

                                    else -> {
                                        false
                                    }
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
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val postList =
                                                        snapshot.children.mapNotNull {
                                                            it.getValue(
                                                                Post::class.java
                                                            )
                                                        }
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

            }

        }
    }
    if (userEditState) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.vector4),
                contentDescription = "상단 배경",
                contentScale = ContentScale.FillBounds, // 또는 다른 ContentScale 값 사용
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            StrayImage()
            Column(modifier = Modifier.align(Alignment.Center)) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("사용자 이름") },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.Black, unfocusedLabelColor = Color.DarkGray)
                )
                Button(
                    onClick = {
                        updateUserName()
                        userEditState = !userEditState
                        fetchAndUpdatePostCounts()
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(8.dp)
                ) {
                    Text("사용자 이름 수정")
                }
            }
        }
    }
}

@Composable
fun ToggleOption(
    option: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(onClick = { onToggle() }) {
        Text(
            text = option,
            fontSize = 25.sp,
            fontWeight = if (isSelected) FontWeight.Bold else null)

    }
}