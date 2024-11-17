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
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupStatusBar()

        val editIcons = listOf(binding.ivEditUsername, binding.ivEditEmail, binding.ivEditPhoneNumber)
        val textViews = listOf(binding.tvUsername, binding.tvEmail, binding.tvPhoneNumber)
        val editTexts = listOf(binding.etUsername, binding.etEmail, binding.etPhoneNumber)

        // When edit icon is clicked, show EditText and save/cancel buttons
        editIcons.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                textViews[index].visibility = View.GONE
                editTexts[index].visibility = View.VISIBLE
                binding.btnContainer.visibility = View.VISIBLE
            }
        }

        // Save button
        binding.btnSave.setOnClickListener {
            textViews.forEachIndexed { index, textView ->
                textView.text = editTexts[index].text.toString()
                textView.visibility = View.VISIBLE
                editTexts[index].visibility = View.GONE
            }
            binding.btnContainer.visibility = View.GONE
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            textViews.forEachIndexed { index, textView ->
                editTexts[index].visibility = View.GONE
                textView.visibility = View.VISIBLE
            }
            binding.btnContainer.visibility = View.GONE
        }

        // Listener to detect taps outside the EditTexts
        binding.root.setOnClickListener {
            // Hide EditText fields and show TextViews
            textViews.forEach { textView ->
                textView.visibility = View.VISIBLE
            }
            editTexts.forEachIndexed { index, editText ->
                editText.visibility = View.GONE
                // Reset value of EditText from TextView
                editText.setText(textViews[index].text)
            }
            binding.btnContainer.visibility = View.GONE

            // Hide the keyboard if focus is not on EditText
            if (currentFocus !is EditText) {
                hideKeyboard()
            }
        }

        // Navigate back
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Prevent hiding inputs when tapping inside EditText fields
        listOf(binding.etUsername, binding.etEmail, binding.etPhoneNumber).forEach { editText ->
            editText.setOnClickListener {
                // Prevent root click from triggering hide behavior
            }
        }

        // Fetch user profile data from Firebase Firestore
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Fetch user profile data from Firestore
            val userId = user.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Populate the UI with the user data
                        val username = document.getString("username") ?: ""
                        val email = document.getString("email") ?: user.email ?: ""
                        val phoneNumber = document.getString("phoneNumber") ?: ""

                        // Set the data to text views
                        binding.tvUsername.text = username
                        binding.tvEmail.text = email
                        binding.tvPhoneNumber.text = phoneNumber

                        // Set the values to EditText fields as well (for editing)
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
