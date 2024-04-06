package edu.utap.pictureperfect.ui.Utils

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Models.Comment
//import edu.utap.pictureperfect.ui.Utils.CommentAdapter
//import edu.utap.pictureperfect.ui.adapters.CommentAdapter
//import kotlinx.android.synthetic.main.activity_view_comments.*

class ViewPostComments : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var imageUrl: String
    private lateinit var photoUserId: String
    private lateinit var location: String
    private lateinit var commentEditText: EditText
    private lateinit var postCommentButton: ImageView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var currentUser: String = ""
    private val TAG = "ViewPostComments"
//    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: MutableList<Comment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_comments)

        btnBack = findViewById(R.id.backArrow)
        commentEditText = findViewById(R.id.comment)
        postCommentButton = findViewById(R.id.ivPostComment)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser?.uid.toString()
        commentList = mutableListOf()

        // Set up the ListView adapter
//        commentAdapter = CommentAdapter(this, commentList)
//        listView.adapter = commentAdapter

        imageUrl = intent.getStringExtra("IMAGE_URL").toString()
        photoUserId = intent.getStringExtra("PHOTO_USER_ID").toString()
        location = intent.getStringExtra("LOCATION").toString()

        btnBack.setOnClickListener {
            finish()
        }

        // Set click listener for posting comment
        postCommentButton.setOnClickListener {
            addCommentToDatabase()
        }

        // Fetch comments from the database
        fetchCommentsFromDatabase()
    }

    private fun addCommentToDatabase() {
        val commentText = commentEditText.text.toString().trim()

        if (commentText.isNotEmpty()) {
            val currentUserId = auth.currentUser?.uid

            val comment = Comment(
                comment = commentText,
                user_id = currentUserId,
                date_created = System.currentTimeMillis().toString()
            )

            currentUserId?.let { userId ->
                database.reference
                    .child("user_photos")
                    .child(userId)
                    .child("comments")
                    .push()
                    .setValue(comment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show()
                        commentEditText.text.clear()
                        // After adding the comment, fetch comments again to refresh the list
                        fetchCommentsFromDatabase()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding comment: ${e.message}")
                    }
            }
        } else {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchCommentsFromDatabase() {
        val currentUserId = auth.currentUser?.uid

        currentUserId?.let { userId ->
            val commentsRef = database.reference
                .child("user_photos")
                .child(userId)
                .child("comments")

            commentsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    commentList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let {
                            commentList.add(comment)
                        }
                    }
                    // Notify the adapter that the dataset has changed
//                    commentAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error fetching comments: ${databaseError.message}")
                }
            })
        }
    }
}
