package com.example.dleep2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password) // Pastikan Anda memiliki layout ini

        emailEditText = findViewById(R.id.editTextEmailUsn)
        auth = FirebaseAuth.getInstance()
        submitButton = findViewById(R.id.buttonSubmit1)

        submitButton.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = emailEditText.text.toString().trim()
        if (email.isNotEmpty()) {
            sendPasswordResetEmail(email)
        } else {
            Toast.makeText(this, "Masukkan email Anda.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ForgotPasswordActivity", "Reset password email sent.")
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "Reset password email sent.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ForgotPasswordActivity", "Failed to send reset password email.", task.exception)
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "Failed to send reset password email.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}