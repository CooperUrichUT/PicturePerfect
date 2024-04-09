package edu.utap.pictureperfect.ui.Utils

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Models.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

//import edu.utap.pictureperfect.ui.Utils.CommentAdapter
//import edu.utap.pictureperfect.ui.adapters.CommentAdapter
//import kotlinx.android.synthetic.main.activity_view_comments.*

class ViewPostComments : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var imageUrl: String
    private lateinit var photoUserId: String
    private lateinit var location: String
    private lateinit var photoId: String
    private lateinit var commentEditText: EditText
    private lateinit var postCommentButton: ImageView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var listView: ListView
    private var currentUser: String = ""
    private val TAG = "ViewPostComments"
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: MutableList<Comment>
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_comments)

        btnBack = findViewById(R.id.backArrow)
        commentEditText = findViewById(R.id.comment)
        postCommentButton = findViewById(R.id.ivPostComment)
        listView = findViewById(R.id.listView)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser?.uid.toString()
        commentList = mutableListOf()

        // Set up the ListView adapter
        commentList = mutableListOf()
        commentAdapter = CommentAdapter(this, R.layout.layout_comment, commentList)
        listView.adapter = commentAdapter

        imageUrl = intent.getStringExtra("IMAGE_URL").toString()
        photoUserId = intent.getStringExtra("PHOTO_USER_ID").toString()
        location = intent.getStringExtra("LOCATION").toString()
        photoId = intent.getStringExtra("PHOTO_ID").toString()

        btnBack.setOnClickListener {
            finish()
        }

        // Set click listener for posting comment
        postCommentButton.setOnClickListener {
            addCommentToDatabase()
        }

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
                    .child(photoId)
                    .child("comments")
                    .push()
                    .setValue(comment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show()
                        commentEditText.text.clear()
                        // After adding the comment, fetch comments again to refresh the list
                        fetchCommentsFromDatabase()

                        firebaseMethods.addNotificationToUser(userId, photoUserId, "comment")

                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding comment: ${e.message}")
                    }
            }

            currentUserId?.let { userId ->
                database.reference
                    .child("photos")
                    .child(photoId)
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

        // Fetch comments for the specific photo under the "photos" node
        val commentsRef = database.reference
            .child("photos")
            .child(photoId)
            .child("comments") // Get the reference to the "comments" node

        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                commentList.clear()
                for (snapshot in dataSnapshot.children) {
                    // Get the key of each comment node
                    val commentKey = snapshot.key
                    // Get the comment data from the child node under the comment key
                    val commentData = snapshot.getValue(Comment::class.java)
                    commentData?.let {
                        // Assuming your Comment class has a property for the comment text,
                        // you can access it using the appropriate getter method
                        val commentText = commentData.comment
                        Log.d(TAG, "Comment: ${commentText}")
                        // Create a new Comment object with the key and comment text
                        val comment = Comment(commentText, auth.currentUser?.uid.toString(), getTimeStamp())
                        commentList.add(comment)
                    }
                }
                // Notify the adapter that the dataset has changed
                commentAdapter.notifyDataSetChanged()
                for (comment in commentList) {
                    Log.d(TAG, "Comment: ${comment.comment}, User ID: ${comment.user_id}, Date: ${comment.date_created}")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching comments: ${databaseError.message}")
            }
        })
    }

    fun getTimeStamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("US/Central")
        return sdf.format(Date())

    }


}
