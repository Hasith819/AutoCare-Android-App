package com.nibm.autocare

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ServiceRecordActivity : AppCompatActivity() {

    private lateinit var lvServiceRecords: ListView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var vehicleRegistration: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_record)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the selected vehicle registration from intent
        vehicleRegistration = intent.getStringExtra("vehicleRegistration") ?: ""

        // Set the title with vehicle registration
        findViewById<TextView>(R.id.tvAppName).text = "Services for $vehicleRegistration"

        // Initialize views
        lvServiceRecords = findViewById(R.id.lvServiceRecords)

        // Fetch and display the service records
        fetchServiceRecords()

    }

    private fun fetchServiceRecords() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val servicesRef = database.reference.child("users_services").child(userId).child(vehicleRegistration)

            servicesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val serviceList = mutableListOf<ServiceRecord>()
                    for (serviceSnapshot in snapshot.children) {
                        val date = serviceSnapshot.child("date").getValue(String::class.java)
                        val odometerReading = serviceSnapshot.child("odometerReading").getValue(String::class.java)
                        val serviceCost = serviceSnapshot.child("serviceCost").getValue(String::class.java)

                        if (date != null && odometerReading != null && serviceCost != null) {
                            val serviceRecord = ServiceRecord(date, odometerReading, serviceCost)
                            serviceList.add(serviceRecord)
                        }
                    }

                    // Sort by date (newest first)
                    val sortedList = serviceList.sortedByDescending { it.date }

                    // Populate the ListView with a custom adapter
                    val adapter = ServiceRecordAdapter(sortedList)
                    lvServiceRecords.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ServiceRecordActivity,
                        "Failed to fetch services: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Data class for Service Record
    data class ServiceRecord(
        val date: String,
        val odometerReading: String,
        val serviceCost: String
    )

    // Custom Adapter for Service Record List
    inner class ServiceRecordAdapter(private val serviceList: List<ServiceRecord>) : BaseAdapter() {
        override fun getCount(): Int = serviceList.size
        override fun getItem(position: Int): Any = serviceList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val viewHolder: ViewHolder

            if (convertView == null) {
                view = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.list_item_service_record, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            val service = serviceList[position]
            viewHolder.tvOdometerReading.text = "${service.odometerReading} km"
            viewHolder.tvServiceDate.text = service.date
            viewHolder.tvServiceCost.text = "Rs ${service.serviceCost}"

            return view
        }

        private inner class ViewHolder(view: View) {
            val tvOdometerReading: TextView = view.findViewById(R.id.tvOdometerReading)
            val tvServiceDate: TextView = view.findViewById(R.id.tvServiceDate)
            val tvServiceCost: TextView = view.findViewById(R.id.tvServiceCost)
        }
    }
}