package com.example.doan.view.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file_share, container, false)

        // Initialize UI components
        progressBar = view.findViewById(R.id.progressBarS)
        rcvFileShare = view.findViewById(R.id.rcv_file_Share)
        rcvFileCommunity = view.findViewById(R.id.rcv_file_Community)
        tvCom = view.findViewById(R.id.btn_community)
        tvShare = view.findViewById(R.id.btn_data_share)
        refreshFileList1()

        tvShare.setOnClickListener {
            tvCom.setBackgroundResource(R.drawable.bg_button_false)
            tvCom.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvShare.setBackgroundResource(R.drawable.bg_button_true)
            tvShare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            rcvFileShare.visibility=View.VISIBLE
            rcvFileCommunity.visibility=View.GONE
            refreshFileList()
        }

        tvCom.setOnClickListener {
            tvCom.setBackgroundResource(R.drawable.bg_button_true)
            tvCom.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvShare.setBackgroundResource(R.drawable.bg_button_false)
            tvShare.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            rcvFileShare.visibility=View.GONE
            rcvFileCommunity.visibility=View.VISIBLE
            refreshFileList1()
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
                            val file = FileCloud(name, size, type, path, lastModified, downloadUrl, location, fileId)
                            filesList.add(file)
                        }

                        if (filesList.isNotEmpty()) {
                            fileListShare = filesList

                            // Initialize RecyclerView and Adapter with data
                            rcvFileShare = view!!.findViewById(R.id.rcv_file_Share)
                            rcvFileShare.layoutManager = LinearLayoutManager(context)

                            fileShareFileAdapter = FileShareFileAdapter(fileListShare)
                            rcvFileShare.adapter = fileShareFileAdapter

                            // Show a Toast message with the number of files
                            Toast.makeText(context, "Fetched ${filesList.size} files", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No files found in listAppUp", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No files found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to load files: ${error.message}")
                }
            })
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "No authenticated user found.", Toast.LENGTH_SHORT).show()
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
                            val file = FileCloud(name, size, type, path, lastModified, downloadUrl, location, fileId)
                            filesList.add(file)
                        }

                        if (filesList.isNotEmpty()) {
                            fileListShare = filesList

                            // Initialize RecyclerView and Adapter with data
                            rcvFileCommunity = view!!.findViewById(R.id.rcv_file_Community)
                            rcvFileCommunity.layoutManager = LinearLayoutManager(context)

                            fileShareAdapter = FileCommunityAdapter(fileListShare)
                            rcvFileCommunity.adapter = fileShareAdapter

                            // Show a Toast message with the number of files
                            Toast.makeText(context, "Fetched ${filesList.size} files", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No files found in listAppUp", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No files found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to load files: ${error.message}")
                }
            })
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "No authenticated user found.", Toast.LENGTH_SHORT).show()
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
}

