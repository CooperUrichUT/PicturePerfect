package edu.utap.pictureperfect.ui.dashboard

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nostra13.universalimageloader.core.ImageLoader
import edu.utap.pictureperfect.MainActivity
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods
import edu.utap.pictureperfect.ui.Utils.UniversalImageLoader
import edu.utap.pictureperfect.ui.profile.ProfileViewModel
import edu.utap.pictureperfect.ui.Utils.ImageDownloadTask

class NextActivity: AppCompatActivity() {

    private var firebaseMethods: FirebaseMethods = FirebaseMethods()
    private val profileViewModel: ProfileViewModel by viewModels()// Initialize profileViewModel
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var imageLoader: UniversalImageLoader
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next) // Replace with your activity layout
        imageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(imageLoader.getConfig())
        val imageURL = intent.getStringExtra("IMAGE_URL")

        Log.d("NextActivity", "Image URL: $imageURL")

        val imageShare = findViewById<ImageView>(R.id.imageShare) // Replace with your ImageView ID
        val btnPost = findViewById<Button>(R.id.btnPost)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

//        if (intent.getStringExtra("PROFILE_PICTURE_URL") != "") {
//            val caption = findViewById<EditText>(R.id.caption)
//            btnPost.text = "Update"
//            caption.hint = "Profile Picture doesnt need caption"
//            caption.isActivated = false
//
//        } else {
//            val caption = findViewById<EditText>(R.id.caption)
//            btnPost.text = "Post"
//            caption.hint = "Give this photo a caption!"
//            caption.isActivated = true
//        }

        imageURL?.let {
            // Download image from URL and convert to Bitmap using AsyncTask
            ImageDownloadTask { bitmap ->
                // Set the bitmap to your ImageView
                imageShare.setImageBitmap(bitmap)
            }.execute(imageURL)
        }

        firebaseMethods.getImageCount { imageCount ->
            // Use the imageCount here
            Log.d(TAG, "Image count: $imageCount")
        }


        if (imageURL == null) {
            val bitmap = intent.getParcelableExtra<Bitmap>("IMAGE_BITMAP")
            bitmap?.let {
                imageShare.setImageBitmap(bitmap)
            }
        }


        btnPost.setOnClickListener {
            Log.d(TAG, "Attempting to upload image")
            val bitmap = (imageShare.drawable as? BitmapDrawable)?.bitmap // Get the bitmap from ImageView
            val caption = findViewById<EditText>(R.id.caption).text.toString()
            if (bitmap != null) {

                if (intent.getStringExtra("PROFILE_PICTURE_URL") != "") {
                    firebaseMethods.uploadNewPhoto("ProfilePicture", caption, bitmap)
                }

                firebaseMethods.uploadNewPhoto("NewPhoto", caption, bitmap)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close LoginActivity

            } else bitmap
        }

        btnCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close LoginActivity
        }
    }
}