package com.example.doan.view.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.FileCloud
import com.example.doan.Adapter.FileUpAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FileCommunityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileUpAdapter: FileUpAdapter  // Adapter to display the files
    private var fileList: List<FileCloud> = emptyList()  // List to hold FileCloud objects
    private lateinit var progressBar: ProgressBar  // Progress bar for loading indication

    private val database = FirebaseDatabase.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val userRef = userId?.let { database.getReference("users").child(it).child("listAppUp") }

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

        // Fetch the list of files from Firebase
        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Hide the ProgressBar once data is loaded
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

                        // Create a FileCloud object and add it to the list
                        val file = FileCloud(name, size, type, path, lastModified, downloadUrl, fileId)
                        filesList.add(file)
                    }
                    fileList = filesList

                    // Initialize RecyclerView and Adapter with data
                    recyclerView = view.findViewById(R.id.rcv_file_Up)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    fileUpAdapter = FileUpAdapter(fileList)
                    recyclerView.adapter = fileUpAdapter

                    // Show a Toast message with the number of files
                    Toast.makeText(context, "Fetched ${filesList.size} files", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No files found in listAppUp", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hide the ProgressBar if there's an error
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to fetch files: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        return view
    }
}
