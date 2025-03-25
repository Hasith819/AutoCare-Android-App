package com.nibm.autocare

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.Authentication.LoginActivity
import com.nibm.autocare.Vehicle.AddVehicleActivity

class HomeActivity : AppCompatActivity() {

    private val vehicleList = mutableListOf<Vehicle>()

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

        // Fetch and display the user's vehicles
        fetchVehicles()

        // Set up menu icon click listener
        findViewById<View>(R.id.ivMenu).setOnClickListener {
            showMenu(it)
        }


        findViewById<View>(R.id.llAddVehicle).setOnClickListener {
            val intent = Intent(this, AddVehicleActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.llAddService).setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            startActivity(intent)
        }


        // Set click listener for vehicle items
        lvVehicles.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedVehicle = vehicleList[position]
            val intent = Intent(this, ServiceRecordActivity::class.java).apply {
                putExtra("vehicleRegistration", selectedVehicle.registrationNumber)
            }
            startActivity(intent)
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
                        tvGreeting.text = "Hello, $username!"
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

    // Fetch vehicles from Firebase Realtime Database
    private fun fetchVehicles() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val vehiclesRef = database.reference.child("users_vehicles").child(userId)

            vehiclesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    vehicleList.clear() // Clear existing items
                    for (vehicleSnapshot in snapshot.children) {
                        val registrationNumber = vehicleSnapshot.child("registrationNumber").getValue(String::class.java)
                        val brand = vehicleSnapshot.child("brand").getValue(String::class.java)
                        val manufacturedYear = vehicleSnapshot.child("manufacturedYear").getValue(String::class.java)
                        val model = vehicleSnapshot.child("model").getValue(String::class.java)

                        if (registrationNumber != null && brand != null && manufacturedYear != null && model != null) {
                            val vehicle = Vehicle(registrationNumber, brand, manufacturedYear, model)
                            vehicleList.add(vehicle)
                        }
                    }

                    val adapter = VehicleAdapter(vehicleList)
                    lvVehicles.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HomeActivity, "Failed to fetch vehicles: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Function to show the dropdown menu
    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_home) // Inflate the menu_home.xml

        // Set click listener for menu items
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_logout -> {
                    // Handle logout
                    logout()
                    true
                }
                R.id.menu_delete_account -> {
                    // Handle delete account
                    deleteAccount()
                    true
                }
                else -> false
            }
        }

        // Show the menu
        popupMenu.show()
    }

    // Logout the user
    private fun logout() {
        auth.signOut() // Sign out from Firebase
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to LoginActivity and clear the back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close the current activity
    }

    // Delete the user account and associated data
    private fun deleteAccount() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Delete user data from Realtime Database
            val userRef = database.reference.child("users").child(userId)
            userRef.removeValue().addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    // Delete Firebase Authentication account
                    currentUser.delete().addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                            // Logout and navigate to LoginActivity
                            logout()
                        } else {
                            Toast.makeText(this, "Failed to delete account: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to delete user data: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Data class for Vehicle
    data class Vehicle(
        val registrationNumber: String,
        val brand: String,
        val manufacturedYear: String,
        val model: String
    )

    // Custom Adapter for Vehicle List
    inner class VehicleAdapter(private val vehicleList: List<Vehicle>) : BaseAdapter() {
        override fun getCount(): Int {
            return vehicleList.size
        }

        override fun getItem(position: Int): Any {
            return vehicleList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val viewHolder: ViewHolder

            if (convertView == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_vehicle, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            val vehicle = vehicleList[position]
            viewHolder.tvRegistrationNumber.text = vehicle.registrationNumber
            viewHolder.tvBrand.text = vehicle.brand
            viewHolder.tvManufacturedYear.text = vehicle.manufacturedYear
            viewHolder.tvModel.text = vehicle.model

            return view
        }

        // ViewHolder pattern for better performance
        private inner class ViewHolder(view: View) {
            val tvRegistrationNumber: TextView = view.findViewById(R.id.tvRegistrationNumber)
            val tvBrand: TextView = view.findViewById(R.id.tvBrand)
            val tvManufacturedYear: TextView = view.findViewById(R.id.tvManufacturedYear)
            val tvModel: TextView = view.findViewById(R.id.tvModel)
        }
    }
}