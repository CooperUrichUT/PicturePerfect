package edu.utap.pictureperfect.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import edu.utap.pictureperfect.MainActivity
import edu.utap.pictureperfect.R

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    private lateinit var mContext: Context
    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var mLoginButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var linkSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        mEmail = findViewById(R.id.user_email)
        mPassword = findViewById(R.id.user_password)
        mLoginButton = findViewById(R.id.login_btn)
        linkSignUp = findViewById(R.id.link_signup)
        mContext = this
        Log.d(TAG, "onCreate: started.")

        mLoginButton.setOnClickListener {
            val email = mEmail.text.toString()
            val password = mPassword.text.toString()


            // Validate email and password
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Call Firebase authentication method to sign in with email and password
                Log.d(TAG ,"email: $email and password: $password")
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            navigateToMainFragmentNavigator()
//                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
//                            updateUI(null)
                        }
                    }
            } else {
                // Display an error message if email or password is empty
                Log.e(TAG, "Email or password is empty")
                // Handle empty email or password
            }

        }
        linkSignUp.setOnClickListener() {
            navigateToRegister()
        }
    }

    private fun isUserIdValid(user: FirebaseUser?): Boolean {
        // Check if the user ID is valid
        return user != null && user.uid.isNotEmpty()
    }

    private fun navigateToMainFragmentNavigator() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close LoginActivity
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish() // Close LoginActivity
    }
}
