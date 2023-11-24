package com.example.stray.database

import android.util.Log
import com.example.stray.data.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class FirebaseDataManager {

    val db = FirebaseDatabase.getInstance("파이어베이스 데이터베이스 링크")
    val postsRef = db.getReference("posts")
    fun getPost(postId: String): Task<DataSnapshot> {
        // Assuming postsRef is your DatabaseReference to the "posts" node
        return postsRef.child(postId).get()
    }

    fun createPost(post: Post) {
        // Initialize hits to 0 when creating a new post
        post.hits = 0
        val postId = postsRef.push().key
        postId?.let {
            post.postId = it
            postsRef.child(it).setValue(post).addOnCompleteListener {}
        }
    }

    fun updatePost(post: Post) {
        // Fetch the existing post to preserve hits count
        getPost(post.postId).addOnSuccessListener { snapshot ->
            val existingPost = snapshot.getValue(Post::class.java)
            existingPost?.let {
                // Preserve the original hits count
                post.hits = it.hits
                postsRef.child(post.postId).setValue(post)
            }
        }
    }

    fun deletePost(postId: String) {
        // Firebase에서 게시글을 삭제하는 로직
        postsRef.child(postId)
            .removeValue()
    }

    // Inside com.example.getiproject.database.FirebaseDataManager class
    fun updatePostHits(postId: String, updatedHits: Int) {
        postsRef.child(postId).child("hits").setValue(updatedHits)
    }

}
