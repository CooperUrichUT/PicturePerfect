package edu.utap.pictureperfect.ui.profile

import GridImageAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.ActivityDashboardProfileBinding
import edu.utap.pictureperfect.databinding.FragmentDashboardBinding
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods
import edu.utap.pictureperfect.ui.Utils.UniversalImageLoader
import edu.utap.pictureperfect.ui.dashboard.NextActivity

class DashboardProfileActivity : AppCompatActivity() {
    private val TAG = "DashboardFragment"
    private lateinit var mViewPager: ViewPager
    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private val profileViewModel: ProfileViewModel by viewModels()// Initialize profileViewModel
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var imageLoader: UniversalImageLoader
    private val viewModel: ProfileViewModel by viewModels()

    private var imageURL: String = "" // Declare imageURL here
    private lateinit var btnUpdateProfilePicture: Button
    private lateinit var btnLaunchCamera: Button
    private lateinit var gridView: GridView
    private lateinit var galleryImageView: ImageView




    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.pictureSuccess()
        } else {
            viewModel.pictureFailure()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_profile)
        btnUpdateProfilePicture = findViewById(R.id.btnUpdateProfilePicture)
        btnLaunchCamera = findViewById(R.id.btnLaunchCamera)
        gridView = findViewById(R.id.gridView)
        galleryImageView = findViewById(R.id.galleryImageView)
        imageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(imageLoader.getConfig())
        tempImageGridSetUp()

        btnUpdateProfilePicture.setOnClickListener {
            if (imageURL != "") {
                val intent = Intent(this, ProfileNextActivity::class.java)
                // Add the imageURL as an extra to the Intent
                intent.putExtra("PROFILE_PICTURE_URL", imageURL)
                // Start NextActivity
                startActivity(intent)
            }
        }

        btnLaunchCamera.setOnClickListener {
            Log.d(TAG, "Open Camera button pressed")
            // Call the function to take a picture from your existing project
            takePicture()
        }

        gridView.setOnItemClickListener { parent, _, position, _ ->
            // Get the URL of the clicked image
            imageURL = (parent.getItemAtPosition(position) as String)
            // Load the image into the galleryImageView
            imageLoader.setImage(imageURL, galleryImageView, null, "")
        }
        // Observe the lastPictureUri LiveData from the ViewModel
        viewModel.lastPictureUri.observe(this) { uri ->
            uri?.let {
                // Load the image into galleryImageView
                galleryImageView.setImageURI(it)
            }
        }

    }

    // This method will help to retrieve the image
    // This method will be called when a picture is successfully taken
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val photo: Bitmap? = data?.extras?.get("data") as? Bitmap
            photo?.let {
                // Pass the Bitmap to the next activity
                val intent = Intent(this, ProfileNextActivity::class.java)
                intent.putExtra("IMAGE_BITMAP", photo)
                startActivity(intent)
            }
        }
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



    // Function to take a picture
    private fun takePicture() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Start the activity with camera_intent, and request pic id
        startActivityForResult(cameraIntent, 123)
    }
    private fun setupImageGrid(imgURLs: ArrayList<String>) {
        val gridView = gridView
        val adapter = GridImageAdapter(this, R.layout.layout_grid_image_view, "", imgURLs)
        gridView.adapter = adapter
    }


}

