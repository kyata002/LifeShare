package com.example.doan.view.ui.fragment

import FileApp
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.Adapter.FileDeviceAdapter
import com.example.doan.R
import com.example.doan.const.Companion.ACTION_FILE_DELETED
import com.example.doan.const.Companion.ACTION_FILE_RENAME
import com.example.doan.const.Companion.ACTION_SORT_FILES
import com.example.doan.view.viewmodel.SplashViewModel
import java.io.File

class FileDeviceFragment : Fragment() {

    private lateinit var viewModel: SplashViewModel
    private lateinit var fileAdapter: FileDeviceAdapter
    private lateinit var btnSort: ImageView

    // A list to hold the files that are currently in the ViewModel
    private var fileList = mutableListOf<FileApp>()

    // Define BroadcastReceiver as a member variable
    private val fileDeletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FILE_DELETED) {
                val position = intent.getIntExtra("FILE_POSITION", -1)
                if (position != -1 && position < fileList.size) {
                    fileList.removeAt(position)
                    fileAdapter.updateFiles(fileList)
                }
            }
        }
    }

    private val fileRenamedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FILE_RENAME) {
                val position = intent.getIntExtra("FILE_POSITION", -1)
                val newFileName = intent.getStringExtra("NEW_FILE_NAME")

                if (position != -1 && newFileName != null) {
                    val oldFileApp = fileList.getOrNull(position)
                    oldFileApp?.let {
                        // Create a new File with the updated name
                        val newFile = File(it.path).parentFile?.let { parent ->
                            File(parent, newFileName)
                        }

                        // If the renamed file was successfully created, update fileList
                        newFile?.let { updatedFile ->
                            fileList[position] = FileApp(
                                name = updatedFile.name,
                                size = updatedFile.length(),
                                type = updatedFile.extension,
                                path = updatedFile.absolutePath,
                                lastModified = updatedFile.lastModified()
                            )
                            fileAdapter.updateFiles(fileList) // Notify adapter to update RecyclerView
                        }
                    }
                }
            }
        }
    }

    private val sortReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SORT_FILES) {
                val sortOption = intent.getStringExtra("sort_option")
                sortFileList(sortOption)
            }
        }
    }

    private fun sortFileList(sortOption: String?) {
        when (sortOption) {
            "a_to_z" -> fileList.sortBy { it.name }
            "z_to_a" -> fileList.sortByDescending { it.name }
            "by_size" -> fileList.sortBy { it.size }
            "by_date" -> fileList.sortBy { it.lastModified }
        }
        fileAdapter.updateFiles(fileList) // Update adapter to reflect changes
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.rcv_file_device)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter and set it to RecyclerView
        fileAdapter = FileDeviceAdapter(fileList)
        recyclerView.adapter = fileAdapter

        // Initialize ViewModel and observe the files LiveData
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        viewModel.files.observe(viewLifecycleOwner) { files ->
            Log.d("FileDeviceFragment", "Observed files: ${files.size}")
            fileList = files.toMutableList()
            fileAdapter.updateFiles(fileList)
        }

        // Fetch the file list from ViewModel
        viewModel.getFileList()

        // Initialize sort button
        btnSort = view.findViewById(R.id.btn_sort)
        btnSort.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), btnSort)
            popupMenu.menuInflater.inflate(R.menu.popup_sort, popupMenu.menu)

            // Retrieve saved sort option from SharedPreferences
            val savedSortOption = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
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
            popupMenu.show()
        }
    }

    private fun saveSortOption(sortOption: String) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("sort_option", sortOption).apply()

        // Broadcast the sort option
        val intent = Intent(ACTION_SORT_FILES)
        intent.putExtra("sort_option", sortOption)
        requireContext().sendBroadcast(intent)
    }

    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(fileDeletedReceiver, IntentFilter(ACTION_FILE_DELETED))
        requireContext().registerReceiver(fileRenamedReceiver, IntentFilter(ACTION_FILE_RENAME))
//        requireContext().registerReceiver(sortReceiver, IntentFilter(ACTION_SORT_FILES))
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(fileDeletedReceiver)
        requireContext().unregisterReceiver(fileRenamedReceiver)
//        requireContext().unregisterReceiver(sortReceiver)
    }
}
