package edu.utap.pictureperfect.ui.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Models.Photo
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(context: Context, private val resource: Int, private val photos: List<Photo>) :
    ArrayAdapter<Photo>(context, resource, photos) {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private val imageLoader: ImageLoader = ImageLoader.getInstance()
    val t = object : GenericTypeIndicator<ArrayList<String>>() {}
    private lateinit var auth: FirebaseAuth
    private val TAG = "PostAdapater"
    private lateinit var currentPhoto: Photo
    private lateinit var holder: ViewHolder


    private class ViewHolder(view: View) {
        lateinit var username: TextView
        lateinit var postImage: ImageView
        lateinit var profileImage: ImageView
        lateinit var imageLikes: TextView
        lateinit var imageCaption: TextView
        lateinit var imageTimePosted: TextView
        lateinit var imageHeart: ImageView
        lateinit var speechBubble: ImageView
        lateinit var imageCommentsLink: TextView
        lateinit var cancelButton: TextView

        init {
            username = view.findViewById(R.id.username)
            postImage = view.findViewById(R.id.post_image)
            profileImage = view.findViewById(R.id.profile_photo)
            imageLikes = view.findViewById(R.id.image_likes)
            imageCaption = view.findViewById(R.id.image_caption)
            imageTimePosted = view.findViewById(R.id.image_time_posted)
            imageHeart = view.findViewById(R.id.image_heart)
            speechBubble = view.findViewById(R.id.speech_bubble)
            imageCommentsLink = view.findViewById(R.id.image_comments_link)
            cancelButton = view.findViewById(R.id.cancel)
        }
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        auth = Firebase.auth

        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false)
            holder = ViewHolder(convertView)
            convertView.tag = holder

            holder.username = convertView.findViewById(R.id.username)
            holder.postImage = convertView.findViewById(R.id.post_image)
            holder.profileImage = convertView.findViewById(R.id.profile_photo)
            holder.imageLikes = convertView.findViewById(R.id.image_likes)
            holder.imageCaption = convertView.findViewById(R.id.image_caption)
            holder.imageTimePosted = convertView.findViewById(R.id.image_time_posted)
            holder.imageHeart = convertView.findViewById(R.id.image_heart)
            holder.speechBubble = convertView.findViewById(R.id.speech_bubble)
            holder.imageCommentsLink = convertView.findViewById(R.id.image_comments_link)
            holder.cancelButton = convertView.findViewById(R.id.cancel)


            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val photo = getItem(position)
        if (photo != null) {
            currentPhoto = photo
        }
        setupWidgets(holder, photo)


        holder.imageHeart.setOnClickListener {
            // Handle like button click
            // You can implement your logic here
            val redHeartDrawableId = R.drawable.ic_heart_red
            val whiteHeartDrawableId = R.drawable.ic_heart_white

            // Check the current drawable resource ID of the heart image
            val currentDrawableId = if (holder.imageHeart.tag == redHeartDrawableId) {
                whiteHeartDrawableId
            } else {
                redHeartDrawableId
            }

            // Set the new drawable resource for the heart image
            holder.imageHeart.setImageResource(currentDrawableId)
            holder.imageHeart.tag = currentDrawableId // Store the current drawable resource ID for future comparisons

            // Check if the heart was red (liked) before toggling
            if (currentDrawableId == redHeartDrawableId) {
                // If the heart was red, user is unliking the photo
                incrementLikesInDatabase()
//                // Remove the current user from the liked_users list
                addUserToLikedList()
                if (photo != null) {
                    photo.liked_users.add(auth.currentUser?.uid.toString())
                    holder.imageLikes.text = "${photo.liked_users.size} Likes"
                }
            } else {
                // If the heart was white, user is liking the photo
                decrementLikesInDatabase()
//                // Add the current user to the liked_users list
                removeUserFromLikedList()
                if (photo != null) {
                    photo.liked_users.remove(auth.currentUser?.uid.toString())
                    holder.imageLikes.text = "${photo.liked_users.size} Likes"
                }
            }
        }

        holder.speechBubble.setOnClickListener {
            val intent = Intent(it.context, ViewPostComments::class.java)
            val location = "Profile"

            // Put the clicked image URL and the user ID into the intent as extras
            intent.putExtra("IMAGE_URL", currentPhoto.image_path)
            intent.putExtra("PHOTO_USER_ID", currentPhoto.user_id)
            intent.putExtra("LOCATION", location)
            intent.putExtra("PHOTO_ID", currentPhoto.photo_id)

            // Start the ViewPostComments activity with the intent
            it.context.startActivity(intent)
        }


        holder.imageCommentsLink.setOnClickListener {
            // Handle comments link click
            // You can implement your logic here
            val intent = Intent(it.context, ViewPostComments::class.java)
            val location = "Profile"

            // Put the clicked image URL and the user ID into the intent as extras
            intent.putExtra("IMAGE_URL", currentPhoto.image_path)
            intent.putExtra("PHOTO_USER_ID", currentPhoto.user_id)
            intent.putExtra("LOCATION", location)
            intent.putExtra("PHOTO_ID", currentPhoto.photo_id)

            // Start the ViewPostComments activity with the intent
            it.context.startActivity(intent)
        }


        return convertView!!
    }

    // Function to set up all widgets
    @SuppressLint("SetTextI18n")
    private fun setupWidgets(holder: ViewHolder, photo: Photo?) {


//        holder.username.text = photo?.user_id // You might want to display the username instead of user_id
        holder.cancelButton.visibility = View.INVISIBLE
        holder.imageLikes.text = "${photo?.likes} likes"
        holder.imageCaption.text = photo?.caption
        if (photo != null) {
            setProfileImage(holder.profileImage, photo.user_id)
        }

        photo?.user_id?.let {
            firebaseMethods.getUserAccountSettingsData(it) { user ->
                holder.username.text = user?.username
            }
        }

        val currentUserLiked = photo?.liked_users?.contains(auth.currentUser?.uid)

        // Set the heart drawable based on whether the current user liked the photo
        val heartDrawableId = if (currentUserLiked == true) {
            R.drawable.ic_heart_red
        } else {
            R.drawable.ic_heart_white
        }

        // Set the heart drawable and tag
        holder.imageHeart.setImageResource(heartDrawableId)
        holder.imageHeart.tag = heartDrawableId


        // Format timestamp and display how long ago the post was posted
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        val postTime = photo?.date_created?.let { timeFormat.parse(it) }

        val diff = currentTime.time - (postTime?.time ?: 0)
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        val timePostedText = when {
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            else -> "$minutes minutes ago"
        }
        holder.imageTimePosted.text = timePostedText

        // Load post image using Universal Image Loader
        imageLoader.displayImage(photo?.image_path, holder.postImage)
    }

    private fun setProfileImage(profilePhoto: ImageView, userId: String) {
        Log.d(TAG, "Setting the profile image")

        // Assuming you have a reference to your Firebase Database and the user's ID
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
                    imageLoader.displayImage(imageURL, profilePhoto)
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
            .child(currentPhoto.user_id) // Adjust this path to match your database structure

        // Fetch the current likes count
        photoRef.child(currentPhoto.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes + 1
                // Update the likes count in the database
                photoRef.child(currentPhoto.photo_id).child("likes").setValue(newLikes)
//                fetchLikesCountFromDatabase()
                firebaseMethods.addNotificationToUser(auth.currentUser?.uid.toString(), currentPhoto.user_id, "like")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })

        userPhotoRef.child(currentPhoto.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes + 1
                // Update the likes count in the database
                userPhotoRef.child(currentPhoto.photo_id).child("likes").setValue(newLikes)
//                fetchLikesCountFromDatabase()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })
    }

    private fun decrementLikesInDatabase() {
        // Assuming you have a reference to the photo node in the database
        val photoRef = FirebaseDatabase.getInstance().reference
            .child("photos")

        val userPhotoRef = FirebaseDatabase.getInstance().reference
            .child("user_photos")
            .child(currentPhoto.user_id) // Adjust this path to match your database structure

        // Fetch the current likes count
        photoRef.child(currentPhoto.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes - 1
                // Update the likes count in the database
                photoRef.child(currentPhoto.photo_id).child("likes").setValue(newLikes)
//                fetchLikesCountFromDatabase()
                firebaseMethods.addNotificationToUser(auth.currentUser?.uid.toString(), currentPhoto.user_id, "like")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })

        userPhotoRef.child(currentPhoto.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentLikes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Increment the likes count by 1
                val newLikes = currentLikes - 1
                // Update the likes count in the database
                userPhotoRef.child(currentPhoto.photo_id).child("likes").setValue(newLikes)
//                fetchLikesCountFromDatabase()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching likes count: ${databaseError.message}")
            }
        })
    }

    private fun fetchLikesCountFromDatabase() {
        // Assuming you have a reference to the photo node in the database
        val photoRef = FirebaseDatabase.getInstance().reference
            .child("user_photos")
            .child(currentPhoto.user_id) // Adjust this path to match your database structure

        // Fetch the likes count from the database
        photoRef.child(currentPhoto.photo_id).child("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val likes = dataSnapshot.getValue(Long::class.java) ?: 0
                // Update the likes text view with the fetched count
                holder.imageLikes.text = "$likes Likes"
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
                .child(currentPhoto.user_id) // Adjust this path to match your database structure
                .child(currentPhoto.photo_id)

            val photoRef = FirebaseDatabase.getInstance().reference
                .child("photos")
                .child(currentPhoto.photo_id)
            // Adjust this path to match your database structure

            // Run transaction to update liked users list for the photo node
            photoRef.child("liked_users").runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    // Get the current liked users list
                    val t = object : GenericTypeIndicator<ArrayList<String>>() {}
                    var likedUsers = mutableData.getValue(t)

                    Log.d(TAG, "the current user is $currentUserId and the photo id is ${currentPhoto.user_id}")

                    // Initialize likedUsers as an empty ArrayList if it's null
                    if (likedUsers == null) {
                        likedUsers = ArrayList()
                    }

                    // Add the current user's ID to the list if it's not already present
                    if (!likedUsers.contains(FirebaseAuth.getInstance().currentUser?.uid)) {
                        FirebaseAuth.getInstance().currentUser?.uid?.let { likedUsers.add(it) }
                    }

                    // Set the updated liked users list in the mutableData
                    mutableData.value = likedUsers

                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                    if (committed) {
                        // Liked users list updated successfully for the photo node
                        // Now you can update the likes count in the UI
//                        fetchLikesCountFromDatabase()
                    } else {
                        // Transaction failed for the photo node
                        Log.e(TAG, "Transaction failed for photo node: ${databaseError?.message}")
                    }
                }
            })

            userPhotoRef.child("liked_users").runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    // Get the current liked users list
                    val t = object : GenericTypeIndicator<ArrayList<String>>() {}
                    var removeLikedUsers = mutableData.getValue(t)

                    Log.d(TAG, "the current user is $currentUserId and the photo id is ${currentPhoto.user_id}")

                    // Initialize likedUsers as an empty ArrayList if it's null
                    if (removeLikedUsers == null) {
                        removeLikedUsers = ArrayList()
                    }

                    // Add the current user's ID to the list if it's not already present
                    if (!removeLikedUsers.contains(FirebaseAuth.getInstance().currentUser?.uid)) {
                        FirebaseAuth.getInstance().currentUser?.uid?.let { removeLikedUsers.add(it) }
                    }

                    // Set the updated liked users list in the mutableData
                    mutableData.value = removeLikedUsers

                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                    if (committed) {
                        // Liked users list updated successfully for the photo node
                        // Now you can update the likes count in the UI
//                        fetchLikesCountFromDatabase()
                    } else {
                        // Transaction failed for the photo node
                        Log.e(TAG, "Transaction failed for photo node: ${databaseError?.message}")
                    }
                }
            })
        }
    }


    private fun removeUserFromLikedList() {
        // Get a reference to the database
        val database = FirebaseDatabase.getInstance()

        // Get a reference to the photo node in the database
        val userPhotoRef = database.getReference("user_photos/${currentPhoto.user_id}/${currentPhoto.photo_id}/liked_users")
        val photoRef = database.getReference("photos/${currentPhoto.photo_id}/liked_users")

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
//                            fetchLikesCountFromDatabase()
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
//                            fetchLikesCountFromDatabase()
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



