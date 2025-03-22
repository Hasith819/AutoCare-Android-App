package com.nibm.autocare.Vehicle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.AddServiceActivity
import com.nibm.autocare.HomeActivity
import com.nibm.autocare.R

class AddVehicleActivity : AppCompatActivity() {

    private lateinit var etRegistrationNumber: EditText
    private lateinit var spinnerBrand: Spinner
    private lateinit var spinnerModel: Spinner
    private lateinit var etManufacturedYear: EditText
    private lateinit var etCurrentMileage: EditText
    private lateinit var etWeeklyRidingDistance: EditText
    private lateinit var btnSaveVehicle: Button

    private val database = FirebaseDatabase.getInstance()
    private val brandsRef = database.reference.child("vehicles").child("brands")
    private val auth = FirebaseAuth.getInstance()

    private val brandList = mutableListOf<String>()
    private val modelList = mutableListOf<String>()

    private var selectedBrand: String = ""
    private var selectedModel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_vehicle)

        // Initialize views
        etRegistrationNumber = findViewById(R.id.etRegistrationNumber)
        spinnerBrand = findViewById(R.id.spinnerBrand)
        spinnerModel = findViewById(R.id.spinnerModel)
        etManufacturedYear = findViewById(R.id.etManufacturedYear)
        etCurrentMileage = findViewById(R.id.etCurrentMileage)
        etWeeklyRidingDistance = findViewById(R.id.etWeeklyRidingDistance)
        btnSaveVehicle = findViewById(R.id.btnSaveVehicle)

        // Load brands into the brand spinner
        loadBrands()

        // Set up brand spinner item selection listener
        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBrand = brandList[position]
                loadModels(selectedBrand)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set up model spinner item selection listener
        spinnerModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = modelList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set up save vehicle button click listener
        btnSaveVehicle.setOnClickListener {
            saveVehicle()
        }

        // Set up footer navigation
        findViewById<View>(R.id.llHome).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }


        findViewById<View>(R.id.llAddService).setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            startActivity(intent)
        }
    }

    // Load brands from Firebase Realtime Database
    private fun loadBrands() {
        brandsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                brandList.clear()
                for (brandSnapshot in snapshot.children) {
                    val brand = brandSnapshot.key ?: continue
                    brandList.add(brand)
                }
                // Populate the brand spinner
                val brandAdapter = ArrayAdapter(this@AddVehicleActivity, android.R.layout.simple_spinner_item, brandList)
                brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerBrand.adapter = brandAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddVehicleActivity, "Failed to load brands: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Load models for the selected brand from Firebase Realtime Database
    private fun loadModels(selectedBrand: String) {
        val modelsRef = brandsRef.child(selectedBrand).child("models")
        modelsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                modelList.clear()
                for (modelSnapshot in snapshot.children) {
                    val model = modelSnapshot.getValue(String::class.java) ?: continue
                    modelList.add(model)
                }
                // Populate the model spinner
                val modelAdapter = ArrayAdapter(this@AddVehicleActivity, android.R.layout.simple_spinner_item, modelList)
                modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerModel.adapter = modelAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddVehicleActivity, "Failed to load models: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Save vehicle details to Firebase Realtime Database under "users_vehicles"
    private fun saveVehicle() {
        val registrationNumber = etRegistrationNumber.text.toString().trim()
        val manufacturedYear = etManufacturedYear.text.toString().trim()
        val currentMileage = etCurrentMileage.text.toString().trim()
        val weeklyRidingDistance = etWeeklyRidingDistance.text.toString().trim()

        // Validate all fields
        if (registrationNumber.isEmpty() || selectedBrand.isEmpty() || selectedModel.isEmpty() || manufacturedYear.isEmpty() || currentMileage.isEmpty() || weeklyRidingDistance.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a vehicle object
        val vehicle = HashMap<String, Any>()
        vehicle["registrationNumber"] = registrationNumber
        vehicle["brand"] = selectedBrand
        vehicle["model"] = selectedModel
        vehicle["manufacturedYear"] = manufacturedYear
        vehicle["currentMileage"] = currentMileage.toInt() // Save as integer
        vehicle["weeklyRidingDistance"] = weeklyRidingDistance.toInt() // Save as integer

        // Save vehicle under "users_vehicles" with user ID as a foreign key
        val userId = currentUser.uid
        val usersVehiclesRef = database.reference.child("users_vehicles").child(userId)
        val vehicleId = usersVehiclesRef.push().key // Generate a unique ID for the vehicle

        if (vehicleId != null) {
            usersVehiclesRef.child(vehicleId).setValue(vehicle)
                .addOnSuccessListener {
                    Toast.makeText(this, "Vehicle saved successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Close the current activity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save vehicle: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}