package edu.utap.pictureperfect.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import edu.utap.pictureperfect.MainActivity
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods

class RegisterActivity : AppCompatActivity() {
    private val TAG = "RegisterActivity"

    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var mUsername: EditText
    private lateinit var linkLogin: TextView
    private lateinit var registerBtn: Button
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var firebaseMethods: FirebaseMethods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mEmail = findViewById(R.id.user_email_register)
        mPassword = findViewById(R.id.user_password_register)
        linkLogin = findViewById(R.id.link_login)
        registerBtn = findViewById(R.id.register_btn)
        mUsername = findViewById(R.id.user_name)
        firebaseMethods = FirebaseMethods()

        Log.d(TAG, "onCreate: started.")

        linkLogin.setOnClickListener() {
            navigateToLogin()
        }

        registerBtn.setOnClickListener() {
            val email = mEmail.text.toString()
            val password = mPassword.text.toString()
            val fullName = mUsername.text.toString()
            val username = fullName.lowercase().replace(" ", ".")

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Log.d(TAG, "email: $email and password: $password")
                firebaseMethods.checkIfUsernameExists(username) { usernameExists ->
                    if (usernameExists) {
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    firebaseMethods.checkIfUsernameExists(username) { usernameExists ->
                                        if (!usernameExists) {
                                            val user = auth.currentUser
                                            firebaseMethods.addNewUser(email, fullName, username, "Set your bio here!", "none")
                                            navigateToMainFragmentNavigator()
                                        } else {
                                            Toast.makeText(
                                                baseContext,
                                                "Authentication failed.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                                    Toast.makeText(
                                        baseContext,
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                    }
                }
            }
        }
    }

    private fun navigateToMainFragmentNavigator() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
