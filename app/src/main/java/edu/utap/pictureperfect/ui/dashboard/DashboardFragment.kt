package edu.utap.pictureperfect.ui.dashboard

import GridImageAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.FragmentDashboardBinding
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods
import edu.utap.pictureperfect.ui.Utils.UniversalImageLoader
import edu.utap.pictureperfect.ui.profile.ProfileViewModel
import java.io.ByteArrayOutputStream
import java.io.File

class DashboardFragment : Fragment() {
    private val TAG = "DashboardFragment"
    private var _binding: FragmentDashboardBinding? = null
    private lateinit var mViewPager: ViewPager
    private lateinit var openCamera: Button
    private lateinit var gridView: GridView
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private val profileViewModel: ProfileViewModel by viewModels()// Initialize profileViewModel
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var imageLoader: UniversalImageLoader
    private val viewModel: DashboardViewModel by viewModels()

    private var imageURL: String = "" // Declare imageURL here

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.pictureSuccess()
        } else {
            viewModel.pictureFailure()
        }
    }


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

        binding.btnPostPhoto.setOnClickListener {
            if (imageURL != "") {
                val intent = Intent(requireContext(), NextActivity::class.java)
                // Add the imageURL as an extra to the Intent
                intent.putExtra("IMAGE_URL", imageURL)
                // Start NextActivity
                startActivity(intent)
            }
        }

        binding.btnLaunchCamera.setOnClickListener {
            Log.d(TAG, "Open Camera button pressed")
            // Call the function to take a picture from your existing project
            takePicture()
        }

        binding.gridView.setOnItemClickListener { parent, _, position, _ ->
            // Get the URL of the clicked image
            imageURL = (parent.getItemAtPosition(position) as String)
            // Load the image into the galleryImageView
            imageLoader.setImage(imageURL, binding.galleryImageView, null, "")
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageLoader = UniversalImageLoader(requireContext())
        ImageLoader.getInstance().init(imageLoader.getConfig())
        // Set custom title for the action bar
        requireActivity().title = "Dashboard"
        tempImageGridSetUp()

        // Observe the lastPictureUri LiveData from the ViewModel
        viewModel.lastPictureUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                // Load the image into galleryImageView
                binding.galleryImageView.setImageURI(it)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // This method will help to retrieve the image
    // This method will be called when a picture is successfully taken
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val photo: Bitmap? = data?.extras?.get("data") as? Bitmap
            photo?.let {
                // Pass the Bitmap to the next activity
                val intent = Intent(requireContext(), NextActivity::class.java)
                intent.putExtra("IMAGE_BITMAP", photo)
                startActivity(intent)
            }
        }
    }

    // Function to take a picture
    private fun takePicture() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Start the activity with camera_intent, and request pic id
        startActivityForResult(cameraIntent, 123)
    }

    private fun tempImageGridSetUp() {
        val imgURLs: ArrayList<String> = ArrayList()
        // Add 7 placeholder image URLs (replace with desired placeholders)
        imgURLs.add("https://i.imgur.com/CzXTtJV.jpg")
        imgURLs.add("https://i.imgur.com/OB0y6MR.jpg")
        imgURLs.add("https://farm4.staticflickr.com/3852/14447103450_2d0ff8802b_z_d.jpg")
        imgURLs.add("https://farm2.staticflickr.com/1533/26541536141_41abe98db3_z_d.jpg")
        imgURLs.add("https://farm4.staticflickr.com/3075/3168662394_7d7103de7d_z_d.jpg")
        imgURLs.add("https://i.imgur.com/OnwEDW3.jpg")
        imgURLs.add("https://farm3.staticflickr.com/2220/1572613671_7311098b76_z_d.jpg")
        imgURLs.add("https://farm6.staticflickr.com/5590/14821526429_5c6ea60405_z_d.jpg")
        imgURLs.add("https://farm7.staticflickr.com/6089/6115759179_86316c08ff_z_d.jpg")
        imgURLs.add("https://farm4.staticflickr.com/3224/3081748027_0ee3d59fea_z_d.jpg")
        imgURLs.add("https://farm8.staticflickr.com/7377/9359257263_81b080a039_z_d.jpg")
        imgURLs.add("https://farm9.staticflickr.com/8295/8007075227_dc958c1fe6_z_d.jpg")
        imgURLs.add("https://farm2.staticflickr.com/1449/24800673529_64272a66ec_z_d.jpg")
        imgURLs.add("https://farm4.staticflickr.com/3827/11349066413_99c32dee4a_z_d.jpg")




        setupImageGrid(imgURLs)
    }
    private fun setupImageGrid(imgURLs: ArrayList<String>) {
        val gridView = binding.gridView
        val adapter = GridImageAdapter(requireContext(), R.layout.layout_grid_image_view, "", imgURLs)
        gridView.adapter = adapter
    }


}

