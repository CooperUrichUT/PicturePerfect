package edu.utap.pictureperfect.ui.Utils

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.MainActivity
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Models.Photo



class ViewPost : AppCompatActivity() {

    private val TAG = "ViewPost"
    private lateinit var userName: TextView
    private lateinit var caption: TextView
    private lateinit var datePosted: TextView
    private lateinit var imagePosted: ImageView
    private lateinit var profilePhoto: ImageView
    private lateinit var cancelButton: TextView
    private lateinit var photo: Photo
    private lateinit var imageLoader: UniversalImageLoader
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private var userId = ""
    private var imageUrl = ""
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_view_post)
        auth = Firebase.auth
        userName = findViewById(R.id.username)
        caption = findViewById(R.id.image_caption)
        datePosted = findViewById(R.id.image_time_posted)
        imagePosted = findViewById(R.id.post_image)
        profilePhoto = findViewById(R.id.profile_photo)
        cancelButton = findViewById(R.id.cancel)
        imageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(imageLoader.getConfig())

        // Get the imageUrl and userId passed to this activity
        imageUrl = intent.getStringExtra("IMAGE_URL").toString()
        userId = intent.getStringExtra("USER_ID").toString()

        Log.d(TAG, "ImageURL: $imageUrl and UserID: $userId")

        // Query the Firebase Realtime Database to fetch the corresponding Photo object
        val userPhotosRef = userId?.let {
            FirebaseDatabase.getInstance().reference
                .child("user_photos")
                .child(it)
        }

        // Attach a ValueEventListener to fetch the Photo object
        if (userPhotosRef != null) {
            userPhotosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Iterate through each photo under the current user's node
                    for (photoSnapshot in dataSnapshot.children) {
                        // Get the photo data (Photo object) from the snapshot
                        photo = photoSnapshot.getValue(Photo::class.java)!!

                        // If photo is null or its image path doesn't match imageUrl, continue to the next iteration
                        if (photo == null || photo.image_path != imageUrl) continue

                        // Now you have the Photo object associated with the imageUrl
                        // You can use this photo object as needed
                        Log.d(TAG, "Found matching photo: $photo")
                        Log.d(TAG, "Photo information: ${photo.caption}")
                        setWidgets()

                        // Break out of the loop since we found the matching photo
                        break
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error fetching user photos: ${databaseError.message}")
                }
            })
        }

        cancelButton.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close LoginActivity
        }
    }

    fun setWidgets() {

        caption.text = photo.caption
        firebaseMethods.getUserData(auth.currentUser?.uid.toString()) { retrievedUser ->
            if (retrievedUser != null) {
                // Handle the retrieved user data here
                Log.d(TAG, "Username is ${retrievedUser.username}")
                userName.text = retrievedUser.username
                // For example, update UI or perform any other operations with the user data
            } else {
                // Handle the case where the user data is null or an error occurred
                Log.e(TAG, "Failed to retrieve user data")
            }
        }

        imageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(imageLoader.getConfig())
        ImageLoader.getInstance().displayImage(photo.image_path, imagePosted)
        setProfileImage()
    }

    private fun setProfileImage() {
        Log.d(TAG, "Setting the profile image")

        // Assuming you have a reference to your Firebase Database and the user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef =
            userId?.let {
                FirebaseDatabase.getInstance().reference.child("profile_pictures").child(
                    it
                ).child("image_path")
            }

        // Listen for changes to the profile picture URL in the database
        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageURL = dataSnapshot.getValue(String::class.java)
                imageURL?.let {
                    // Set the retrieved imageURL in the shared ViewModel
                    imageLoader.setImage(imageURL, profilePhoto, null, "")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching profile picture URL: ${databaseError.message}")
            }
        })
    }

}
