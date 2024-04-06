//package edu.utap.pictureperfect.ui.Utils
//
//import edu.utap.pictureperfect.ui.Models.Comment
//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//import com.nostra13.universalimageloader.core.ImageLoader
//import java.text.ParseException
//import java.text.SimpleDateFormat
//import java.util.*
//
//class CommentAdapter(context: Context, private val resource: Int, private val comments: List<Comment>) :
//    ArrayAdapter<Comment>(context, resource, comments) {
//
//    private val mInflater: LayoutInflater = LayoutInflater.from(context)
//    private val mContext: Context = context
//    private val TAG = "CommentAdapter"
//
//    private lateinit var comment: TextView
//    private lateinit var username: TextView
//    private lateinit var timestamp: TextView
//    private lateinit var reply: TextView
//    private lateinit var like: ImageView
//    private lateinit var likes: TextView
//    private lateinit var profileImage: ImageView
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        var convertView = convertView
//        val holder: RecyclerView.ViewHolder
//
//        if (convertView == null) {
//            convertView = mInflater.inflate(resource, parent, false)
//            holder = RecyclerView.ViewHolder()
//
//            holder.comment = convertView!!.findViewById(R.id.comment)
//            holder.username = convertView.findViewById(R.id.comment_username)
//            holder.timestamp = convertView.findViewById(R.id.comment_time_posted)
//            holder.profileImage = convertView.findViewById(R.id.comment_profile_image)
//
//            convertView.tag = holder
//        } else {
//            holder = convertView.tag as ViewHolder
//        }
//
//        // Set the comment
//        holder.comment.text = getItem(position).comment
//
//        // Set the timestamp difference
//        val timestampDifference = getTimestampDifference(getItem(position))
//        holder.timestamp.text = if (timestampDifference != "0") "$timestampDifference d" else "today"
//
//        // Set the username and profile image
//        val reference = FirebaseDatabase.getInstance().reference
//        val query = reference
//            .child(mContext.getString(R.string.dbname_user_account_settings))
//            .orderByChild(mContext.getString(R.string.field_user_id))
//            .equalTo(getItem(position).user_id)
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (singleSnapshot in dataSnapshot.children) {
//                    holder.username.text = singleSnapshot.getValue(UserAccountSettings::class.java)?.username
//
//                    val imageLoader = ImageLoader.getInstance()
//
//                    imageLoader.displayImage(
//                        singleSnapshot.getValue(UserAccountSettings::class.java)?.profile_photo,
//                        holder.profileImage
//                    )
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.d(TAG, "onCancelled: query cancelled.")
//            }
//        })
//
//        try {
//            if (position == 0) {
//                holder.like.visibility = View.GONE
//                holder.likes.visibility = View.GONE
//                holder.reply.visibility = View.GONE
//            }
//        } catch (e: NullPointerException) {
//            Log.e(TAG, "getView: NullPointerException: " + e.message)
//        }
//
//        return convertView
//    }
//
//    /**
//     * Returns a string representing the number of days ago the post was made
//     */
//    private fun getTimestampDifference(comment: Comment): String {
//        Log.d(TAG, "getTimestampDifference: getting timestamp difference.")
//
//        val difference: String
//        val c = Calendar.getInstance()
//        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA)
//        sdf.timeZone = TimeZone.getTimeZone("Canada/Pacific") // google 'android list of timezones'
//        val today = c.time
//        sdf.format(today)
//        val timestamp: Date
//        val photoTimestamp = comment.date_created
//        difference = try {
//            timestamp = sdf.parse(photoTimestamp)
//            ((today.time - timestamp.time) / 1000 / 60 / 60 / 24).toString()
//        } catch (e: ParseException) {
//            Log.e(TAG, "getTimestampDifference: ParseException: " + e.message)
//            "0"
//        }
//        return difference
//    }
//}