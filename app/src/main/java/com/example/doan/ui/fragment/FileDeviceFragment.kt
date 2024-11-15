package com.example.doan.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.Adapter.FileDeviceAdapter
import com.example.doan.R
import com.example.doan.viewmodel.SplashViewModel
import java.io.File

@Suppress("DEPRECATION")
class FileDeviceFragment : Fragment() {

    private lateinit var viewModel: SplashViewModel
    private lateinit var fileAdapter: FileDeviceAdapter

    // A list to hold the files that are currently in the ViewModel
    private var fileList = mutableListOf<File>()

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
    }

    // Handle result from DeleteDialog activity
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FileDeviceAdapter.DELETE_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // If file was deleted, we remove it from the list
                val filePath = data?.getStringExtra("FILE_PATH")
                val fileToRemove = fileList.find { it.absolutePath == filePath }

                fileToRemove?.let {
                    fileList.remove(it)
                    fileAdapter.updateFiles(fileList) // Update the adapter with the modified list
                    Log.d("FileDeviceFragment", "File deleted and list updated")
                }
            } else {
                Log.d("FileDeviceFragment", "File deletion failed or canceled")
            }
        }
    }
}
