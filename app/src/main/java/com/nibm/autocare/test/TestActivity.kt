package com.nibm.autocare

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.R

class TestActivity : AppCompatActivity() {

    private lateinit var etSearchEmail: EditText
    private lateinit var btnSearch: Button
    private lateinit var lvEmails: ListView
    private lateinit var emailList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        etSearchEmail = findViewById(R.id.etSearchEmail)
        btnSearch = findViewById(R.id.btnSearch)
        lvEmails = findViewById(R.id.lvEmails)

        emailList = mutableListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emailList)
        lvEmails.adapter = adapter

        btnSearch.setOnClickListener {
            val searchQuery = etSearchEmail.text.toString().trim()
            if (searchQuery.isEmpty()) {
                Toast.makeText(this, "Please enter an email to search", Toast.LENGTH_SHORT).show()
            } else {
                searchEmails(searchQuery)
            }
        }
    }

    private fun searchEmails(searchQuery: String) {
        val database = FirebaseDatabase.getInstance().reference.child("users")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                emailList.clear()
                for (userSnapshot in snapshot.children) {
                    val email = userSnapshot.child("email").getValue(String::class.java)
                    if (email != null && email.contains(searchQuery, ignoreCase = true)) {
                        emailList.add(email)
                    }
                }
                if (emailList.isEmpty()) {
                    Toast.makeText(this@TestActivity, "No matching emails found", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TestActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}