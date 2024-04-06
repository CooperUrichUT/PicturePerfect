package edu.utap.pictureperfect.ui.Utils

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.utap.pictureperfect.ui.Models.Photo
import edu.utap.pictureperfect.ui.Models.User
import edu.utap.pictureperfect.ui.Models.UserAccountSettings
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FirebaseMethods {
    private val TAG = "FirebaseMethods"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference()
    private var FIREBASE_STORAGE_LOCATION = "photos/users/"

    fun checkIfUsernameExists(username: String, onComplete: (Boolean) -> Unit) {
        val query = databaseReference.child("users").orderByChild("username").equalTo(username)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var usernameExists = false
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null && user.username == username) {
                        usernameExists = true
                        break
                    }
                }
                onComplete(usernameExists)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "checkIfUsernameExists: Database error: ${databaseError.message}")
                onComplete(false)
            }
        })
    }

    fun addNewUser(email: String, fullName: String, username: String, user_bio: String, profile_photo: String) {
        val userId = auth.currentUser?.uid

        val userRefUsers = databaseReference.child("users").child(userId.toString())
        val userData = HashMap<String, Any>()
        userData["email"] = email
        userData["username"] = username
        userData["user_id"] = userId.toString()
        userRefUsers.setValue(userData)

        val userRefUserAccountSettings = databaseReference.child("user_account_settings").child(userId.toString())
        val userAccountSettingsData = HashMap<String, Any>()
        userAccountSettingsData["display_name"] = fullName
        userAccountSettingsData["followers"] = 0
        userAccountSettingsData["following"] = 0
        userAccountSettingsData["posts"] = 0
        userAccountSettingsData["profile_picture"] = "none"
        userAccountSettingsData["user_bio"] = user_bio
        userAccountSettingsData["username"] = username
        userRefUserAccountSettings.setValue(userAccountSettingsData)
    }

    fun getUserData(userId: String, onComplete: (User?) -> Unit) {
        val userRef = databaseReference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = User()
                user.email = dataSnapshot.child("email").getValue(String::class.java)
                user.user_id = dataSnapshot.child("user_id").getValue(String::class.java)
                user.username = dataSnapshot.child("username").getValue(String::class.java)
                onComplete(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error retrieving user data: ${databaseError.message}")
                onComplete(null)
            }
        })
    }

    fun getUserAccountSettingsData(userId: String, onComplete: (UserAccountSettings?) -> Unit) {
        val userAccountSettingsRef = databaseReference.child("user_account_settings").child(userId)
        userAccountSettingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userAccountSettings = UserAccountSettings()
                userAccountSettings.display_name = dataSnapshot.child("display_name").getValue(String::class.java)
                userAccountSettings.followers = dataSnapshot.child("followers").getValue(Long::class.java) ?: 0
                userAccountSettings.following = dataSnapshot.child("following").getValue(Long::class.java) ?: 0
                userAccountSettings.posts = dataSnapshot.child("posts").getValue(Long::class.java) ?: 0
                userAccountSettings.profile_photo = dataSnapshot.child("profile_picture").getValue(String::class.java)
                userAccountSettings.user_bio = dataSnapshot.child("user_bio").getValue(String::class.java)
                userAccountSettings.username = dataSnapshot.child("username").getValue(String::class.java)
                onComplete(userAccountSettings)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error retrieving user account settings: ${databaseError.message}")
                onComplete(null)
            }
        })
    }

    fun updateUsernameAndBio(userId: String, username: String, bio: String) {
        getUserAccountSettingsData(userId) { userAccountSettings ->
            userAccountSettings?.let {
                if (username.isNotEmpty() && username != userAccountSettings.username) {
                    databaseReference.child("user_account_settings").child(userId).child("username").setValue(username)
                    databaseReference.child("users").child(userId).child("username").setValue(username)
                }
                if (bio.isNotEmpty() && bio != userAccountSettings.user_bio) {
                    databaseReference.child("user_account_settings").child(userId).child("user_bio").setValue(bio)
                }
            }
        }
    }

    fun getImageCount(onComplete: (Long?) -> Unit) {
        val userId = auth.currentUser?.uid.toString()
        val userAccountSettingsRef = databaseReference.child("user_account_settings").child(userId)
        userAccountSettingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userAccountSettings = UserAccountSettings()
                userAccountSettings.posts = dataSnapshot.child("posts").getValue(Long::class.java) ?: 0
                onComplete(userAccountSettings.posts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error retrieving user account settings: ${databaseError.message}")
                onComplete(null)
            }
        })
    }

    fun uploadNewPhoto(photoType: String, caption: String, bitmap: Bitmap) {
        Log.d(TAG, "Attempting to upload photo")
        val uid = auth.currentUser?.uid ?: ""
        getImageCount { count ->
            count?.let { imageCount ->

                if (photoType == "NewPhoto") {
                    val newStorageReference = storageReference.child("$FIREBASE_STORAGE_LOCATION/$uid/photo$imageCount")
                    Log.d(TAG, "Attempting to upload New Photo")
                    // Upload the new photo logic here
                    // For example, you can convert the Bitmap to a byte array and upload it
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    // Upload data to Firebase Storage
                    newStorageReference.putBytes(data)
                        .addOnSuccessListener { taskSnapshot ->
                            // Handle successful upload
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                // Get the download URL of the uploaded image


                                val downloadUrl = uri.toString()
                                Log.d(TAG, "DownloadURL: $downloadUrl")

                                addPhotoToDatabase(caption, downloadUrl)
                                incrementPhotoCountForUserId(uid)
                                // Now you have the download URL, you can store it in Firebase Database
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle failed upload
                            Log.e(TAG, "Upload failed: $exception")
                        }
                } else if (photoType == "ProfilePicture") {
                    Log.d(TAG, "Attempting to upload New Profile Photo")
                    FIREBASE_STORAGE_LOCATION = "profile_pictures/users/"
                    val newStorageReference = storageReference.child("$FIREBASE_STORAGE_LOCATION/$uid/profile_photo")

                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    // Upload data to Firebase Storage
                    newStorageReference.putBytes(data)
                        .addOnSuccessListener { taskSnapshot ->
                            // Handle successful upload
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                // Get the download URL of the uploaded image


                                val downloadUrl = uri.toString()
                                Log.d(TAG, "DownloadURL: $downloadUrl")

                                addProfilePictureToDatabase(downloadUrl)
                                // Now you have the download URL, you can store it in Firebase Database
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle failed upload
                            Log.e(TAG, "Upload failed: $exception")
                        }

                        // Upload the new profile photo logic here
                    } else {
                        Log.d(TAG, "Function not found")
                    }
                } ?: run {
                    Log.e(TAG, "Failed to retrieve image count")
                    // Handle error scenario
                }
        }
    }

    fun incrementPhotoCountForUserId(uid: String) {
        val userId = auth.currentUser?.uid.toString()
        val userAccountSettingsRef = databaseReference.child("user_account_settings").child(userId)
        userAccountSettingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userAccountSettings = dataSnapshot.getValue(UserAccountSettings::class.java)
                userAccountSettings?.let {
                    val currentPosts = userAccountSettings.posts ?: 0
                    val newPosts = currentPosts + 1
                    userAccountSettings.posts = newPosts
                    // Update the posts count in Firebase
                    userAccountSettingsRef.child("posts").setValue(newPosts)
                        .addOnSuccessListener {
                            Log.d(TAG, "User's posts count incremented successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Failed to increment user's posts count: $exception")
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error retrieving user account settings: ${databaseError.message}")
            }
        })
    }


    fun addPhotoToDatabase(caption: String, downloadUrl: String) {
        val uid = auth.currentUser?.uid ?: ""
        val newPhotoKey = databaseReference.child("photos").push().key.toString() // Push a new child node under "photos"
        val photoData = Photo() // Create PhotoData object with caption and download URL
        photoData.image_path = downloadUrl
        photoData.caption = caption
        photoData.user_id = uid
        photoData.date_created = getTimeStamp() // do this
        photoData.tags = getTags(caption)
        photoData.photo_id = newPhotoKey
        photoData.comments = ArrayList()
        photoData.likes = 0
//        photoData.liked_users.add("init")
        databaseReference.child("user_photos").child(uid).child(newPhotoKey).setValue(photoData)
        databaseReference.child("photos").child(newPhotoKey).setValue(photoData)
    }

    fun addProfilePictureToDatabase(downloadUrl: String) {
        val uid = auth.currentUser?.uid ?: ""
        val newPhotoKey = databaseReference.child("profile_pictures").push().key.toString() // Push a new child node under "photos"
        val photoData = Photo() // Create PhotoData object with caption and download URL
        photoData.image_path = downloadUrl
        photoData.caption = ""
        photoData.user_id = uid
        photoData.date_created = getTimeStamp() // do this
        photoData.tags = ""
        photoData.photo_id = newPhotoKey
//        databaseReference.child("user_photos").child(uid).child(newPhotoKey).setValue(photoData)
        databaseReference.child("profile_pictures").child(uid).setValue(photoData)
    }

    fun getTimeStamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("US/Central")
        return sdf.format(Date())

    }

    fun getTags(string: String): String {
        if (string.indexOf("#") > 0) {
            val sb = StringBuilder()
            val charArray = string.toCharArray()
            var foundWord = false
            for (c in charArray) {
                if (c == '#') {
                    foundWord = true
                    sb.append(c)
                } else {
                    if (foundWord) {
                        sb.append(c)
                    }
                }
                if (c == ' ') {
                    foundWord = false
                }
            }
            val s = sb.toString().replace(" ", "").replace("#", ",#")
            return s.substring(1, s.length)
        }
        return string
    }

    fun getUsername(userId: String): String {

        val userRef = databaseReference.child("users").child(userId).child("username").toString()
        return userRef
    }




}
