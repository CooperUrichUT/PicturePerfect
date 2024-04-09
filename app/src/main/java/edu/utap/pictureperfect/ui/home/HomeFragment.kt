package edu.utap.pictureperfect.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Models.Photo
import edu.utap.pictureperfect.ui.Utils.PostAdapter
import edu.utap.pictureperfect.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var listView: ListView
    private lateinit var postAdapter: PostAdapter
    private val photosList = ArrayList<Photo>() // List to store photo objects
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize ListView
        listView = binding.mainListView
        postAdapter = PostAdapter(requireContext(), R.layout.layout_view_post, photosList)
        listView.adapter = postAdapter

        // Retrieve photos from Firebase Realtime Database
        fetchPhotosFromDatabase()

        return root
    }

    private fun fetchPhotosFromDatabase() {
        // Reference to the "photos" node in the database
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("photos")

        // Get the current user's ID
        val currentUserId = Firebase.auth.currentUser?.uid

        // Query to get the list of users that the current user is following
        val followingRef =
            currentUserId?.let {
                FirebaseDatabase.getInstance().reference.child("users").child(
                    it
                ).child("following_users")
            }

        // Attach a listener to get the list of users that the current user is following
        if (followingRef != null) {
            followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val followingUsers = dataSnapshot.children.mapNotNull { it.key }

                    // Attach a listener to read the data at the "photos" node
                    databaseReference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Clear the list before adding new data
                            photosList.clear()

                            // Iterate through each child node under "photos"
                            for (snapshot in dataSnapshot.children) {
                                val photo: Photo? = snapshot.getValue(Photo::class.java)
                                photo?.let {
                                    // Add the photo object to the list if it belongs to the current user or a user they are following
                                    if (photo.user_id == currentUserId || followingUsers.contains(photo.user_id)) {
                                        photosList.add(photo)
                                    }
                                }
                            }

                            // Notify the adapter that the dataset has changed
                            postAdapter.notifyDataSetChanged()
//                            photosList.reverse()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
