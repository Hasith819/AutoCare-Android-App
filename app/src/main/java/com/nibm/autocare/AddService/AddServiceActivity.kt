//package com.nibm.autocare
//
//import android.Manifest
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Button
//import android.widget.ListView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.nibm.autocare.Vehicle.AddVehicleActivity
//
//
//class AddServiceActivity : AppCompatActivity() {
//
//    private lateinit var btnGallery: Button
//    private lateinit var btnCamera: Button
//    private lateinit var lvPictures: ListView
//
//    private val REQUEST_CODE_CAMERA = 100
//    private val REQUEST_CODE_GALLERY = 101
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_service)
//
//        btnGallery = findViewById(R.id.btnGallery)
//        btnCamera = findViewById(R.id.btnCamera)
//        lvPictures = findViewById(R.id.lvPictures)
//
//        btnCamera.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA)
//            } else {
//                openCamera()
//            }
//        }
//
//        btnGallery.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, REQUEST_CODE_GALLERY)
//        }
//
//        findViewById<View>(R.id.llHome).setOnClickListener {
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//        }
//
//
//        findViewById<View>(R.id.llAddVehicle).setOnClickListener {
//            val intent = Intent(this, AddVehicleActivity::class.java)
//            startActivity(intent)
//        }
//
//
//    }
//
//    private fun openCamera() {
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            openCamera()
//        } else {
//            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                REQUEST_CODE_CAMERA -> {
//                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
//                    // Handle the captured photo here
//                    // For example, you can add it to a list or display it in an ImageView
//                }
//                REQUEST_CODE_GALLERY -> {
//                    val selectedImage: Uri? = data?.data
//                    // Handle the selected image from gallery here
//                    // For example, you can upload it or display it in an ImageView
//                }
//            }
//        }
//    }
//}


package com.nibm.autocare

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibm.autocare.Vehicle.AddVehicleActivity
import com.nibm.autocare.adapter.UploadedPhotosAdapter
import java.io.File
import java.io.IOException

class AddServiceActivity : AppCompatActivity() {

    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button
    private lateinit var rvUploadedPhotos: RecyclerView

    private val REQUEST_CODE_CAMERA = 100
    private val REQUEST_CODE_GALLERY = 101

    private lateinit var uploadedPhotosAdapter: UploadedPhotosAdapter
    private val uploadedPhotos = mutableListOf<Uri>()

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        btnGallery = findViewById(R.id.btnGallery)
        btnCamera = findViewById(R.id.btnCamera)
        rvUploadedPhotos = findViewById(R.id.rvUploadedPhotos)

        // Initialize RecyclerView
        rvUploadedPhotos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        uploadedPhotosAdapter = UploadedPhotosAdapter(uploadedPhotos) { position ->
            // Remove photo from the list
            uploadedPhotos.removeAt(position)
            uploadedPhotosAdapter.notifyDataSetChanged()
        }
        rvUploadedPhotos.adapter = uploadedPhotosAdapter

        // Set up camera button click listener
        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_CAMERA)
            } else {
                openCamera()
            }
        }

        // Set up gallery button click listener
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_CODE_GALLERY)
        }


        findViewById<View>(R.id.llHome).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.llAddVehicle).setOnClickListener {
            val intent = Intent(this, AddVehicleActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.llAddService).setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            startActivity(intent)
        }


    }

    // Open the camera to take a picture
    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider", // Authority must match the one in AndroidManifest.xml
                photoFile
            )

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

            // Grant URI permissions
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
            } else {
                Toast.makeText(this, "No Camera app found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e("AddServiceActivity", "Failed to create image file", e)
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("AddServiceActivity", "An error occurred while opening the camera", e)
            Toast.makeText(this, "An error occurred while opening the camera", Toast.LENGTH_SHORT).show()
        }
    }

    // Create a file to save the camera photo
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera and Storage permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the result from camera or gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAMERA -> {
                    if (photoUri != null) {
                        uploadedPhotos.add(photoUri!!)
                        uploadedPhotosAdapter.notifyDataSetChanged()
                    }
                }
                REQUEST_CODE_GALLERY -> {
                    val clipData = data?.clipData
                    if (clipData != null) {
                        // Multiple images selected
                        for (i in 0 until clipData.itemCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            uploadedPhotos.add(imageUri)
                        }
                    } else if (data?.data != null) {
                        // Single image selected
                        val imageUri = data.data
                        if (imageUri != null) {
                            uploadedPhotos.add(imageUri)
                        }
                    }
                    uploadedPhotosAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}