package com.nibm.autocare

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.Vehicle.AddVehicleActivity
import com.nibm.autocare.adapter.UploadedPhotosAdapter
import java.text.SimpleDateFormat
import java.util.*

class AddServiceActivity : AppCompatActivity() {

    // UI Components
    private lateinit var spinnerServiceType: Spinner
    private lateinit var spinnerVehicle: Spinner
    private lateinit var etServiceDate: EditText
    private lateinit var etOdometerReading: EditText
    private lateinit var etServiceCost: EditText
    private lateinit var etServiceNotes: EditText
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
    private lateinit var btnSave: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Photo handling
    private lateinit var uploadedPhotosAdapter: UploadedPhotosAdapter
    private val uploadedPhotos = mutableListOf<Uri>()
    private var cameraImageUri: Uri? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    // Progress tracking
    private lateinit var progressDialog: AlertDialog
    private var totalPhotosToUpload = 0
    private var uploadedPhotoCount = 0

    // Activity result launchers
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                if (data.clipData != null) {
                    // Multiple photos selected
                    val clipData = data.clipData
                    for (i in 0 until clipData!!.itemCount) {
                        uploadedPhotos.add(clipData.getItemAt(i).uri)
                    }
                } else {
                    // Single photo selected
                    data.data?.let { uri -> uploadedPhotos.add(uri) }
                }
                uploadedPhotosAdapter.notifyDataSetChanged()
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            cameraImageUri?.let {
                uploadedPhotos.add(it)
                uploadedPhotosAdapter.notifyDataSetChanged()
                cameraImageUri = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        initializeFirebase()
        initializeUIComponents()
        setupProgressDialog()
        setupNavigation()
        fetchUserVehicles()
        setupServiceTypeSpinner()
        setupDatePicker()
        setupPhotoButtons()
        setupSaveButton()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initializeUIComponents() {
        // Initialize all UI components
        spinnerServiceType = findViewById(R.id.spinnerServiceType)
        spinnerVehicle = findViewById(R.id.spinnerVehicle)
        etServiceDate = findViewById(R.id.etServiceDate)
        etOdometerReading = findViewById(R.id.etOdometerReading)
        etServiceCost = findViewById(R.id.etServiceCost)
        etServiceNotes = findViewById(R.id.etServiceNotes)
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
        btnSave = findViewById(R.id.btnSave)

        // Setup RecyclerView
        uploadedPhotosAdapter = UploadedPhotosAdapter(uploadedPhotos) { position ->
            uploadedPhotos.removeAt(position)
            uploadedPhotosAdapter.notifyDataSetChanged()
        }
        rvUploadedPhotos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvUploadedPhotos.adapter = uploadedPhotosAdapter
    }

    private fun setupProgressDialog() {
        progressDialog = AlertDialog.Builder(this)
            .setTitle("Uploading Photos")
            .setView(R.layout.dialog_upload_progress)
            .setCancelable(false)
            .create()
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.llHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<View>(R.id.llAddVehicle).setOnClickListener {
            startActivity(Intent(this, AddVehicleActivity::class.java))
        }
    }

    private fun fetchUserVehicles() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        val vehiclesRef = database.reference.child("users_vehicles").child(userId)

        vehiclesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vehicleList = mutableListOf<String>()
                snapshot.children.forEach { vehicleSnapshot ->
                    vehicleSnapshot.child("registrationNumber").getValue(String::class.java)?.let {
                        vehicleList.add(it)
                    }
                }

                val adapter = ArrayAdapter(
                    this@AddServiceActivity,
                    android.R.layout.simple_spinner_item,
                    vehicleList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerVehicle.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AddServiceActivity,
                    "Failed to fetch vehicles: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupServiceTypeSpinner() {
        val serviceTypes = listOf("5,000 km Service", "40,000 km Service", "100,000 km Service")
        val serviceChecklists = mapOf(
            "5,000 km Service" to listOf(
                cbEngineOilChange, cbOilFilterReplace, cbFluidLevelChecks,
                cbTireInspection, cbBrakeSystemCheck, cbLightsElectricalsCheck,
                cbAirFilterInspection, cbWheelAlignmentCheck
            ),
            "40,000 km Service" to listOf(
                cbEngineOilChange, cbOilFilterReplace, cbAirFilterInspection,
                cbCabinFilterChange, cbFuelFilterInspection, cbBrakeFluidFlush,
                cbCoolantFluidFlush, cbTransmissionOilChange, cbTireInspection,
                cbBrakeSystemCheck, cbLightsElectricalsCheck, cbAirConditioningSystemCheck,
                cbWheelAlignmentCheck
            ),
            "100,000 km Service" to listOf(
                cbTimingBeltChainReplacement, cbSparkPlugReplacement, cbSuspensionComponentCheck,
                cbDriveBeltReplacement, cbFuelSystemService, cbEngineOilChange,
                cbOilFilterReplace, cbAirFilterInspection, cbCabinFilterChange,
                cbFuelFilterInspection, cbBrakeFluidFlush, cbCoolantFluidFlush,
                cbTransmissionOilChange, cbAirConditioningSystemCheck, cbTireInspection,
                cbBrakeSystemCheck, cbLightsElectricalsCheck, cbWheelAlignmentCheck
            )
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerServiceType.adapter = adapter

        spinnerServiceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateChecklist(serviceChecklists[serviceTypes[position]] ?: emptyList())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateChecklist(checklist: List<CheckBox>) {
        listOf(
            cbEngineOilChange, cbOilFilterReplace, cbFluidLevelChecks, cbTireInspection,
            cbBrakeSystemCheck, cbLightsElectricalsCheck, cbAirFilterInspection, cbWheelAlignmentCheck,
            cbCabinFilterChange, cbFuelFilterInspection, cbBrakeFluidFlush, cbCoolantFluidFlush,
            cbTransmissionOilChange, cbAirConditioningSystemCheck, cbTimingBeltChainReplacement,
            cbSparkPlugReplacement, cbSuspensionComponentCheck, cbDriveBeltReplacement, cbFuelSystemService
        ).forEach { it.visibility = View.GONE }

        checklist.forEach { it.visibility = View.VISIBLE }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            etServiceDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time))
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

    private fun setupPhotoButtons() {
        findViewById<View>(R.id.btnGallery).setOnClickListener { openGallery() }
        findViewById<View>(R.id.btnCamera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                openCamera()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        cameraImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
            cameraLauncher.launch(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveServiceData()
            }
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            etServiceDate.text.toString().trim().isEmpty() -> {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                false
            }
            etOdometerReading.text.toString().trim().isEmpty() -> {
                Toast.makeText(this, "Please enter odometer reading", Toast.LENGTH_SHORT).show()
                false
            }
            etServiceCost.text.toString().trim().isEmpty() -> {
                Toast.makeText(this, "Please enter service cost", Toast.LENGTH_SHORT).show()
                false
            }
            getCheckedItems().size < 3 -> {
                Toast.makeText(this, "Please select at least 3 services", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun getCheckedItems(): List<String> {
        val checkedItems = mutableListOf<String>()
        if (cbEngineOilChange.isChecked) checkedItems.add("Engine Oil Change")
        if (cbOilFilterReplace.isChecked) checkedItems.add("Oil Filter Replace")
        if (cbFluidLevelChecks.isChecked) checkedItems.add("Fluid Level Checks")
        if (cbTireInspection.isChecked) checkedItems.add("Tire Inspection")
        if (cbBrakeSystemCheck.isChecked) checkedItems.add("Brake System Check")
        if (cbLightsElectricalsCheck.isChecked) checkedItems.add("Lights and Electricals Check")
        if (cbAirFilterInspection.isChecked) checkedItems.add("Air Filter Inspection")
        if (cbWheelAlignmentCheck.isChecked) checkedItems.add("Wheel Alignment Check")
        if (cbCabinFilterChange.isChecked) checkedItems.add("Cabin Filter Change")
        if (cbFuelFilterInspection.isChecked) checkedItems.add("Fuel Filter Inspection")
        if (cbBrakeFluidFlush.isChecked) checkedItems.add("Brake Fluid Flush")
        if (cbCoolantFluidFlush.isChecked) checkedItems.add("Coolant Fluid Flush")
        if (cbTransmissionOilChange.isChecked) checkedItems.add("Transmission Oil Change")
        if (cbAirConditioningSystemCheck.isChecked) checkedItems.add("Air Conditioning System Check")
        if (cbTimingBeltChainReplacement.isChecked) checkedItems.add("Timing Belt/Chain Replacement")
        if (cbSparkPlugReplacement.isChecked) checkedItems.add("Spark Plug Replacement")
        if (cbSuspensionComponentCheck.isChecked) checkedItems.add("Suspension Component Check")
        if (cbDriveBeltReplacement.isChecked) checkedItems.add("Drive Belt Replacement")
        if (cbFuelSystemService.isChecked) checkedItems.add("Fuel System Service")
        return checkedItems
    }

    private fun saveServiceData() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val registrationNumber = spinnerVehicle.selectedItem?.toString() ?: run {
            Toast.makeText(this, "Please select a vehicle", Toast.LENGTH_SHORT).show()
            return
        }

        val date = etServiceDate.text.toString().trim()
        val odometerReading = etOdometerReading.text.toString().trim()
        val serviceType = spinnerServiceType.selectedItem.toString()
        val serviceCost = etServiceCost.text.toString().trim()
        val notes = etServiceNotes.text.toString().trim()
        val checkedItems = getCheckedItems()
        val dateKey = date.replace("/", "-")

        val serviceData = hashMapOf(
            "date" to date,
            "odometerReading" to odometerReading,
            "serviceType" to serviceType,
            "serviceCost" to serviceCost,
            "notes" to notes,
            "checkedItems" to checkedItems
        )

        val serviceRef = database.reference.child("users_services")
            .child(userId)
            .child(registrationNumber)
            .child(dateKey)

        serviceRef.setValue(serviceData)
            .addOnSuccessListener {
                if (uploadedPhotos.isNotEmpty()) {
                    uploadPhotosToCloudinary(userId, registrationNumber, dateKey)
                } else {
                    navigateToHome()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save service data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPhotosToCloudinary(userId: String, registrationNumber: String, dateKey: String) {
        totalPhotosToUpload = uploadedPhotos.size
        uploadedPhotoCount = 0
        val photoUrls = mutableListOf<String>()

        progressDialog.show()
        progressDialog.findViewById<TextView>(R.id.tvUploadStatus)?.text =
            "Uploading 0 of $totalPhotosToUpload photos..."

        uploadedPhotos.forEachIndexed { index, uri ->
            MediaManager.get().upload(uri)
                .option("folder", "Home/AutoCare")
                .option("public_id", "service_${userId}_${registrationNumber}_${dateKey}_$index")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        runOnUiThread {
                            progressDialog.findViewById<TextView>(R.id.tvUploadStatus)?.text =
                                "Uploading photo ${index + 1} of $totalPhotosToUpload..."
                        }
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        runOnUiThread {
                            progressDialog.findViewById<ProgressBar>(R.id.progressBar)?.apply {
                                isIndeterminate = false
                                this.progress = progress
                            }
                        }
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        uploadedPhotoCount++
                        resultData["url"]?.toString()?.let { photoUrls.add(it) }

                        runOnUiThread {
                            progressDialog.findViewById<TextView>(R.id.tvUploadStatus)?.text =
                                "Uploaded $uploadedPhotoCount of $totalPhotosToUpload photos..."
                        }

                        if (uploadedPhotoCount == totalPhotosToUpload) {
                            savePhotoUrls(userId, registrationNumber, dateKey, photoUrls)
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        uploadedPhotoCount++
                        Log.e("Cloudinary", "Upload failed: ${error.description}")
                        runOnUiThread {
                            Toast.makeText(
                                this@AddServiceActivity,
                                "Failed to upload photo ${index + 1}",
                                Toast.LENGTH_SHORT
                            ).show()

                            if (uploadedPhotoCount == totalPhotosToUpload) {
                                if (photoUrls.isNotEmpty()) {
                                    savePhotoUrls(userId, registrationNumber, dateKey, photoUrls)
                                } else {
                                    progressDialog.dismiss()
                                    navigateToHome()
                                }
                            }
                        }
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("Cloudinary", "Upload rescheduled for photo ${index + 1}")
                    }
                })
                .dispatch()
        }
    }

    private fun savePhotoUrls(userId: String, registrationNumber: String, dateKey: String, photoUrls: List<String>) {
        progressDialog.findViewById<TextView>(R.id.tvUploadStatus)?.text = "Saving to database..."

        database.reference.child("users_services")
            .child(userId)
            .child(registrationNumber)
            .child(dateKey)
            .child("photoUrls")
            .setValue(photoUrls)
            .addOnSuccessListener {
                progressDialog.dismiss()
                navigateToHome()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to save photo URLs: ${e.message}", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}