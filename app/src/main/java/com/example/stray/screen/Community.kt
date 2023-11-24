package com.example.stray.screen

import com.example.stray.database.FirebaseDataManager
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.example.stray.Screen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale
import coil.compose.rememberImagePainter
import com.example.stray.StrayImage
import com.example.stray.TopImage
import com.example.stray.data.Comment
import com.example.stray.data.Post
import com.google.firebase.auth.FirebaseAuth


// PostDetail 화면에서 댓글을 수정하고 등록할 수 있도록 수정
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun PostDetail(
    navController: NavController,
    postId: String,
    firebaseDataManager: FirebaseDataManager
) {
    var post by remember { mutableStateOf<Post?>(null) }
    var newComment by remember { mutableStateOf("") }

    DisposableEffect(postId) {
        val postReference = firebaseDataManager.postsRef.child(postId)

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val retrievedPost = snapshot.getValue(Post::class.java)
                post = retrievedPost
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
                Log.e("PostDetail", "Error fetching post details: ${error.message}")
            }
        }

        // Fetch the initial post details
        postReference.addValueEventListener(postListener)

        // Remove the listener when the composable is disposed
        onDispose {
            postReference.removeEventListener(postListener)
        }
    }
    TopImage(Modifier)
    post?.let { actualPost ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = actualPost.title,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            LazyColumn {
                item {
                    Text(text = actualPost.author)
                    Text(text = actualPost.timestamp)
                    // Display all

                    actualPost.imageUrls?.let { imageUrls ->
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp)
                        ) {
                            items(imageUrls) { imageUrl ->
                                ImageItem(imageUrl = imageUrl)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = actualPost.content)

                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                // Navigate to the EditPostScreen when the Edit button is clicked
                                navController.navigate(Screen.EditPostScreen.route + "/$postId")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3C9FF))
                        ) {
                            Text(text = "수정")
                        }

                    }

                    // Display comments
                    Text("댓글", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    CommentSection(comments = actualPost.comments)

                    // Comment input field
                    TextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        placeholder = { Text("댓글을 입력하세요") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val authorName = currentUser?.displayName ?: "Unknown User"
                    // Button to add a new comment
                    Button(
                        onClick = {
                            val updatedComments = actualPost.comments.toMutableList()
                            val timestamp = SimpleDateFormat(
                                "yyyy-MM-dd HH:mm",
                                Locale.getDefault()
                            ).format(System.currentTimeMillis())

                            updatedComments.add(
                                Comment(
                                    author = authorName, // 작성자를 설정하거나, 현재 사용자 정보를 사용
                                    content = newComment,
                                    timestamp = timestamp
                                )
                            )

                            // Update the post with the new comment
                            val updatedPost = Post(
                                postId = postId,
                                title = actualPost.title,
                                content = actualPost.content,
                                author = actualPost.author,
                                imageUrls = actualPost.imageUrls,
                                timestamp = actualPost.timestamp,
                                hits = actualPost.hits,
                                comments = updatedComments
                            )

                            // Use the com.example.getiproject.database.FirebaseDataManager to update the post
                            firebaseDataManager.updatePost(updatedPost)

                            // Clear the new comment field
                            newComment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3C9FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(text = "댓글 등록")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImageItem(imageUrl: Any) {
    val context = LocalContext.current

    val imageModifier = Modifier
        .width(200.dp)
        .height(200.dp)
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(4.dp))
        .clickable {

        }

    Image(
        painter = rememberImagePainter(data = imageUrl),
        contentDescription = null,
        modifier = imageModifier
    )
}

// 댓글 섹션을 표시하는 컴포저블 추가
@Composable
fun CommentSection(comments: List<Comment>) {
    Column(
    ) {
        comments.forEach { comment ->
            CommentItem(comment = comment)
        }
    }
}

// 댓글 항목을 표시하는 컴포저블 추가
@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Divider(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp))
        Text(text = comment.author, fontWeight = FontWeight.Bold)
        Text(text = comment.content, modifier = Modifier.padding(start = 3.dp))
        Text(text = comment.timestamp)
        Divider(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp))

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    navController: NavController,
    postId: String,
    firebaseDataManager: FirebaseDataManager
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var retrievedPost: Post? by remember { mutableStateOf(null) }

    // Fetch post details using postId and populate the fields
    LaunchedEffect(postId) {
        firebaseDataManager.getPost(postId).addOnSuccessListener { snapshot ->
            retrievedPost = snapshot.getValue(Post::class.java)
            if (retrievedPost != null) {
                title = retrievedPost!!.title
                content = retrievedPost!!.content
            }
        }.addOnFailureListener { e ->
            // Handle failure if needed
            Log.e("EditPostScreen", "Error fetching post details: ${e.message}")
        }
    }

    TopImage(Modifier)
    StrayImage()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 190.dp)
    ) {
        // Title input field
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFFF7F8F9),
                focusedBorderColor = Color.Transparent, // 포커스된 경우의 테두리 색상
                unfocusedBorderColor = Color.Transparent, // 포커스가 해제된 경우의 테두리 색상
            )
        )

        // Content input field
        TextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("내용") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(200.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFFF7F8F9),
                focusedBorderColor = Color.Transparent, // 포커스된 경우의 테두리 색상
                unfocusedBorderColor = Color.Transparent, // 포커스가 해제된 경우의 테두리 색상
            )
        )

        Row {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    // Update the existing post with the new title and content
                    val updatedPost = Post(
                        postId = postId,
                        title = title,
                        content = content,
                        author = retrievedPost?.author
                            ?: "", // Keep the original author or set a default value
                        imageUrls = retrievedPost?.imageUrls, // Keep the original imageUrl
                        timestamp = retrievedPost?.timestamp
                            ?: "" // Keep the original timestamp or set a default value
                    )
                    // Use the com.example.getiproject.database.FirebaseDataManager to update the post
                    firebaseDataManager.updatePost(updatedPost)

                    // Navigate back to the post detail screen after updating
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3C9FF)), modifier = Modifier.padding(10.dp)
            ) {
                Text(text = "저장")
            }
            Button(
                onClick = {
                    // Delete the existing post
                    firebaseDataManager.deletePost(postId)

                    // Navigate back to the previous screen after deleting
                    navController.navigate(Screen.CommunityHome.route)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3C9FF)), modifier = Modifier.padding(10.dp)

            ) {
                Text(text = "삭제")
            }
        }
    }
}

