package com.photogalleryapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)

        // Check for permission to read external storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Permission not granted. Requesting permission.")
            requestMediaAccessPermission()
        } else {
            Log.d("MainActivity", "Permission already granted. Loading photos.")
            loadPhotos()
        }
    }

    // Request the READ_MEDIA_IMAGES permission
    private fun requestMediaAccessPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO),
            REQUEST_CODE_READ_EXTERNAL_STORAGE
        )
        Log.d("MainActivity", "Permission request initiated.")
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permission granted. Loading photos.")
                loadPhotos()
            } else {
                Log.d("MainActivity", "Permission denied.")
                Toast.makeText(this, "Permission denied. Unable to load photos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load photos from external storage
    private fun loadPhotos() {
        val photoUris = mutableListOf<Uri>()

        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val photoUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                photoUris.add(photoUri)
            }
        }

        if (photoUris.isEmpty()) {
            Log.d("MainActivity", "No photos found.")
            Toast.makeText(this, "No photos found.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("MainActivity", "Photos loaded: ${photoUris.size}")
            recyclerView.adapter = PhotoGalleryAdapter(photoUris)
        }
    }
}
