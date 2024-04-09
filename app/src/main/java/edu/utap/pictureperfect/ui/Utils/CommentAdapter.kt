package edu.utap.pictureperfect.ui.Utils

import edu.utap.pictureperfect.ui.Models.Comment
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.ui.Models.UserAccountSettings
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import edu.utap.pictureperfect.R

class CommentAdapter(context: Context, private val resource: Int, private val comments: List<Comment>) :
    ArrayAdapter<Comment>(context, resource, comments) {

    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mContext: Context = context
    private val TAG = "CommentAdapter"
    val imageLoader = ImageLoader.getInstance()

    private class ViewHolder {
        lateinit var comment: TextView
        lateinit var username: TextView
        lateinit var timestamp: TextView
        lateinit var profileImage: ImageView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false)
            holder = ViewHolder()

            holder.comment = convertView.findViewById(R.id.comment)
            holder.username = convertView.findViewById(R.id.comment_username)
            holder.timestamp = convertView.findViewById(R.id.comment_time_posted)
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val comment = getItem(position)

        // Set the comment text
        holder.comment.text = comment?.comment
        comment?.user_id?.let {
            firebaseMethods.getUserAccountSettingsData(it) { user ->
                holder.username.text  = user?.username
                setProfileImage(comment.user_id, holder.profileImage)

            }
        }

        // Set the timestamp difference
        val timestampDifference = comment?.let { getTimestampDifference(it) }
        holder.timestamp.text = if (timestampDifference != "0") "$timestampDifference d" else "today"

//        // Set the username and profile image
//        val userAccountSettings = comment?.userAccountSettings

//        val imageLoader = ImageLoader.getInstance()
//        imageLoader.displayImage(userAccountSettings?.profile_photo, holder.profileImage)

        return convertView!!
    }

    /**
     * Returns a string representing the number of days ago the post was made
     */
    private fun getTimestampDifference(comment: Comment): String {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.")

        val difference: String
        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA)
        sdf.timeZone = TimeZone.getTimeZone("Canada/Pacific") // google 'android list of timezones'
        val today = c.time
        sdf.format(today)
        val timestamp: Date
        val photoTimestamp = comment.date_created
        difference = try {
            timestamp = sdf.parse(photoTimestamp)
            ((today.time - timestamp.time) / 1000 / 60 / 60 / 24).toString()
        } catch (e: ParseException) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.message)
            "0"
        }
        return difference
    }

    private fun setProfileImage(userId: String?, profileImage: ImageView) {
        // Assuming you have a reference to your Firebase Database and the user's ID
        val userRef = userId?.let {
            FirebaseDatabase.getInstance().reference
                .child("profile_pictures")
                .child(it)
                .child("image_path")
        }

        // Listen for changes to the profile picture URL in the database
        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageURL = dataSnapshot.getValue(String::class.java)
                imageURL?.let {
                    // Load the profile image using Universal Image Loader
                    ImageLoader.getInstance().displayImage(imageURL, profileImage)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching profile picture URL: ${databaseError.message}")
            }
        })
    }
}
