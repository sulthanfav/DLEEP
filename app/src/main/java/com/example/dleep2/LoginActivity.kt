package com.example.dleep2

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dleep2.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var eyeShows: ImageView
    private lateinit var eyeHides: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Find references to eyeShows and eyeHides ImageViews
        eyeShows = binding.eyeShows
        eyeHides = binding.eyeHides

        // Set up click listeners for eyeShows and eyeHides
        eyeShows.setOnClickListener {
            binding.editTextPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            eyeShows.visibility = View.GONE
            eyeHides.visibility = View.VISIBLE
            binding.editTextPassword.setSelection(binding.editTextPassword.length())
        }

        eyeHides.setOnClickListener {
            binding.editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            eyeShows.visibility = View.VISIBLE
            eyeHides.visibility = View.GONE
            binding.editTextPassword.setSelection(binding.editTextPassword.length())
        }


        binding.textViewForgotPw.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.textViewSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogin2.setOnClickListener {
            val emailOrUsername = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()

            // Validate email or username
            if (emailOrUsername.isEmpty() || emailOrUsername.contains(" ")) {
                binding.editTextUsername.error = "Email or username must not be empty or contain spaces"
                binding.editTextUsername.requestFocus()
                return@setOnClickListener
            }

            // Validate password
            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password must not be empty"
                binding.editTextPassword.requestFocus()
                return@setOnClickListener
            }

            // Attempt login
            login(emailOrUsername, password)
        }
    }

    private fun login(emailOrUsername: String, password: String) {
        // Check if input is email or username
        val isEmail = Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()

        if (isEmail) {
            auth.signInWithEmailAndPassword(emailOrUsername, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Find email associated with username in Firestore
            db.collection("users")
                .whereEqualTo("username", emailOrUsername)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val document = result.documents[0]
                        val userEmail = document.getString("email")
                        if (userEmail != null) {
                            auth.signInWithEmailAndPassword(userEmail, password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Email not found for username: $emailOrUsername", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Username not found: $emailOrUsername", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error searching for username: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}