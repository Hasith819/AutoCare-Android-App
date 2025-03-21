package com.nibm.autocare.Vehicle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.HomeActivity
import com.nibm.autocare.R

class AddVehicleActivity : AppCompatActivity() {

    private lateinit var spinnerBrand: Spinner
    private lateinit var spinnerModel: Spinner
    private lateinit var btnHome: LinearLayout

    private val database = FirebaseDatabase.getInstance()
    private val brandsRef = database.reference.child("vehicles").child("brands")

    private val brandList = mutableListOf<String>()
    private val modelList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_vehicle)

        // Initialize views
        spinnerBrand = findViewById(R.id.spinnerBrand)
        spinnerModel = findViewById(R.id.spinnerModel)
        btnHome = findViewById(R.id.llHome)

        // Load brands into the brand spinner
        loadBrands()

        // Set up brand spinner item selection listener
        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedBrand = brandList[position]
                loadModels(selectedBrand)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set up home button click listener
        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity
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
}