package com.example.doan.view.ui.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R
import com.example.doan.databinding.ActivityProfileBinding
import com.google.firebase.auth.EmailAuthProvider
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
        binding.ivEditPassword.setOnClickListener {
            // Inflate giao diện dialog từ layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_repass, null)
            val oldPasswordInput = dialogView.findViewById<EditText>(R.id.etOldPassword)
            val newPasswordInput = dialogView.findViewById<EditText>(R.id.etNewPassword)
            val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
            val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

            // Tạo dialog với kiểu nền trong suốt
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            // Xử lý sự kiện khi nhấn nút xác nhận
            btnSubmit.setOnClickListener {
                val oldPassword = oldPasswordInput.text.toString().trim()
                val newPassword = newPasswordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                // Kiểm tra đầu vào
                when {
                    oldPassword.isEmpty() -> {
                        oldPasswordInput.error = "Vui lòng nhập mật khẩu cũ"
                    }
                    newPassword.isEmpty() -> {
                        newPasswordInput.error = "Vui lòng nhập mật khẩu mới"
                    }
                    confirmPassword.isEmpty() -> {
                        confirmPasswordInput.error = "Vui lòng xác nhận mật khẩu"
                    }
                    newPassword != confirmPassword -> {
                        confirmPasswordInput.error = "Mật khẩu xác nhận không khớp"
                    }
                    else -> {
                        changePassword(oldPassword, newPassword, dialog)
                    }
                }
            }

            // Hiển thị dialog
            dialog.show()
        }



        // Fetch user profile data from Firebase Realtime Database
        fetchUserProfile()
    }
    private fun validatePasswords(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
            return false
        }
        if (newPassword.length < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Hàm đổi mật khẩu
    private fun changePassword(oldPassword: String, newPassword: String, dialog: AlertDialog) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, oldPassword)

            // Xác thực mật khẩu cũ
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Đổi mật khẩu
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fetchUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Use sanitized email as user ID
            val userId = user.email?.replace(".", "")?.replace("@", "") ?: return

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
                        Toast.makeText(this, "Thông tin người dùng không tồn tại.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Lỗi lấy dữ liệu thông tin người dùng: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Use sanitized email as user ID
            val userId = user.email?.replace(".", "")?.replace("@", "") ?: return

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

            // Update the database using the sanitized email as ID
            database.child("users").child(userId).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Thông tin người dùng cập nhật thành công", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Thông tin người dùng cập nhật thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
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
