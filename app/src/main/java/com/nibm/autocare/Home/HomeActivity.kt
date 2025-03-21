package com.nibm.autocare

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
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

    private lateinit var tvGreeting: TextView
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

        // Fetch and display the username from Firebase Realtime Database
        fetchUsername()

        // Set up menu icon click listener
        findViewById<View>(R.id.ivMenu).setOnClickListener {
            showMenu(it)
        }

        // Set up "Add Vehicle" button click listener
        findViewById<View>(R.id.btnSearch).setOnClickListener {
            // Navigate to AddVehicleActivity
            val intent = Intent(this, AddVehicleActivity::class.java)
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
                    Toast.makeText(this, "Delete Account clicked", Toast.LENGTH_SHORT).show()
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
}