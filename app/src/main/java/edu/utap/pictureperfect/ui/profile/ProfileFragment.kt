package edu.utap.pictureperfect.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.FragmentProfileBinding
import edu.utap.pictureperfect.ui.Models.User
import edu.utap.pictureperfect.ui.Models.UserAccountSettings
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods
import edu.utap.pictureperfect.ui.Utils.GridImageAdapter
import edu.utap.pictureperfect.ui.Utils.UniversalImageLoader
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
        firebaseMethods.getUserSettingsData(auth.currentUser?.uid.toString()) { retrievedUserSettings ->
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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set custom title for the action bar
        requireActivity().title = "Profile"
        imageLoader = UniversalImageLoader(requireContext())
        ImageLoader.getInstance().init(imageLoader.getConfig())
        setProfileImage()
        tempImageGridSetUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setProfileImage() {
        val imageURL = "https://www.android.com/static/2016/img/share/andy-lg.png"
        imageLoader.setImage(imageURL, binding.imageProfile, null, "")
    }
    private fun tempImageGridSetUp() {
        val imgURLs: ArrayList<String> = ArrayList()
        // Add 7 placeholder image URLs (replace with desired placeholders)
        imgURLs.add("https://picsum.photos/200/300")
        imgURLs.add("https://picsum.photos/200/300")
        imgURLs.add("https://picsum.photos/200/300")
        imgURLs.add("https://picsum.photos/200/300")

        setupImageGrid(imgURLs)
    }
    private fun setupImageGrid(imgURLs: ArrayList<String>) {
        val gridView = binding.gridViewPosts
        val adapter = GridImageAdapter(requireContext(), R.layout.layout_grid_image_view, "", imgURLs)
        gridView.adapter = adapter
    }

    fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}

