package com.nibm.autocare.Authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nibm.autocare.R

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Back to Sign In
        val tvBackToSignIn = findViewById<TextView>(R.id.tvBackToSignIn)
        tvBackToSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val signUpTextView = findViewById<TextView>(R.id.tvSignUp)
        signUpTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Send Code Button
        val btnSendCode = findViewById<Button>(R.id.btnSendCode)
        btnSendCode.setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, VerificationActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle specific errors
                    val error = task.exception
                    if (error != null) {
                        when (error.message) {
                            "There is no user record corresponding to this identifier. The user may have been deleted." -> {
                                Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Failed to send password reset email: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
    }
}