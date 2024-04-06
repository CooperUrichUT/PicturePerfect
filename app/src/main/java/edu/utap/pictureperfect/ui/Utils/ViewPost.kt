package edu.utap.pictureperfect.ui.Utils

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.MainActivity
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Models.Photo

import java.text.SimpleDateFormat
import java.util.*



class ViewPost : AppCompatActivity() {

    private val TAG = "ViewPost"
    private lateinit var userName: TextView
    private lateinit var caption: TextView
    private lateinit var datePosted: TextView
    private lateinit var commentLink: TextView
    private lateinit var imagePosted: ImageView
    private lateinit var profilePhoto: ImageView
    private lateinit var imageHeart: ImageView
    private lateinit var btnComments: ImageView
    private lateinit var cancelButton: TextView
    private lateinit var imageLikes: TextView
    private lateinit var photo: Photo
    private lateinit var imageLoader: UniversalImageLoader
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private var userId = ""
    private var photoUserId = ""
    private var imageUrl = ""
    private var location = ""
    val t = object : GenericTypeIndicator<ArrayList<String>>() {}
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_view_post)
        auth = Firebase.auth
        userName = findViewById(R.id.username)
        caption = findViewById(R.id.image_caption)
        datePosted = findViewById(R.id.image_time_posted)
        imagePosted = findViewById(R.id.post_image)
        imageLikes = findViewById(R.id.image_likes)
        profilePhoto = findViewById(R.id.profile_photo)
        cancelButton = findViewById(R.id.cancel)
        imageHeart = findViewById(R.id.image_heart)
        btnComments = findViewById(R.id.speech_bubble)
        commentLink = findViewById(R.id.image_comments_link)

        imageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(imageLoader.getConfig())

        // Get the imageUrl and userId passed to this activity
        imageUrl = intent.getStringExtra("IMAGE_URL").toString()
        userId = intent.getStringExtra("USER_ID").toString()
        location = intent.getStringExtra("LOCATION").toString()

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

        cancelButton.setOnClickListener {

            if (location.equals("Profile")) {
                val navController = findNavController(R.id.mobile_navigation)
                // Navigate to the profile screen using the defined action ID
                navController.navigate(R.id.action_navigation_viewPost_to_profile_settings_fragment)
            }
        }

        imageHeart.setOnClickListener {
            // Get the resource IDs of the red and white heart drawables
            val redHeartDrawableId = R.drawable.ic_heart_red
            val whiteHeartDrawableId = R.drawable.ic_heart_white

            // Check the current drawable resource ID of the heart image
            val currentDrawableId = if (imageHeart.tag == redHeartDrawableId) {
                whiteHeartDrawableId
            } else {
                redHeartDrawableId
            }

            // Set the new drawable resource for the heart image
            imageHeart.setImageResource(currentDrawableId)
            imageHeart.tag = currentDrawableId // Store the current drawable resource ID for future comparisons

            // Check if the heart was red (liked) before toggling
            if (currentDrawableId == redHeartDrawableId) {
                // If the heart was red, user is unliking the photo
                incrementLikesInDatabase()
                // Remove the current user from the liked_users list
                addUserToLikedList()
                photo.liked_users.remove(auth.currentUser?.uid.toString())
            } else {
                // If the heart was white, user is liking the photo
                decrementImageLikes()
                // Add the current user to the liked_users list
                removeUserFromLikedList()
                photo.liked_users.add(auth.currentUser?.uid.toString())
            }
        }

        btnComments.setOnClickListener {
            val intent = Intent(this, ViewPostComments::class.java)
            val location = "Profile"

            // Put the clicked image URL and the user ID into the intent as extras
            intent.putExtra("IMAGE_URL", imageUrl)
            intent.putExtra("PHOTO_USER_ID", photoUserId)
            intent.putExtra("LOCATION", location)

            // Start the ViewPost activity with the intent
            startActivity(intent)
        }

        commentLink.setOnClickListener {
            val intent = Intent(this, ViewPostComments::class.java)
            val location = "Profile"

            // Put the clicked image URL and the user ID into the intent as extras
            intent.putExtra("IMAGE_URL", imageUrl)
            intent.putExtra("PHOTO_USER_ID", photoUserId)
            intent.putExtra("LOCATION", location)

            // Start the ViewPost activity with the intent
            startActivity(intent)
        }


    }

    private fun fetchLikesCountFromDatabase() {
        // Assuming you have a reference to the photo node in the database
        val photoRef = FirebaseDatabase.getInstance().reference
            .child("user_photos")
            .child(userId) // Adjust this path to match your database structure

        // Fetch the likes count from the database
        photoRef.child(photo.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val likes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Update the likes text view with the fetched count
                imageLikes.text = "$likes Likes"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })
    }

    private fun setWidgets() {
        // Set caption, user ID, and fetch likes count
        caption.text = photo.caption
        photoUserId = photo.user_id
        fetchLikesCountFromDatabase()

        // Display how long ago the photo was posted
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        val photoTime = timeFormat.parse(photo.date_created)

        val diff = currentTime.time - photoTime.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        val timePostedText = when {
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            else -> "$minutes minutes ago"
        }
        datePosted.text = timePostedText

        // Check if the current user's ID is in the liked_users list
        val currentUserLiked = photo.liked_users.contains(auth.currentUser?.uid)

        // Set the heart drawable based on whether the current user liked the photo
        val heartDrawableId = if (currentUserLiked) {
            R.drawable.ic_heart_red
        } else {
            R.drawable.ic_heart_white
        }

        // Set the heart drawable and tag
        imageHeart.setImageResource(heartDrawableId)
        imageHeart.tag = heartDrawableId

        // Fetch user data and display username
        firebaseMethods.getUserData(auth.currentUser?.uid.toString()) { retrievedUser ->
            if (retrievedUser != null) {
                userName.text = retrievedUser.username
            } else {
                Log.e(TAG, "Failed to retrieve user data")
            }
        }

        // Load and display the image
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

    private fun incrementLikesInDatabase() {
        // Assuming you have a reference to the photo node in the database
        val photoRef = FirebaseDatabase.getInstance().reference
            .child("photos")

        val userPhotoRef = FirebaseDatabase.getInstance().reference
            .child("user_photos")
            .child(userId) // Adjust this path to match your database structure

        // Fetch the current likes count
        photoRef.child(photo.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes + 1
                // Update the likes count in the database
                photoRef.child(photo.photo_id).child("likes").setValue(newLikes)
                fetchLikesCountFromDatabase()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })

        userPhotoRef.child(photo.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes + 1
                // Update the likes count in the database
                userPhotoRef.child(photo.photo_id).child("likes").setValue(newLikes)
                fetchLikesCountFromDatabase()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })
    }

    private fun decrementImageLikes() {
        // Assuming you have a reference to the photo node in the database
        val photoRef = FirebaseDatabase.getInstance().reference
            .child("photos")


        val userPhotoRef = FirebaseDatabase.getInstance().reference
            .child("user_photos")
            .child(userId) // Adjust this path to match your database structure

        // Fetch the current likes count
        photoRef.child(photo.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes - 1
                // Update the likes count in the database
                photoRef.child(photo.photo_id).child("likes").setValue(newLikes)
                fetchLikesCountFromDatabase()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })

        userPhotoRef.child(photo.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes - 1
                // Update the likes count in the database
                userPhotoRef.child(photo.photo_id).child("likes").setValue(newLikes)
                fetchLikesCountFromDatabase()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })
    }

    private fun addUserToLikedList() {
        // Get the current user's ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        currentUserId?.let { userId ->
            // Assuming you have a reference to the photo node in the database
            val userPhotoRef = FirebaseDatabase.getInstance().reference
                .child("user_photos")
                .child(photoUserId) // Adjust this path to match your database structure

            val photoRef = FirebaseDatabase.getInstance().reference
                .child("photos")
                .child(photo.photo_id) // Adjust this path to match your database structure

            // Run transaction to update liked users list for the user node
            userPhotoRef.child(photo.photo_id).child("liked_users").runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    // Get the current liked users list
                    var likedUsers = mutableData.getValue(ArrayList::class.java) as? ArrayList<String>

                    // Initialize likedUsers as an empty ArrayList if it's null
                    if (likedUsers == null) {
                        likedUsers = ArrayList()
                    }

                    // Add the current user's ID to the list if it's not already present
                    if (!likedUsers.contains(userId)) {
                        likedUsers.add(userId)
                    }

                    // Set the updated liked users list in the mutableData
                    mutableData.value = likedUsers

                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                    if (committed) {
                        // Liked users list updated successfully for user node
                        // Now update liked users list for photo node
                        photoRef.child("liked_users").setValue(dataSnapshot?.value)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Liked users list updated successfully for photo node
                                    // Now you can update the likes count in the UI
                                    fetchLikesCountFromDatabase()
                                } else {
                                    Log.e(TAG, "Failed to update liked users list for photo node: ${task.exception?.message}")
                                }
                            }
                    } else {
                        // Transaction failed for user node
                        Log.e(TAG, "Transaction failed for user node: ${databaseError?.message}")
                    }
                }
            })
        }
    }




    private fun removeUserFromLikedList() {
        // Get a reference to the database
        val database = FirebaseDatabase.getInstance()

        // Get a reference to the photo node in the database
        val userPhotoRef = database.getReference("user_photos/$photoUserId/${photo.photo_id}/liked_users")
        val photoRef = database.getReference("photos/${photo.photo_id}/liked_users")

        // Remove the current user's ID from the liked list
        photoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val likedUsers = dataSnapshot.getValue(t)
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d(TAG, "Liked Users: $likedUsers")

                likedUsers?.remove(currentUserId)

                // Update the liked users list in the database
                photoRef.setValue(likedUsers)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Liked users list updated successfully
                            // Now you can update the likes count in the UI
                            fetchLikesCountFromDatabase()
                        } else {
                            Log.e(TAG, "Failed to update liked users list: ${task.exception?.message}")
                        }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error removing user from liked list: ${databaseError.message}")
            }
        })

        userPhotoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val likedUsers = dataSnapshot.getValue(t)
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d(TAG, "Liked Users: $likedUsers")

                likedUsers?.remove(currentUserId)

                // Update the liked users list in the database
                userPhotoRef.setValue(likedUsers)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Liked users list updated successfully
                            // Now you can update the likes count in the UI
                            fetchLikesCountFromDatabase()
                        } else {
                            Log.e(TAG, "Failed to update liked users list: ${task.exception?.message}")
                        }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error removing user from liked list: ${databaseError.message}")
            }
        })
    }
}
