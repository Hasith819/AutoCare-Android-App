package com.nibm.autocare

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nibm.autocare.Vehicle.AddVehicleActivity


class AddServiceActivity : AppCompatActivity() {

    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button
    private lateinit var lvPictures: ListView

    private val REQUEST_CODE_CAMERA = 100
    private val REQUEST_CODE_GALLERY = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        btnGallery = findViewById(R.id.btnGallery)
        btnCamera = findViewById(R.id.btnCamera)
        lvPictures = findViewById(R.id.lvPictures)

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA)
            } else {
                openCamera()
            }
        }

        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_GALLERY)
        }

        findViewById<View>(R.id.llHome).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }


        findViewById<View>(R.id.llAddVehicle).setOnClickListener {
            val intent = Intent(this, AddVehicleActivity::class.java)
            startActivity(intent)
        }


    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAMERA -> {
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    // Handle the captured photo here
                    // For example, you can add it to a list or display it in an ImageView
                }
                REQUEST_CODE_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    // Handle the selected image from gallery here
                    // For example, you can upload it or display it in an ImageView
                }
            }
        }
    }
}