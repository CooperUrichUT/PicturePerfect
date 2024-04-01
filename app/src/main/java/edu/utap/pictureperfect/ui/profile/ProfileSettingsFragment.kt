// ProfileSettingsFragment.kt
package edu.utap.pictureperfect.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.databinding.FragmentProfileSettingsBinding
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods
import edu.utap.pictureperfect.ui.Utils.UniversalImageLoader

class ProfileSettingsFragment : Fragment() {

    private var _binding: FragmentProfileSettingsBinding? = null
    private val binding get() = _binding!!
    private val TAG = "ProfileSettingsFragment"
    private lateinit var imageLoader: UniversalImageLoader
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private val profileViewModel: ProfileViewModel by viewModels()// Initialize profileViewModel
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var editUsername: TextView
    private lateinit var editBio: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set custom title for the action bar
        requireActivity().title = "Settings"
        auth = Firebase.auth

        // Enable back button in the action bar
        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize UniversalImageLoader
        imageLoader = UniversalImageLoader(requireContext())
        ImageLoader.getInstance().init(imageLoader.getConfig())
        editUsername = binding.editUsername
        editBio = binding.editBio

        // Set profile image using Universal Image Loader
        setProfileImage()

        // Set click listener for the "Save" button
        binding.btnSave.setOnClickListener {
            // Navigate back to the profile screen
            findNavController().popBackStack()
            // Assume you have a reference to the Firebase Database
            val databaseReference = FirebaseDatabase.getInstance().reference
            firebaseMethods.updateUsernameAndBio(auth.currentUser?.uid.toString(), editUsername.text.toString(), editBio.text.toString())
        }

        // Set click listener for the "Discard" button
        binding.btnDiscard.setOnClickListener {
            // Navigate back to the profile screen
            findNavController().popBackStack()
        }
    }

    private fun setProfileImage() {
        Log.d(TAG, "Setting the profile image")
        val imageURL = "https://www.android.com/static/2016/img/share/andy-lg.png"
        // Set the imageURL in the shared ViewModel
        imageLoader.setImage(imageURL, binding.imageProfile, null, "")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
