package com.nibm.autocare

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class TestActivity : AppCompatActivity() {

    private lateinit var etRecipientEmail: EditText
    private lateinit var etEmailBody: EditText
    private lateinit var btnSendEmail: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // Initialize views
        etRecipientEmail = findViewById(R.id.etRecipientEmail)
        etEmailBody = findViewById(R.id.etEmailBody)
        btnSendEmail = findViewById(R.id.btnSendEmail)

        // Set up send email button click listener
        btnSendEmail.setOnClickListener {
            val recipientEmail = etRecipientEmail.text.toString().trim()

            if (recipientEmail.isEmpty()) {
                Toast.makeText(this, "Please enter recipient email", Toast.LENGTH_SHORT).show()
            } else {
                // Send email in the background
                SendEmailTask().execute(recipientEmail)
            }
        }
    }

    // AsyncTask to send email in the background
    private inner class SendEmailTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            val recipientEmail = params[0]
            val emailBody = "This is a hardcoded message sent from the AutoCare app."

            // Sender's email credentials
            val senderEmail = "hasithpubudu@gmail.com" // Replace with your Gmail address
            val senderPassword = "buyf seft oeao ywnj" // Replace with your 16-digit App Password

            // Set up mail server properties
            val properties = Properties()
            properties["mail.smtp.host"] = "smtp.gmail.com"
            properties["mail.smtp.port"] = "587"
            properties["mail.smtp.auth"] = "true"
            properties["mail.smtp.starttls.enable"] = "true"

            // Create a session with authentication
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            try {
                // Create a MimeMessage
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(senderEmail))
                message.addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                message.subject = "Test Email from AutoCare"
                message.setText(emailBody)

                // Send the email
                Transport.send(message)
                return "Email sent successfully to $recipientEmail"
            } catch (e: MessagingException) {
                Log.e("SendEmailTask", "Failed to send email", e)
                return "Failed to send email: ${e.message}"
            } catch (e: Exception) {
                Log.e("SendEmailTask", "Unexpected error", e)
                return "Unexpected error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String) {
            Toast.makeText(this@TestActivity, result, Toast.LENGTH_SHORT).show()
            Log.d("SendEmailTask", result) // Log the result to Logcat
        }
    }
}