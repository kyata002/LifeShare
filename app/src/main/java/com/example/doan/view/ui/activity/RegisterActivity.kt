package com.example.doan.view.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R
import com.example.doan.data.model.AccountModel
import com.example.doan.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var mDatabase: DatabaseReference? = null
    private val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().getReference("users") // Reference to "users" node

        setupStatusBar()

        // Register button click listener
        binding.btSignUp.setOnClickListener {
            val username = binding.tvUsername.text.toString().trim()
            val email = binding.tvEmail.text.toString().trim()
            val phoneNumber = binding.tvPhone.text.toString().trim()
            val password = binding.tvPassword.text.toString().trim()
            val confirmPassword = binding.tvRepassWord.text.toString().trim()

            // Validate input
            if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register the user with Firebase Authentication
            registerUser(email, password, username, phoneNumber)
        }

        // Login redirect
        binding.tvLoginRedirect.setOnClickListener {
            navigateToActivity(LoginActivity::class.java) // Direct to LoginActivity
        }
    }

    private fun registerUser(email: String, password: String, username: String, phoneNumber: String) {
        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User registered successfully
                    val firebaseUser: FirebaseUser? = auth.currentUser
                    if (firebaseUser != null) {
                        // Use email as the user ID, sanitize it to avoid invalid characters in the Firebase path
                        val userId = email.replace(".", "").replace("@", "") // Remove dots and at symbol
                        val userRef: DatabaseReference = mDatabase!!.child(userId) // Create reference to user's node

                        // Create the AccountModel object
                        val user = AccountModel(
                            id = userId,           // Use the sanitized email as ID
                            username = username,
                            email = email,
                            password = password,   // Consider removing this from the database for security
                            numberphone = phoneNumber,
                            listAppUp = emptyList(),
                            listShare = emptyList()
                        )

                        // Map the user data to be pushed to Firebase
                        val dataMap = hashMapOf<String, Any>(
                            "id" to user.id,
                            "name" to user.username,
                            "email" to user.email,
                            "phone" to user.numberphone,
                            "listAppUp" to user.listAppUp,
                            "listShare" to user.listShare
                        )

                        // Push user data to Firebase
                        userRef.setValue(dataMap)
                            .addOnSuccessListener {
                                // User data saved successfully
                                navigateToActivity(LoginActivity::class.java)
                                finish() // End the current activity
                            }
                            .addOnFailureListener {
                                // Handle failure in data saving
                                Toast.makeText(context, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Handle case where currentUser is null
                        Toast.makeText(context, "User creation failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User registration failed
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(context, "Email đã được đăng ký", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }



    @SuppressLint("ObsoleteSdkInt")
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Set status bar color
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)

            // Set status bar icon color (light or dark)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true // or false for light icons
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
    }
}
