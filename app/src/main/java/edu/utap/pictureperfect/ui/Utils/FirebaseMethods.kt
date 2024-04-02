package edu.utap.pictureperfect.ui.Utils

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import edu.utap.pictureperfect.ui.Models.User
import edu.utap.pictureperfect.ui.Models.UserAccountSettings

class FirebaseMethods {
    private val TAG = "FirebaseMethods"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

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
}
