package com.example.dleep2

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dleep2.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textViewLogSign.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.editTextDatepicker.setOnClickListener {
            showDatePickerDialog()
        }

        binding.registerbutton.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val username = binding.editTextUsername.text.toString()
            val dob = binding.editTextDatepicker.text.toString()

            // Validate username
            if (username.isEmpty()) {
                binding.editTextUsername.error = "Username must not be empty"
                binding.editTextUsername.requestFocus()
                return@setOnClickListener
            }

            // Validate username format (no spaces)
            if (username.contains(" ")) {
                binding.editTextUsername.error = "Username must not contain spaces"
                binding.editTextUsername.requestFocus()
                return@setOnClickListener
            }

            // Validate email
            if (email.isEmpty()) {
                binding.editTextEmail.error = "Email must not be empty"
                binding.editTextEmail.requestFocus()
                return@setOnClickListener
            }

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextEmail.error = "Invalid email format"
                binding.editTextEmail.requestFocus()
                return@setOnClickListener
            }

            // Validate password
            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password must not be empty"
                binding.editTextPassword.requestFocus()
                return@setOnClickListener
            }

            // Validate password length
            if (password.length < 6) {
                binding.editTextPassword.error = "Password must be at least 6 characters"
                binding.editTextPassword.requestFocus()
                return@setOnClickListener
            }

            // Register user
            registerUser(email, password, username, dob)
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                // Format tanggal yang dipilih
                val selectedDateString = String.format("%02d/%02d/%04d", day, month + 1, year)
                selectedDate = selectedDateString
                binding.editTextDatepicker.setText(selectedDateString)
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun registerUser(email: String, password: String, username: String, dob: String) {
        val finalDob = selectedDate ?: dob
        // Check if username already exists
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // Username is available, proceed with registration
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Save username, email, and date of birth to Firebase
                                val user = auth.currentUser
                                val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build()
                                user?.updateProfile(userProfileChangeRequest)
                                    ?.addOnCompleteListener { profileUpdateTask ->
                                        if (profileUpdateTask.isSuccessful) {
                                            // Save additional user data to Firestore
                                            val userData = hashMapOf(
                                                "username" to username,
                                                "email" to email,
                                                "date_of_birth" to finalDob
                                            )
                                            db.collection("users")
                                                .document(user.uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(this, LoginActivity::class.java)
                                                    startActivity(intent)
                                                    finish() // Close RegisterActivity
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(this, "Error updating profile: ${profileUpdateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Registration failed. ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Username already exists, show error message
                    binding.editTextUsername.error = "Username is already taken"
                    binding.editTextUsername.requestFocus()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking username availability: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
