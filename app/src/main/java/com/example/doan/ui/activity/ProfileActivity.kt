package com.example.doan.ui.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R
import com.example.doan.databinding.ActivityProfileBinding
import com.google.android.material.internal.ViewUtils.hideKeyboard

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        // Listener to detect taps outside the EditTexts
        binding.root.setOnClickListener {
            // Hide EditText fields and show TextViews
            textViews.forEach { textView ->
                textView.visibility = View.VISIBLE
            }
            editTexts.forEachIndexed { index, editText ->
                editText.visibility = View.GONE
                // Gán lại giá trị của TextView cho EditText tương ứng
                editText.setText(textViews[index].text)
            }
            binding.btnContainer.visibility = View.GONE

            // Only hide keyboard if focus is not on EditText
            if (currentFocus !is EditText) {
                hideKeyboard()
            }
        }
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

// Prevent the root layout from hiding inputs when tapping inside the EditText fields
        listOf(binding.etUsername, binding.etEmail, binding.etPhoneNumber).forEach { editText ->
            editText.setOnClickListener {
                // Just prevent the root from triggering hide behavior when tapping inside EditText
            }
        }



    }

    private fun hideKeyboard() {
        // Use the root view or any other focused view to hide the keyboard
        val view = currentFocus ?: binding.root  // Fallback to root view if no focus
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
    @SuppressLint("ObsoleteSdkInt")
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Đặt nền của thanh trạng thái
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)

            // Đặt màu biểu tượng (tối hoặc sáng)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true // hoặc false cho biểu tượng sáng
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)
        }
    }
}

