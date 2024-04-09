package edu.utap.pictureperfect.ui.Utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Models.User

class UserSearchAdapter(context: Context, private val resource: Int, private val userList: List<User>) :
    ArrayAdapter<User>(context, resource, userList) {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val imageLoader: ImageLoader = ImageLoader.getInstance()
    private lateinit var firebaseMethods: FirebaseMethods
    private lateinit var auth: FirebaseAuth

    private class ViewHolder {
        lateinit var profileImage: ImageView
        lateinit var username: TextView
        lateinit var followBtn: Button
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        auth = Firebase.auth
        firebaseMethods = FirebaseMethods()

        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false)
            holder = ViewHolder()

            holder.profileImage = convertView.findViewById(R.id.search_profile_image)
            holder.username = convertView.findViewById(R.id.search_username)
            holder.followBtn = convertView.findViewById(R.id.add_button)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val user = getItem(position)

        // Set the profile image (Assuming you have the URL stored in the User object)
        // imageLoader.displayImage(user?.profileImageUrl, holder.profileImage)

        // Set display name
        holder.username.text = user?.username

        var isFollowed = false

        val followedUserId = user?.user_id
        val currentUserId = auth.currentUser?.uid.toString()


        if (followedUserId != null) {
            firebaseMethods.checkIfFollowing(currentUserId, followedUserId) { isFollowing ->
                if (isFollowing) {
                    // Followed user exists in the current user's followedUsers list
                    // Update UI for following state
                    isFollowed = true
                    holder.followBtn.text = "Unfollow" // Change button text to "Unfollow"

                    // Here you can add any additional logic you need for following state
                } else {
                    // Followed user does not exist in the current user's followedUsers list
                    // Update UI for not following state
                    isFollowed = false
                    holder.followBtn.text = "Follow" // Change button text to "Follow"

                    // Here you can add any additional logic you need for not following state
                }
            }
        }

        holder.followBtn.setOnClickListener {
            val followedUserId = user?.user_id
            val currentUserId = auth.currentUser?.uid.toString()

            if (followedUserId != null) {
                if (isFollowed) {
                    // If user is followed, unfollow
    //                    firebaseMethods.unfollowUser(currentUserId, followedUserId)
                    firebaseMethods.unfollowUser(currentUserId, followedUserId)
                    firebaseMethods.removeFollowingUser(currentUserId, followedUserId)
                    firebaseMethods.decreaseFollowing(currentUserId)
                    firebaseMethods.decreaseFollowers(followedUserId)
                    isFollowed = false
                    holder.followBtn.text = "Follow" // Change button text to "Follow"
                } else {
                    // If user is not followed, follow
                    firebaseMethods.followersUser(currentUserId, followedUserId)
                    firebaseMethods.followingUsers(currentUserId, followedUserId)
                    firebaseMethods.increaseFollowing(currentUserId)
                    firebaseMethods.increaseFollowers(followedUserId)
                    isFollowed = true
                    holder.followBtn.text = "Unfollow" // Change button text to "Unfollow"
                }
            }
        }

        return convertView!!
    }
}
