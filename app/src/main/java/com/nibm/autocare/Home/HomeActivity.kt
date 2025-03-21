package com.nibm.autocare

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nibm.autocare.Vehicle.AddVehicleActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var ivMenu: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        tvGreeting = findViewById(R.id.tvGreeting)
        ivMenu = findViewById(R.id.ivMenu)

        // Set greeting with username (replace "John" with actual username from database)
        val username = "John" // Replace with actual username from database
        tvGreeting.text = "Hello, $username"

        // Set up menu icon click listener
        ivMenu.setOnClickListener {
            showMenu(it)
        }

        // Set up "Add Vehicle" button click listener
        val btnAddVehicle = findViewById<View>(R.id.btnSearch)
        btnAddVehicle.setOnClickListener {
            // Navigate to AddVehicleActivity
            val intent = Intent(this, AddVehicleActivity::class.java)
            startActivity(intent)
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
                    Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
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
}