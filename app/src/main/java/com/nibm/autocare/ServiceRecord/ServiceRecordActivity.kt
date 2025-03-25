package com.nibm.autocare

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.Vehicle.AddVehicleActivity

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

        findViewById<View>(R.id.llHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<View>(R.id.llAddVehicle).setOnClickListener {
            startActivity(Intent(this, AddVehicleActivity::class.java))
        }

        findViewById<View>(R.id.llAddService).setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            startActivity(intent)
        }

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
                        val serviceType = serviceSnapshot.child("serviceType").getValue(String::class.java)
                        val checkedItems = serviceSnapshot.child("checkedItems").children.map { it.getValue(String::class.java) }.filterNotNull()
                        val notes = serviceSnapshot.child("notes").getValue(String::class.java)
                        val photoUrls = serviceSnapshot.child("photoUrls").children.map { it.getValue(String::class.java) }.filterNotNull()

                        if (date != null && odometerReading != null && serviceCost != null) {
                            serviceList.add(ServiceRecord(
                                date, odometerReading, serviceCost,
                                serviceType, checkedItems, notes, photoUrls
                            ))
                        }
                    }

                    val sortedList = serviceList.sortedByDescending { it.date }
                    val adapter = ServiceRecordAdapter(sortedList, this@ServiceRecordActivity)
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
        val serviceCost: String,
        val serviceType: String? = null,
        val checkedItems: List<String>? = null,
        val notes: String? = null,
        val photoUrls: List<String>? = null
    )

    // Custom Adapter for Service Record List
        inner class ServiceRecordAdapter(
            private val serviceList: List<ServiceRecord>,
            private val context: Context
        ) : BaseAdapter() {
            private val expandedPositions = mutableSetOf<Int>()
            private val imageSize = 250.dpToPx() // Helper extension to convert dp to px

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
                    // Clear previous images
                    viewHolder.imageContainer.removeAllViews()
                }

                val service = serviceList[position]

                // Set basic info
                viewHolder.tvOdometerReading.text = "${service.odometerReading} km"
                viewHolder.tvServiceDate.text = service.date
                viewHolder.tvServiceCost.text = "Rs ${service.serviceCost}"

                // Set expanded details
                service.serviceType?.let {
                    viewHolder.tvServiceType.text = "Service: $it"
                    viewHolder.tvServiceType.visibility = View.VISIBLE
                } ?: run { viewHolder.tvServiceType.visibility = View.GONE }

                service.checkedItems?.let {
                    viewHolder.tvCheckedItems.text = "Services:\n${it.joinToString("\n• ", "• ")}"
                    viewHolder.tvCheckedItems.visibility = View.VISIBLE
                } ?: run { viewHolder.tvCheckedItems.visibility = View.GONE }

                service.notes?.let {
                    viewHolder.tvNotes.text = "Notes: $it"
                    viewHolder.tvNotes.visibility = View.VISIBLE
                } ?: run { viewHolder.tvNotes.visibility = View.GONE }

                // Load images if available
                service.photoUrls?.let { urls ->
                    if (urls.isNotEmpty()) {
                        urls.forEach { url ->
                            val imageView = ImageView(context).apply {
                                layoutParams = LinearLayout.LayoutParams(imageSize, imageSize).apply {
                                    marginEnd = 8.dpToPx()
                                }
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                adjustViewBounds = true
                                clipToOutline = true
                                background = ContextCompat.getDrawable(context, R.drawable.image_border)
                            }

                            // Load image using Glide or Picasso
                            Glide.with(context)
                                .load(url)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .into(imageView)

                            // Add click to view full image
                            imageView.setOnClickListener {
                                showFullImageDialog(url)
                            }

                            viewHolder.imageContainer.addView(imageView)
                        }
                    }
                }

                // Toggle expanded state
                viewHolder.llExpandedDetails.visibility =
                    if (expandedPositions.contains(position)) View.VISIBLE else View.GONE

                // Set click listener to toggle expansion
                view.setOnClickListener {
                    if (expandedPositions.contains(position)) {
                        expandedPositions.remove(position)
                    } else {
                        expandedPositions.add(position)
                    }
                    notifyDataSetChanged()
                }

                return view
            }

            private fun showFullImageDialog(imageUrl: String) {
                val dialog = Dialog(context).apply {
                    setContentView(R.layout.dialog_full_image)
                    window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    findViewById<ImageView>(R.id.ivFullImage).let { imageView ->
                        Glide.with(context)
                            .load(imageUrl)
                            .into(imageView)
                    }
                    findViewById<View>(R.id.btnClose).setOnClickListener { dismiss() }
                }
                dialog.show()
            }

            private inner class ViewHolder(view: View) {
                val tvOdometerReading: TextView = view.findViewById(R.id.tvOdometerReading)
                val tvServiceDate: TextView = view.findViewById(R.id.tvServiceDate)
                val tvServiceCost: TextView = view.findViewById(R.id.tvServiceCost)
                val tvServiceType: TextView = view.findViewById(R.id.tvServiceType)
                val tvCheckedItems: TextView = view.findViewById(R.id.tvCheckedItems)
                val tvNotes: TextView = view.findViewById(R.id.tvNotes)
                val llExpandedDetails: LinearLayout = view.findViewById(R.id.llExpandedDetails)
                val imageContainer: LinearLayout = view.findViewById(R.id.imageContainer)
            }

            private fun Int.dpToPx(): Int = (this * context.resources.displayMetrics.density).toInt()
        }
}