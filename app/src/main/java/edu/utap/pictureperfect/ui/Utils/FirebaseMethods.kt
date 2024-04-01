package edu.utap.pictureperfect.ui.Utils

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import edu.utap.pictureperfect.ui.Models.User
import edu.utap.pictureperfect.ui.Models.UserAccountSettings

class FirebaseMethods {
    private val TAG = "FirebaseMethods"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase =  Firebase.database
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val context = FirebaseApp.getInstance().applicationContext

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
//        val user = User(userId, 1, email, username)
//        val userAccountSettings = UserAccountSettings(user_bio, fullName, 0, 0, 0, "none", username, userId)

//        val userRef = databaseReference.child("users").child(userId.toString())

        // Set the user data under the generated key
        val userRefUsers = databaseReference.child("users").child(userId.toString())

        // Create a map to store the user's data
        val userData = HashMap<String, Any>()
        userData["email"] = email
        userData["username"] = username
        userData["user_id"] = userId.toString()


        // Set the user's data under the newly generated child node
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

                val email = dataSnapshot.child("email").getValue(String::class.java)
                user.email = email
                val user_id = dataSnapshot.child("user_id").getValue(String::class.java)
                user.user_id = user_id
                val username = dataSnapshot.child("username").getValue(String::class.java)
                user.username = username

                // Now you have email, user_id, and username
                Log.d(TAG, "Email: $email")
                Log.d(TAG, "user_id: $user_id")
                Log.d(TAG, "Username: $username")

                // Pass the user object to the callback function
                onComplete(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error retrieving user account settings: ${databaseError.message}")
                // Pass null to the callback function in case of an error
                onComplete(null)
            }
        })
    }

    fun getUserSettingsData(userId: String, onComplete: (UserAccountSettings?) -> Unit){
        val userAccountSettingsRef = databaseReference.child("user_account_settings").child(userId)

        userAccountSettingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userAccountSettings = UserAccountSettings()

                val displayName = dataSnapshot.child("display_name").getValue(String::class.java)
                userAccountSettings.display_name = displayName

                val followers = dataSnapshot.child("followers").getValue(Long::class.java)
                if (followers != null) {
                    userAccountSettings.followers = followers
                }

                val following = dataSnapshot.child("following").getValue(Long::class.java)
                if (following != null) {
                    userAccountSettings.following = following
                }

                val posts = dataSnapshot.child("posts").getValue(Long::class.java)
                if (posts != null) {
                    userAccountSettings.posts = posts
                }


                Log.d(TAG, "Users followers: $followers")
                Log.d(TAG, "Users followers: $following")
                Log.d(TAG, "Users followers: $posts")

                val profilePicture = dataSnapshot.child("profile_picture").getValue(String::class.java)
                userAccountSettings.profile_photo = profilePicture

                val userBio = dataSnapshot.child("user_bio").getValue(String::class.java)
                userAccountSettings.user_bio = userBio

                val username = dataSnapshot.child("username").getValue(String::class.java)
                userAccountSettings.username = username

                // Pass the user object to the callback function
                onComplete(userAccountSettings)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error retrieving user account settings: ${databaseError.message}")
                // Pass null to the callback function in case of an error
                onComplete(null)
            }
        })
    }

    fun updateUsernameAndBio(userId: String, username: String, bio: String) {
        val userAccountSettingsRef = databaseReference.child("user_account_settings").child(userId)
        val userRed = databaseReference.child("users").child(userId)

        userAccountSettingsRef.child("username").setValue(username)
        userAccountSettingsRef.child("user_bio").setValue(bio)
        userRed.child("username").setValue(username)
    }

}
