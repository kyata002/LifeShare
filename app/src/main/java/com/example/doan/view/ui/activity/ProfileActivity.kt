package com.example.doan.view.ui.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R
import com.example.doan.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupStatusBar()

        // Configure edit icons and input handling
        val editIcons = listOf(binding.ivEditUsername, binding.ivEditEmail, binding.ivEditPhoneNumber)
        val textViews = listOf(binding.tvUsername, binding.tvEmail, binding.tvPhoneNumber)
        val editTexts = listOf(binding.etUsername, binding.etEmail, binding.etPhoneNumber)

        // Show EditText and save/cancel buttons on edit icon click
        editIcons.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                textViews[index].visibility = View.GONE
                editTexts[index].visibility = View.VISIBLE
                binding.btnContainer.visibility = View.VISIBLE
            }
        }

        // Save button
        binding.btnSave.setOnClickListener {
            saveUserProfile() // Call the new method to save data

            // Update UI to show TextViews and hide EditTexts
            textViews.forEachIndexed { index, textView ->
                textView.text = editTexts[index].text.toString()
                textView.visibility = View.VISIBLE
                editTexts[index].visibility = View.GONE
            }
            binding.btnContainer.visibility = View.GONE
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            hideEditableFields()
        }

        // Hide EditText fields when tapping outside them
        binding.root.setOnClickListener {
            hideEditableFields()
        }

        // Navigate back
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Prevent hiding inputs when tapping inside EditText fields
        editTexts.forEach { editText ->
            editText.setOnClickListener {
                // Prevent root click from triggering hide behavior
            }
        }

        // Fetch user profile data from Firebase Realtime Database
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            database.child("users").child(userId).get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        // Retrieve user data
                        val username = dataSnapshot.child("name").getValue(String::class.java) ?: "No username"
                        val email = dataSnapshot.child("email").getValue(String::class.java) ?: user.email ?: "No email"
                        val phoneNumber = dataSnapshot.child("phone").getValue(String::class.java) ?: "No phone number"

                        // Set the data to TextViews and EditTexts
                        binding.tvUsername.text = username
                        binding.tvEmail.text = email
                        binding.tvPhoneNumber.text = phoneNumber

                        binding.etUsername.setText(username)
                        binding.etEmail.setText(email)
                        binding.etPhoneNumber.setText(phoneNumber)
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid

            // Collect data from EditTexts
            val updatedUsername = binding.etUsername.text.toString()
            val updatedEmail = binding.etEmail.text.toString()
            val updatedPhoneNumber = binding.etPhoneNumber.text.toString()

            // Create a map of updated data
            val updates = mapOf(
                "name" to updatedUsername,
                "email" to updatedEmail,
                "phone" to updatedPhoneNumber
            )

            // Update the database
            database.child("users").child(userId).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hideEditableFields() {
        val editTexts = listOf(binding.etUsername, binding.etEmail, binding.etPhoneNumber)
        val textViews = listOf(binding.tvUsername, binding.tvEmail, binding.tvPhoneNumber)

        // Hide EditText fields and show TextViews
        textViews.forEach { it.visibility = View.VISIBLE }
        editTexts.forEachIndexed { index, editText ->
            editText.visibility = View.GONE
            editText.setText(textViews[index].text) // Reset value
        }
        binding.btnContainer.visibility = View.GONE

        // Hide keyboard if focus is not on EditText
        if (currentFocus !is EditText) {
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus ?: binding.root
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)
        }
    }
}
