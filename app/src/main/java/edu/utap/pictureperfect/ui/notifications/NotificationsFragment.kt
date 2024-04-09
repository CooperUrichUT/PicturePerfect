package edu.utap.pictureperfect.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.ActivityViewNotificationsBinding
import edu.utap.pictureperfect.databinding.FragmentNotificationsBinding
import edu.utap.pictureperfect.ui.Models.Comment
import edu.utap.pictureperfect.ui.Models.Notification
import edu.utap.pictureperfect.ui.Utils.NotificationAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationsList: MutableList<Notification>
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val TAG = "NotificationsFragment"
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()


        // Initialize notifications list and adapter
        notificationsList = mutableListOf()
        notificationAdapter = NotificationAdapter(requireContext(), R.layout.layout_notification, notificationsList)

        // Set the adapter to the ListView
        val listView: ListView = root.findViewById(R.id.notificationsListView)
        listView.adapter = notificationAdapter

        // Notify the adapter that the dataset has changed
//        notificationAdapter.notifyDataSetChanged()
        fetchNotificationsFromDatabase()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchNotificationsFromDatabase() {
        // Get the reference to the "notifications" node under the current user's node
        val notificationsRef = database.reference
            .child("users")
            .child(auth.currentUser?.uid.toString())
            .child("notifications")

        // Add a ValueEventListener to fetch the notifications data
        notificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notificationsList.clear()
                for (snapshot in dataSnapshot.children) {
                    // Get the key of each notification node
                    val notificationKey = snapshot.key
                    // Get the notification data from the child node under the notification key
                    val notificationData = snapshot.getValue(Notification::class.java)
                    notificationData?.let {
                        // Assuming your Notification class has appropriate properties,
                        // you can create a Notification object from the retrieved data
                        val notification = Notification(
                            notificationData.from,
                            notificationData.type,
                            notificationData.message,
                            notificationData.date_created,
                        )
                        // Add the notification to the list
                        notificationsList.add(notification)
                    }
                }
                // Notify the adapter that the dataset has changed
                notificationAdapter.notifyDataSetChanged()
                notificationsList.reverse()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching notifications: ${databaseError.message}")
            }
        })
    }

}