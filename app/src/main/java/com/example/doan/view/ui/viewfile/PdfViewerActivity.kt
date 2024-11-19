package com.example.doan.view.ui.viewfile

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.R
import com.example.doan.databinding.ActivityPdfViewerBinding
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var fileDescriptor: ParcelFileDescriptor

    private var currentPageIndex: Int = 0
    private var totalPageCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("FILE_PATH") ?: run {
            Log.e("PdfViewerActivity", "FILE_PATH is null")
            finish()
            return
        }

        val pdfFile = File(filePath)
        if (!pdfFile.exists()) {
            Log.e("PdfViewerActivity", "PDF file does not exist at: $filePath")
            finish()
            return
        }

        try {
            fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            totalPageCount = pdfRenderer.pageCount // Get the total number of pages
        } catch (e: Exception) {
            Log.e("PdfViewerActivity", "Error initializing PdfRenderer", e)
            finish()
            return
        }

        showPage(currentPageIndex) // Show the first page

        binding.nextPageButton.setOnClickListener {
            val nextPageIndex = currentPageIndex + 1
            if (nextPageIndex < totalPageCount) {
                showPage(nextPageIndex)
            }
        }

        binding.prevPageButton.setOnClickListener {
            val prevPageIndex = currentPageIndex - 1
            if (prevPageIndex >= 0) {
                showPage(prevPageIndex)
            }
        }
    }

    private fun showPage(index: Int) {
        try {
            if (::currentPage.isInitialized) currentPage.close()
            currentPage = pdfRenderer.openPage(index)

            val bitmap = Bitmap.createBitmap(
                currentPage.width,
                currentPage.height,
                Bitmap.Config.ARGB_8888
            )
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            binding.pdfImageView.setImageBitmap(bitmap)

            // Update the current page index and total page count display
            currentPageIndex = index
            binding.pageNumberText.text = "${currentPageIndex + 1} / $totalPageCount" // Display page number

            // Disable/Enable buttons based on current page
            updateNavigationButtons()

        } catch (e: Exception) {
            Log.e("PdfViewerActivity", "Error displaying page", e)
        }
    }

    private fun updateNavigationButtons() {
        binding.btnBack.setOnClickListener { finish() }
        // Disable the "Previous" button on the first page
        binding.prevPageButton.isEnabled = currentPageIndex > 0
        if (currentPageIndex == 0) {
            binding.prevPageButton.setColorFilter(getColor(android.R.color.darker_gray)) // Gray color for first page
        } else {
            binding.prevPageButton.setColorFilter(getColor(R.color.Main)) // Black color for other pages
        }

        // Disable the "Next" button on the last page
        binding.nextPageButton.isEnabled = currentPageIndex < totalPageCount - 1
        if (currentPageIndex == totalPageCount - 1) {
            binding.nextPageButton.setColorFilter(getColor(android.R.color.darker_gray)) // Gray color for last page
        } else {
            binding.nextPageButton.setColorFilter(getColor(R.color.Main)) // Black color for other pages
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::currentPage.isInitialized) currentPage.close()
            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            Log.e("PdfViewerActivity", "Error closing resources", e)
        }
    }
}
