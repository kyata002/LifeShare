package com.example.doan.view.ui.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.FileCloud
import com.example.doan.Adapter.FileUpAdapter
import com.example.doan.const.Companion.ACTION_FILE_UPLOADED
import com.example.doan.const.Companion.ACTION_SORT_FILES
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FileCommunityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileUpAdapter: FileUpAdapter
    val filesListMul = mutableListOf<FileCloud>()
    private var fileList: List<FileCloud> = emptyList()  // List to hold FileCloud objects
    private lateinit var progressBar: ProgressBar  // Progress bar for loading indication
    private lateinit var btnSort: ImageView
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_file_community, container, false)

        // Initialize ProgressBar
        progressBar = view.findViewById(R.id.progressBar)

        // Show the ProgressBar while loading data
        progressBar.visibility = View.VISIBLE

        // Get the current user
        val user = auth.currentUser
        if (user != null) {
            // Sanitize the email to create a valid Firebase key
            val sanitizedEmail = user.email?.sanitizeEmail()
            if (sanitizedEmail != null) {
                // Reference to the user's listAppUp node using sanitized email
                val userRef =
                    database.getReference("users").child(sanitizedEmail).child("listAppUp")

                // Fetch the list of files from Firebase
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Hide the ProgressBar once data is loaded
                        progressBar.visibility = View.GONE


                        if (snapshot.exists()) {

                            // Populate the list with data from the snapshot
                            for (fileSnapshot in snapshot.children) {
                                val name =
                                    fileSnapshot.child("name").getValue(String::class.java) ?: ""
                                val size =
                                    fileSnapshot.child("size").getValue(Long::class.java) ?: 0L
                                val type =
                                    fileSnapshot.child("type").getValue(String::class.java) ?: ""
                                val path =
                                    fileSnapshot.child("path").getValue(String::class.java) ?: ""
                                val lastModified =
                                    fileSnapshot.child("lastModified").getValue(Long::class.java)
                                        ?: 0L
                                val downloadUrl =
                                    fileSnapshot.child("downloadUrl").getValue(String::class.java)
                                        ?: ""
                                val fileId =
                                    fileSnapshot.child("fileId").getValue(Int::class.java) ?: 0
                                val location =
                                    fileSnapshot.child("location").getValue(String::class.java)
                                        ?: ""

                                // Create a FileCloud object and add it to the list
                                val file = FileCloud(
                                    name,
                                    size,
                                    type,
                                    lastModified,
                                    downloadUrl,
                                    location,
                                    fileId
                                )
                                filesListMul.add(file)
                            }

                            if (filesListMul.isNotEmpty()) {
                                fileList = filesListMul

                                // Initialize RecyclerView and Adapter with data
                                recyclerView = view.findViewById(R.id.rcv_file_Up)
                                recyclerView.layoutManager = LinearLayoutManager(context)

                                fileUpAdapter = FileUpAdapter(fileList)
                                recyclerView.adapter = fileUpAdapter


                            } else {
                                Toast.makeText(
                                    context,
                                    "Không có tài liệu được tải lên",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Không có tài liệu được tải lên",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Hide the ProgressBar if there's an error
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            context,
                            "Lấy dữ liệu tài liệu thất bại: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                // Handle case where email is null
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Email người dùng không tồn tại.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle case where user is not authenticated
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Không có tài khoản tồn tại.", Toast.LENGTH_SHORT).show()
        }

        btnSort = view.findViewById(R.id.btn_sort)
        btnSort.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), btnSort)
            popupMenu.menuInflater.inflate(R.menu.popup_sort, popupMenu.menu)

            // Retrieve saved sort option from SharedPreferences
            val savedSortOption =
                requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getString("sort_option", "a_to_z") // Default to "a_to_z"

            // Set checked state based on saved sort option
            when (savedSortOption) {
                "a_to_z" -> popupMenu.menu.findItem(R.id.a_to_z).isChecked = true
                "z_to_a" -> popupMenu.menu.findItem(R.id.z_to_a).isChecked = true
                "by_size" -> popupMenu.menu.findItem(R.id.by_size).isChecked = true
                "by_date" -> popupMenu.menu.findItem(R.id.by_date).isChecked = true
            }

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.a_to_z -> {
                        sortFileList("a_to_z")
                        saveSortOption("a_to_z")
                        true
                    }

                    R.id.z_to_a -> {
                        sortFileList("z_to_a")
                        saveSortOption("z_to_a")
                        true
                    }

                    R.id.by_size -> {
                        sortFileList("by_size")
                        saveSortOption("by_size")
                        true
                    }

                    R.id.by_date -> {
                        sortFileList("by_date")
                        saveSortOption("by_date")
                        true
                    }

                    else -> false
                }
            }

// Use reflection to access the internal PopupMenu popup and set the background
            try {
                val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldPopup.isAccessible = true
                val popup = fieldPopup.get(popupMenu)
                popup.javaClass.getDeclaredMethod("setBackgroundDrawable", Drawable::class.java)
                    .invoke(
                        popup,
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_menu_sort)
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            popupMenu.show()
        }

        return view
    }

    /**
     * Extension function to sanitize email by removing '.' and '@' characters.
     */
    private fun String.sanitizeEmail(): String {
        return this.replace(".", "").replace("@", "")
    }

    private val fileUploadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Handle the broadcast here
            if (intent?.action == ACTION_FILE_UPLOADED) {
                val message = intent.getStringExtra("message")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                // Call the method to refresh the file list after successful upload
                refreshFileList()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Register the receiver to listen for file upload success
        val filter = IntentFilter(ACTION_FILE_UPLOADED)
        requireContext().registerReceiver(fileUploadReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        // Unregister the receiver when the fragment is no longer active
        requireContext().unregisterReceiver(fileUploadReceiver)
    }

    private fun refreshFileList() {
        progressBar = requireView().findViewById(R.id.progressBar)

        // Show the ProgressBar while loading data
        progressBar.visibility = View.VISIBLE

        // Get the current user
        val user = auth.currentUser
        if (user != null) {
            // Sanitize the email to create a valid Firebase key
            val sanitizedEmail = user.email?.sanitizeEmail()
            if (sanitizedEmail != null) {
                // Reference to the user's listAppUp node using sanitized email
                val userRef =
                    database.getReference("users").child(sanitizedEmail).child("listAppUp")

                // Fetch the list of files from Firebase
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Hide the ProgressBar once data is loaded
                        progressBar.visibility = View.GONE

                        if (snapshot.exists()) {
                            val filesList = mutableListOf<FileCloud>()

                            // Populate the list with data from the snapshot
                            for (fileSnapshot in snapshot.children) {
                                val name =
                                    fileSnapshot.child("name").getValue(String::class.java) ?: ""
                                val size =
                                    fileSnapshot.child("size").getValue(Long::class.java) ?: 0L
                                val type =
                                    fileSnapshot.child("type").getValue(String::class.java) ?: ""
                                val path =
                                    fileSnapshot.child("path").getValue(String::class.java) ?: ""
                                val lastModified =
                                    fileSnapshot.child("lastModified").getValue(Long::class.java)
                                        ?: 0L
                                val downloadUrl =
                                    fileSnapshot.child("downloadUrl").getValue(String::class.java)
                                        ?: ""
                                val fileId =
                                    fileSnapshot.child("fileId").getValue(Int::class.java) ?: 0
                                val location =
                                    fileSnapshot.child("location").getValue(String::class.java)
                                        ?: ""

                                // Create a FileCloud object and add it to the list
                                val file = FileCloud(
                                    name,
                                    size,
                                    type,
                                    lastModified,
                                    downloadUrl,
                                    location,
                                    fileId
                                )
                                filesList.add(file)
                            }

                            if (filesList.isNotEmpty()) {
                                fileList = filesList

                                // Initialize RecyclerView and Adapter with data
                                recyclerView = view?.findViewById(R.id.rcv_file_Up)!!
                                recyclerView.layoutManager = LinearLayoutManager(context)

                                fileUpAdapter = FileUpAdapter(fileList)
                                recyclerView.adapter = fileUpAdapter

                                // Show a Toast message with the number of files
                            } else {
                                Toast.makeText(
                                    context,
                                    "Không có tài liệu được tải lên",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Không có tài liệu được tải lên",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Hide the ProgressBar if there's an error
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            context,
                            "Lấy dữ liệu tài liệu thất bại: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                // Handle case where email is null
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Email người dùng không tồn tại.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle case where user is not authenticated
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Không có tài khoản tồn tại.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sortFileList(sortOption: String?) {
        when (sortOption) {
            "a_to_z" -> filesListMul.sortBy { it.name }
            "z_to_a" -> filesListMul.sortByDescending { it.name }
            "by_size" -> filesListMul.sortBy { it.size }
            "by_date" -> filesListMul.sortBy { it.lastModified }
        }
        fileUpAdapter.updateFiles(filesListMul) // Update adapter to reflect changes
    }

    private fun saveSortOption(sortOption: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("sort_option", sortOption).apply()

        // Broadcast the sort option
        val intent = Intent(ACTION_SORT_FILES)
        intent.putExtra("sort_option", sortOption)
        requireContext().sendBroadcast(intent)
    }


}
