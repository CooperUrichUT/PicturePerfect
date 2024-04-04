package edu.utap.pictureperfect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import edu.utap.pictureperfect.databinding.ActivityMainBinding
import edu.utap.pictureperfect.ui.dashboard.DashboardFragment
import edu.utap.pictureperfect.ui.login.LoginActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        // Check if the intent contains the PROFILE_PICTURE_URL extra
        if (intent.hasExtra("PROFILE_PICTURE_URL")) {
            // Retrieve the PROFILE_PICTURE_URL extra
            val profilePictureUrl = intent.getStringExtra("PROFILE_PICTURE_URL") ?: ""
            // Navigate to the DashboardFragment and pass the profile picture URL
            val dashboardFragment = DashboardFragment()
            val bundle = Bundle()
            bundle.putString("PROFILE_PICTURE_URL", profilePictureUrl)
            dashboardFragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, dashboardFragment)
                .commit()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        // Check if a user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in, navigate to the appropriate destination
            // For example, navigate to the home fragment
//            navController.navigate(R.id.nav_host_fragment_activity_main)
        } else {
           val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
