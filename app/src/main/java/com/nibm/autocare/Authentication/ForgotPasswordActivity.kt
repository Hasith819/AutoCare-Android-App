package com.nibm.autocare.Authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nibm.autocare.R

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var etEmail: EditText
    private lateinit var btnSendCode: Button
    private lateinit var tvBackToSignIn: TextView
    private lateinit var tvSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        etEmail = findViewById(R.id.etEmail)
        btnSendCode = findViewById(R.id.btnSendCode)
        tvBackToSignIn = findViewById(R.id.tvBackToSignIn)
        tvSignUp = findViewById(R.id.tvSignUp)

        btnSendCode.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                checkEmailInDatabase(email)
            }
        }

        tvBackToSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun checkEmailInDatabase(email: String) {
        val databaseRef = database.getReference("users")
        databaseRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Email exists in the database
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Email verified.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@ForgotPasswordActivity, NewPasswordActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        // Email does not exist in the database
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Email is not registered. Please check your email or sign up.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Database error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}