package edu.utap.pictureperfect.ui.dashboard

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
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.FragmentDashboardBinding
import edu.utap.pictureperfect.ui.Utils.SectionsPagerAdapter

class DashboardFragment : Fragment() {
    private val TAG = "DaashboardFragment"
    private var _binding: FragmentDashboardBinding? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private var galleryFragment = GalleryFragment()
    private var photoFragment = PhotoFragment()
    private lateinit var openCamera: Button


    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("StringFormatInvalid", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        openCamera = binding.btnLaunchCamera

        openCamera.setOnClickListener() {
            Log.d(TAG, "Open Camera button pressed")
            // Create an intent to open the camera application
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            // Check if there is a camera app available to handle the intent
            if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
                // Start the camera activity
                startActivity(cameraIntent)
            } else {
                // If no camera app is available, display a toast or handle the situation accordingly
                Log.e(TAG, "No camera app found")
            }
        }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set custom title for the action bar
        requireActivity().title = "Dashboard"

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

