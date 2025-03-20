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

class VerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get email from intent
        email = intent.getStringExtra("email") ?: ""

        // Verify Button
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        btnVerify.setOnClickListener {
            val code = findViewById<EditText>(R.id.etVerificationCode).text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(code)
            }
        }

        // Resend Code
        val tvResendCode = findViewById<TextView>(R.id.tvResendCode)
        tvResendCode.setOnClickListener {
            sendVerificationCode(email)
        }
    }

    private fun verifyCode(code: String) {
        // For simplicity, assume the code is correct
        // In a real app, you would validate the code sent to the email
        Toast.makeText(this, "Code verified successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, NewPasswordActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }

    private fun sendVerificationCode(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification code resent to $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to resend verification code", Toast.LENGTH_SHORT).show()
                }
            }
    }
}