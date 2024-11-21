package com.example.doan.view.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.Adapter.FileCommunityAdapter
import com.example.doan.Adapter.FileShareFileAdapter
import com.example.doan.R
import com.example.doan.const.Companion.ACTION_SORT_FILES
import com.example.doan.data.model.FileCloud
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FileShareFragment : Fragment() {
    private lateinit var rcvFileShare: RecyclerView
    private lateinit var rcvFileCommunity: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fileShareAdapter: FileCommunityAdapter
    private lateinit var fileShareFileAdapter: FileShareFileAdapter
    private var fileListShare: MutableList<FileCloud> = mutableListOf()
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var tvCom: TextView
    private lateinit var tvShare: TextView
    private lateinit var btnSort: ImageView
    private lateinit var imgNotFound: LinearLayout
    private  var isCheck: Boolean = true


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file_share, container, false)
        imgNotFound = view.findViewById(R.id.img_NotFound1)
        // Initialize UI components
        progressBar = view.findViewById(R.id.progressBarS)
        rcvFileShare = view.findViewById(R.id.rcv_file_Share)
        rcvFileCommunity = view.findViewById(R.id.rcv_file_Community)
        tvCom = view.findViewById(R.id.btn_community)
        tvShare = view.findViewById(R.id.btn_data_share)
        btnSort = view.findViewById(R.id.btn_sorts)
        refreshFileList1()

        tvShare.setOnClickListener {
            tvCom.setBackgroundResource(R.drawable.bg_button_false)
            tvCom.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvShare.setBackgroundResource(R.drawable.bg_button_true)
            tvShare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            rcvFileShare.visibility=View.VISIBLE
            rcvFileCommunity.visibility=View.GONE
            refreshFileList()
            isCheck=true
        }

        tvCom.setOnClickListener {
            tvCom.setBackgroundResource(R.drawable.bg_button_true)
            tvCom.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvShare.setBackgroundResource(R.drawable.bg_button_false)
            tvShare.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            rcvFileShare.visibility=View.GONE
            rcvFileCommunity.visibility=View.VISIBLE
            refreshFileList1()
            isCheck=false
        }
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

//        rcvFileShare.layoutManager = LinearLayoutManager(requireContext())
//
//        // Initialize RecyclerView adapter
//        fileShareAdapter = FileShareAdapter(fileListShare)
//        rcvFileShare.adapter = fileShareAdapter

        // Fetch and display file list

        return view
    }

    private fun refreshFileList() {
        progressBar.visibility = View.VISIBLE

        val user = auth.currentUser
        if (user?.email != null) {
            val sanitizedEmail = user.email!!.sanitizeEmail()

            // Reference the Firebase database for the user's shared files
            val userRef = database.getReference("users").child(sanitizedEmail).child("listShare")

            // Fetch the list of files from Firebase
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE

                    if (snapshot.exists()) {
                        val filesList = mutableListOf<FileCloud>()

                        // Populate the list with data from the snapshot
                        for (fileSnapshot in snapshot.children) {
                            val name = fileSnapshot.child("name").getValue(String::class.java) ?: ""
                            val size = fileSnapshot.child("size").getValue(Long::class.java) ?: 0L
                            val type = fileSnapshot.child("type").getValue(String::class.java) ?: ""
                            val path = fileSnapshot.child("path").getValue(String::class.java) ?: ""
                            val lastModified = fileSnapshot.child("lastModified").getValue(Long::class.java) ?: 0L
                            val downloadUrl = fileSnapshot.child("downloadUrl").getValue(String::class.java) ?: ""
                            val fileId = fileSnapshot.child("fileId").getValue(Int::class.java) ?: 0
                            val location = fileSnapshot.child("location").getValue(String::class.java) ?: ""

                            // Create a FileCloud object and add it to the list
                            val file = FileCloud(name, size, type, lastModified, downloadUrl, location, fileId)
                            filesList.add(file)
                        }

                        if (filesList.isNotEmpty()) {
                            fileListShare = filesList

                            // Initialize RecyclerView and Adapter with data
                            rcvFileShare = view!!.findViewById(R.id.rcv_file_Share)
                            rcvFileShare.layoutManager = LinearLayoutManager(context)

                            fileShareFileAdapter = FileShareFileAdapter(fileListShare)
                            rcvFileShare.adapter = fileShareFileAdapter
                            imgNotFound.visibility = View.GONE
                            // Show a Toast message with the number of files
//                            Toast.makeText(context, "Fetched ${filesList.size} files", Toast.LENGTH_SHORT).show()
                        } else {
                            imgNotFound.visibility = View.VISIBLE

                            Toast.makeText(context, "Không có tài liệu được tải lên", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        imgNotFound.visibility = View.VISIBLE
                        Toast.makeText(context, "Lấy dữ liệu tài liệu thất bại", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Lấy dữ liệu tài liệu thất bại: ${error.message}")
                }
            })
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Không có tài khoản tồn tại..", Toast.LENGTH_SHORT).show()
        }
    }
    private fun refreshFileList1() {
        progressBar.visibility = View.VISIBLE

        val user = auth.currentUser
        if (user?.email != null) {
            val sanitizedEmail = user.email!!.sanitizeEmail()

            // Reference the Firebase database for the user's shared files
            val userRef = database.getReference("Community")

            // Fetch the list of files from Firebase
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE

                    if (snapshot.exists()) {
                        val filesList = mutableListOf<FileCloud>()

                        // Populate the list with data from the snapshot
                        for (fileSnapshot in snapshot.children) {
                            val name = fileSnapshot.child("name").getValue(String::class.java) ?: ""
                            val size = fileSnapshot.child("size").getValue(Long::class.java) ?: 0L
                            val type = fileSnapshot.child("type").getValue(String::class.java) ?: ""
                            val path = fileSnapshot.child("path").getValue(String::class.java) ?: ""
                            val lastModified = fileSnapshot.child("lastModified").getValue(Long::class.java) ?: 0L
                            val downloadUrl = fileSnapshot.child("downloadUrl").getValue(String::class.java) ?: ""
                            val fileId = fileSnapshot.child("fileId").getValue(Int::class.java) ?: 0
                            val location = fileSnapshot.child("location").getValue(String::class.java) ?: ""

                            // Create a FileCloud object and add it to the list
                            val file = FileCloud(name, size, type, lastModified, downloadUrl, location, fileId)
                            filesList.add(file)
                        }

                        if (filesList.isNotEmpty()) {
                            fileListShare = filesList

                            // Initialize RecyclerView and Adapter with data
                            rcvFileCommunity = view!!.findViewById(R.id.rcv_file_Community)
                            rcvFileCommunity.layoutManager = LinearLayoutManager(context)

                            fileShareAdapter = FileCommunityAdapter(fileListShare)
                            rcvFileCommunity.adapter = fileShareAdapter
                            imgNotFound.visibility = View.GONE

                            // Show a Toast message with the number of files
//                            Toast.makeText(context, "Fetched ${filesList.size} files", Toast.LENGTH_SHORT).show()
                        } else {
                            imgNotFound.visibility = View.VISIBLE
                            Toast.makeText(context, "Không có tài liệu được tải lên", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        imgNotFound.visibility = View.VISIBLE
                        Toast.makeText(context, "Tài liệu không tồn tại.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Lấy dữ liệu tài liệu thất bại: ${error.message}")
                }
            })
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Không có tài khoản tồn tại.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    // Extension function to sanitize email
    private fun String.sanitizeEmail(): String {
        return replace(".", "").replace("@", "")
    }
    private fun sortFileList(sortOption: String?) {
        when (sortOption) {
            "a_to_z" -> fileListShare.sortBy { it.name }
            "z_to_a" -> fileListShare.sortByDescending { it.name }
            "by_size" -> fileListShare.sortBy { it.size }
            "by_date" -> fileListShare.sortBy { it.lastModified }
        }
        if(isCheck){

            fileShareAdapter.updateFiles(fileListShare)
        }else{

            fileShareFileAdapter.updateFiles(fileListShare)
        }
         // Update adapter to reflect changes
    }
    private fun saveSortOption(sortOption: String) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("sort_option", sortOption).apply()

        // Broadcast the sort option
        val intent = Intent(ACTION_SORT_FILES)
        intent.putExtra("sort_option", sortOption)
        requireContext().sendBroadcast(intent)
    }
}

