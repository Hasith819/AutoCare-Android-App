package com.nibm.autocare

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nibm.autocare.Vehicle.AddVehicleActivity
import com.nibm.autocare.adapter.UploadedPhotosAdapter
import java.text.SimpleDateFormat
import java.util.*

class AddServiceActivity : AppCompatActivity() {

    // Declare UI components
    private lateinit var spinnerServiceType: Spinner
    private lateinit var spinnerVehicle: Spinner
    private lateinit var etServiceDate: EditText
    private lateinit var cbEngineOilChange: CheckBox
    private lateinit var cbOilFilterReplace: CheckBox
    private lateinit var cbFluidLevelChecks: CheckBox
    private lateinit var cbTireInspection: CheckBox
    private lateinit var cbBrakeSystemCheck: CheckBox
    private lateinit var cbLightsElectricalsCheck: CheckBox
    private lateinit var cbAirFilterInspection: CheckBox
    private lateinit var cbWheelAlignmentCheck: CheckBox
    private lateinit var cbCabinFilterChange: CheckBox
    private lateinit var cbFuelFilterInspection: CheckBox
    private lateinit var cbBrakeFluidFlush: CheckBox
    private lateinit var cbCoolantFluidFlush: CheckBox
    private lateinit var cbTransmissionOilChange: CheckBox
    private lateinit var cbAirConditioningSystemCheck: CheckBox
    private lateinit var cbTimingBeltChainReplacement: CheckBox
    private lateinit var cbSparkPlugReplacement: CheckBox
    private lateinit var cbSuspensionComponentCheck: CheckBox
    private lateinit var cbDriveBeltReplacement: CheckBox
    private lateinit var cbFuelSystemService: CheckBox
    private lateinit var rvUploadedPhotos: RecyclerView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Adapter for uploaded photos
    private lateinit var uploadedPhotosAdapter: UploadedPhotosAdapter
    private val uploadedPhotos = mutableListOf<Uri>()

    // Activity result launcher for gallery and camera
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    // Handle multiple photo selection
                    if (data.clipData != null) {
                        val clipData = data.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            val photoUri = clipData.getItemAt(i).uri
                            uploadedPhotos.add(photoUri)
                        }
                    } else if (data.data != null) {
                        // Handle single photo selection
                        val photoUri = data.data
                        if (photoUri != null) {
                            uploadedPhotos.add(photoUri)
                        }
                    }
                    uploadedPhotosAdapter.notifyDataSetChanged()
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                cameraImageUri?.let { uri ->
                    uploadedPhotos.add(uri)
                    uploadedPhotosAdapter.notifyDataSetChanged()
                    cameraImageUri = null
                }
            }
        }

    private var cameraImageUri: Uri? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize UI components
        spinnerServiceType = findViewById(R.id.spinnerServiceType)
        spinnerVehicle = findViewById(R.id.spinnerVehicle)
        etServiceDate = findViewById(R.id.etServiceDate)
        cbEngineOilChange = findViewById(R.id.cbEngineOilChange)
        cbOilFilterReplace = findViewById(R.id.cbOilFilterReplace)
        cbFluidLevelChecks = findViewById(R.id.cbFluidLevelChecks)
        cbTireInspection = findViewById(R.id.cbTireInspection)
        cbBrakeSystemCheck = findViewById(R.id.cbBrakeSystemCheck)
        cbLightsElectricalsCheck = findViewById(R.id.cbLightsElectricalsCheck)
        cbAirFilterInspection = findViewById(R.id.cbAirFilterInspection)
        cbWheelAlignmentCheck = findViewById(R.id.cbWheelAlignmentCheck)
        cbCabinFilterChange = findViewById(R.id.cbCabinFilterChange)
        cbFuelFilterInspection = findViewById(R.id.cbFuelFilterInspection)
        cbBrakeFluidFlush = findViewById(R.id.cbBrakeFluidFlush)
        cbCoolantFluidFlush = findViewById(R.id.cbCoolantFluidFlush)
        cbTransmissionOilChange = findViewById(R.id.cbTransmissionOilChange)
        cbAirConditioningSystemCheck = findViewById(R.id.cbAirConditioningSystemCheck)
        cbTimingBeltChainReplacement = findViewById(R.id.cbTimingBeltChainReplacement)
        cbSparkPlugReplacement = findViewById(R.id.cbSparkPlugReplacement)
        cbSuspensionComponentCheck = findViewById(R.id.cbSuspensionComponentCheck)
        cbDriveBeltReplacement = findViewById(R.id.cbDriveBeltReplacement)
        cbFuelSystemService = findViewById(R.id.cbFuelSystemService)
        rvUploadedPhotos = findViewById(R.id.rvUploadedPhotos)

        // Set up RecyclerView for uploaded photos
        uploadedPhotosAdapter = UploadedPhotosAdapter(uploadedPhotos) { position ->
            uploadedPhotos.removeAt(position)
            uploadedPhotosAdapter.notifyDataSetChanged()
        }
        rvUploadedPhotos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvUploadedPhotos.adapter = uploadedPhotosAdapter

        // Set up navigation
        findViewById<View>(R.id.llHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<View>(R.id.llAddVehicle).setOnClickListener {
            startActivity(Intent(this, AddVehicleActivity::class.java))
        }


        // Fetch and populate the logged-in user's vehicles
        fetchUserVehicles()

        // Set up the service type spinner
        setupServiceTypeSpinner()

        // Set up the date picker
        setupDatePicker()

        // Set up gallery button
        findViewById<View>(R.id.btnGallery).setOnClickListener {
            openGallery()
        }

        // Set up camera button
        findViewById<View>(R.id.btnCamera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                openCamera()
            }
        }
    }

    // Open gallery for photo selection
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryLauncher.launch(intent)
    }

    // Open camera for photo capture
    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        cameraImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    // Fetch the logged-in user's vehicles from Firebase
    private fun fetchUserVehicles() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val vehiclesRef = database.reference.child("users_vehicles").child(userId)

            vehiclesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val vehicleList = mutableListOf<String>()
                    for (vehicleSnapshot in snapshot.children) {
                        val registrationNumber = vehicleSnapshot.child("registrationNumber").getValue(String::class.java)
                        if (registrationNumber != null) {
                            vehicleList.add(registrationNumber)
                        }
                    }

                    // Populate the spinner with the vehicle registration numbers
                    val adapter = ArrayAdapter(this@AddServiceActivity, android.R.layout.simple_spinner_item, vehicleList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerVehicle.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AddServiceActivity, "Failed to fetch vehicles: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Set up the service type spinner
    private fun setupServiceTypeSpinner() {
        val serviceTypes = listOf("5,000 km Service", "40,000 km Service", "100,000 km Service")
        val serviceChecklists = mapOf(
            "5,000 km Service" to listOf(
                cbEngineOilChange,
                cbOilFilterReplace,
                cbFluidLevelChecks,
                cbTireInspection,
                cbBrakeSystemCheck,
                cbLightsElectricalsCheck,
                cbAirFilterInspection,
                cbWheelAlignmentCheck
            ),
            "40,000 km Service" to listOf(
                cbEngineOilChange,
                cbOilFilterReplace,
                cbAirFilterInspection,
                cbCabinFilterChange,
                cbFuelFilterInspection,
                cbBrakeFluidFlush,
                cbCoolantFluidFlush,
                cbTransmissionOilChange,
                cbTireInspection,
                cbBrakeSystemCheck,
                cbLightsElectricalsCheck,
                cbAirConditioningSystemCheck,
                cbWheelAlignmentCheck
            ),
            "100,000 km Service" to listOf(
                cbTimingBeltChainReplacement,
                cbSparkPlugReplacement,
                cbSuspensionComponentCheck,
                cbDriveBeltReplacement,
                cbFuelSystemService,
                cbEngineOilChange,
                cbOilFilterReplace,
                cbAirFilterInspection,
                cbCabinFilterChange,
                cbFuelFilterInspection,
                cbBrakeFluidFlush,
                cbCoolantFluidFlush,
                cbTransmissionOilChange,
                cbAirConditioningSystemCheck,
                cbTireInspection,
                cbBrakeSystemCheck,
                cbLightsElectricalsCheck,
                cbWheelAlignmentCheck
            )
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerServiceType.adapter = adapter

        spinnerServiceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedService = serviceTypes[position]
                updateChecklist(serviceChecklists[selectedService] ?: emptyList())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    // Update the checklist based on the selected service type
    private fun updateChecklist(checklist: List<CheckBox>) {
        // Hide all checkboxes initially
        listOf(
            cbEngineOilChange,
            cbOilFilterReplace,
            cbFluidLevelChecks,
            cbTireInspection,
            cbBrakeSystemCheck,
            cbLightsElectricalsCheck,
            cbAirFilterInspection,
            cbWheelAlignmentCheck,
            cbCabinFilterChange,
            cbFuelFilterInspection,
            cbBrakeFluidFlush,
            cbCoolantFluidFlush,
            cbTransmissionOilChange,
            cbAirConditioningSystemCheck,
            cbTimingBeltChainReplacement,
            cbSparkPlugReplacement,
            cbSuspensionComponentCheck,
            cbDriveBeltReplacement,
            cbFuelSystemService
        ).forEach { it.visibility = View.GONE }

        // Show the checkboxes for the selected service type
        checklist.forEach { it.visibility = View.VISIBLE }
    }

    // Set up the date picker
    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            calendar.set(year, month, day)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etServiceDate.setText(dateFormat.format(calendar.time))
        }

        etServiceDate.setOnClickListener {
            DatePickerDialog(
                this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}