package com.nibm.autocare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.Vehicle.AddVehicleActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var lvVehicles: ListView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        tvGreeting = findViewById(R.id.tvGreeting)
        lvVehicles = findViewById(R.id.lvEmails)

        // Fetch and display the username from Firebase Realtime Database
        fetchUsername()

        // Load the user's vehicles into the ListView
        loadUserVehicles()

        // Set up "Add Vehicle" button click listener
        findViewById<View>(R.id.btnSearch).setOnClickListener {
            val intent = Intent(this, AddVehicleActivity::class.java)
            startActivity(intent)
        }

        // Set up footer navigation
        findViewById<View>(R.id.llHome).setOnClickListener {
            // Already in HomeActivity, no action needed
        }


    }

    // Fetch username from Firebase Realtime Database
    private fun fetchUsername() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.reference.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    if (username != null) {
                        tvGreeting.text = "Hello, $username"
                    } else {
                        tvGreeting.text = "Hello, User"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HomeActivity, "Failed to fetch username", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Load the user's vehicles from Firebase Realtime Database
    private fun loadUserVehicles() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userVehiclesRef = database.reference.child("users").child(userId).child("vehicles")

            userVehiclesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val vehicleList = mutableListOf<String>()
                    for (vehicleSnapshot in snapshot.children) {
                        val registrationNumber = vehicleSnapshot.child("registrationNumber").getValue(String::class.java)
                        val model = vehicleSnapshot.child("model").getValue(String::class.java)

                        if (registrationNumber != null && model != null) {

                            val vehicleInfo = "$registrationNumber\n$model"
                            vehicleList.add(vehicleInfo)
                        }
                    }

                    // Populate the ListView
                    val adapter = ArrayAdapter(this@HomeActivity, android.R.layout.simple_list_item_1, vehicleList)
                    lvVehicles.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HomeActivity, "Failed to load vehicles: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}