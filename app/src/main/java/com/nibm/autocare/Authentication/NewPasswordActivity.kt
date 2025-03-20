package com.nibm.autocare.Authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nibm.autocare.R

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSetNewPassword: Button
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        auth = FirebaseAuth.getInstance()

        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSetNewPassword = findViewById(R.id.btnSetNewPassword)

        // Get the email passed from the previous activity
        email = intent.getStringExtra("email") ?: ""

        btnSetNewPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(email, newPassword)
            }
        }
    }

    private fun resetPassword(email: String, newPassword: String) {
        // Re-authenticate the user (if needed) and update the password
        val user = auth.currentUser

        if (user != null && user.email == email) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        // Redirect to LoginActivity
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to update password: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "User not found or email mismatch", Toast.LENGTH_SHORT).show()
        }
    }
}