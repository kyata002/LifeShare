package com.example.doan.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.Adapter.FileDeviceAdapter
import com.example.doan.R
import com.example.doan.const.Companion.ACTION_FILE_DELETED
import com.example.doan.viewmodel.SplashViewModel
import java.io.File

class FileDeviceFragment : Fragment() {

    private lateinit var viewModel: SplashViewModel
    private lateinit var fileAdapter: FileDeviceAdapter

    // A list to hold the files that are currently in the ViewModel
    private var fileList = mutableListOf<File>()

    // Define BroadcastReceiver as a member variable
    private val fileDeletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent?.action == ACTION_FILE_DELETED) {
                val position = intent.getIntExtra("FILE_POSITION", -1)
                if (position != -1 && position < fileList.size) {
                    fileList.removeAt(position)
                    fileAdapter.updateFiles(fileList)
//                    recyclerView.adapter = fileAdapter
                }
            }
        }
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
    }

    // Register BroadcastReceiver in onStart
    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(fileDeletedReceiver, IntentFilter(ACTION_FILE_DELETED))
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(fileDeletedReceiver)
    }

}


