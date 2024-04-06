package edu.utap.pictureperfect.ui.profile

import GridImageAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.FragmentProfileBinding
import edu.utap.pictureperfect.ui.Models.User
import edu.utap.pictureperfect.ui.Models.UserAccountSettings
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods
import edu.utap.pictureperfect.ui.Utils.UniversalImageLoader
import edu.utap.pictureperfect.ui.Utils.ViewPost
import edu.utap.pictureperfect.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var imageLoader: UniversalImageLoader
    private lateinit var textUsername: TextView
    private lateinit var textBio: TextView
    private lateinit var postCount: TextView
    private lateinit var followCount: TextView
    private lateinit var followerCount: TextView
    private lateinit var gridView: GridView
    private lateinit var imgURLs: ArrayList<String>
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private val profileViewModel: ProfileViewModel by viewModels()// Initialize profileViewModel
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private var user: User = User()
    private var userSettings = UserAccountSettings()
    private var username: String = ""
    private val TAG = "ProfileFragment"

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("StringFormatInvalid", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize views
        editProfileButton = binding.btnEditProfile
        logoutButton = binding.btnLogout
        textUsername = binding.textUsername
        textBio = binding.textBio
        postCount = binding.textPostsCount
        followCount = binding.textFollowingCount
        followerCount = binding.textFollowersCount
        gridView = binding.gridViewPosts

        auth = Firebase.auth
        firebaseMethods.getUserData(auth.currentUser?.uid.toString()) { retrievedUser ->
            if (retrievedUser != null) {
                // Handle the retrieved user data here
                Log.d(TAG, "Username is ${retrievedUser.username}")
                user = retrievedUser
                textUsername.text = retrievedUser.username
                // For example, update UI or perform any other operations with the user data
            } else {
                // Handle the case where the user data is null or an error occurred
                Log.e(TAG, "Failed to retrieve user data")
            }
        }
        firebaseMethods.getUserAccountSettingsData(auth.currentUser?.uid.toString()) { retrievedUserSettings ->
            if (retrievedUserSettings != null) {
                // Handle the retrieved user data here
                userSettings = retrievedUserSettings
                textBio.text = retrievedUserSettings.user_bio
                postCount.text = "Posts: " + retrievedUserSettings.posts.toString()
                followCount.text = "Following: " + retrievedUserSettings.following.toString()
                followerCount.text = "Followers: " + retrievedUserSettings.followers.toString()
                // For example, update UI or perform any other operations with the user data
            } else {
                // Handle the case where the user data is null or an error occurred
                Log.e(TAG, "Failed to retrieve user data")
            }
        }



//

        // Set click listener for Edit Profile button
        editProfileButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_profile, false)
                .build()
            findNavController().navigate(R.id.profileSettingsFragment, null, navOptions)
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            navigateToLogin()
        }

        // Inside your onViewCreated() method after setting up the GridView adapter

        gridView.setOnItemClickListener { parent, view, position, id ->
            // Get the imageURL of the clicked item based on its position in the list
            val clickedImageURL = imgURLs[position]

            // Get the UID of the current user
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Create an intent to launch the ViewPost activity
            val intent = Intent(requireContext(), ViewPost::class.java)
            val location = "Profile"

            // Put the clicked image URL and the user ID into the intent as extras
            intent.putExtra("IMAGE_URL", clickedImageURL)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("LOCATION", location)

            // Start the ViewPost activity with the intent
            startActivity(intent)
        }



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set custom title for the action bar
        requireActivity().title = "Profile"
        imageLoader = UniversalImageLoader(requireContext())
        ImageLoader.getInstance().init(imageLoader.getConfig())
        setProfileImage()
        fetchUserPhotos()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                    imageLoader.setImage(imageURL, binding.imageProfile, null, "")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching profile picture URL: ${databaseError.message}")
            }
        })
    }

    private fun fetchUserPhotos() {
        Log.d(TAG, "Fetching user photos")

        // Assuming you have a reference to your Firebase Database and the user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userPhotosRef = userId?.let {
            FirebaseDatabase.getInstance().reference.child("user_photos").child(it)
        }

        // Listen for changes to the user's photos in the database
        userPhotosRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val photoList = ArrayList<String>()

                // Iterate through each child node under the user's photos node
                for (snapshot in dataSnapshot.children) {
                    // Get the imageURL of the photo and add it to the list
                    val imageURL = snapshot.child("image_path").getValue(String::class.java)
                    imageURL?.let {
                        photoList.add(it)
                    }
                }

                // Once all photos are retrieved, you can use photoList as needed
                // For example, you can display them in a grid or a list
                // Here you could pass photoList to your adapter or display logic
                // for populating the UI with user photos
                photoList.reverse()
                setupImageGrid(photoList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching user photos: ${databaseError.message}")
            }
        })
    }
    private fun setupImageGrid(images: ArrayList<String>) {
        imgURLs = images
        val gridView = binding.gridViewPosts
        val adapter = GridImageAdapter(requireContext(), R.layout.layout_grid_image_view, "", images)
        gridView.adapter = adapter
    }

    fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}

