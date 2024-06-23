package com.example.dleep2.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.dleep2.LoginActivity
import com.example.dleep2.PrivacyPolicyActivity
import com.example.dleep2.R
import com.example.dleep2.ServiceGuideActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private var userData: DocumentSnapshot? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var buttonEdit: Button
    private lateinit var buttonSave: Button
    private lateinit var txtUsername: TextView
    private lateinit var txtEmail: TextView
    private lateinit var editUsername: EditText
    private lateinit var editEmail: EditText
    private lateinit var profdobvalues: TextView
    private lateinit var profsname: TextView
    private lateinit var profsemail: TextView
    private lateinit var serviceGuideLayout: ConstraintLayout
    private lateinit var privacyPolicyLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        currentUser = mAuth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        buttonLogout = view.findViewById(R.id.buttonlogout)
        buttonEdit = view.findViewById(R.id.editprof)
        buttonSave = view.findViewById(R.id.saveprof)
        txtUsername = view.findViewById(R.id.profusnvalues)
        txtEmail = view.findViewById(R.id.profemailvalues)
        editUsername = view.findViewById(R.id.edit_profusnvalues)
        editEmail = view.findViewById(R.id.edit_profemailvalues)
        profdobvalues = view.findViewById(R.id.profdobvalues)
        profsname = view.findViewById(R.id.profsname)
        profsemail = view.findViewById(R.id.profsemail)
        serviceGuideLayout = view.findViewById(R.id.constraintLayoutSg)
        privacyPolicyLayout = view.findViewById(R.id.constraintLayoutPp)

        loadUserData()

        buttonLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Toast.makeText(activity, "Logout Successful!", Toast.LENGTH_SHORT).show()
        }

        buttonEdit.setOnClickListener {
            buttonEdit.visibility = View.GONE
            buttonSave.visibility = View.VISIBLE
            enableEditText()
        }

        buttonSave.setOnClickListener {
            saveChanges()
            buttonSave.visibility = View.GONE
            buttonEdit.visibility = View.VISIBLE
            disableEditText()
        }

        serviceGuideLayout.setOnClickListener {
            val intent = Intent(activity, ServiceGuideActivity::class.java)
            startActivity(intent)
        }

        privacyPolicyLayout.setOnClickListener {
            val intent = Intent(activity, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun enableEditText() {
        editUsername.visibility = View.VISIBLE
        editUsername.setText(txtUsername.text)
        editEmail.visibility = View.VISIBLE
        editEmail.setText(txtEmail.text)
        txtUsername.visibility = View.GONE
        txtEmail.visibility = View.GONE
    }

    private fun disableEditText() {
        editUsername.visibility = View.GONE
        txtUsername.visibility = View.VISIBLE
        editEmail.visibility = View.GONE
        txtEmail.visibility = View.VISIBLE
    }

    private fun saveChanges() {
        val newUsername = editUsername.text.toString()
        val newEmail = editEmail.text.toString()

        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userRef = db.collection("users").document(currentUser.uid)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val updates = hashMapOf(
                        "username" to newUsername,
                        "email" to newEmail
                    )

                    userRef.update(updates as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(
                                activity,
                                "Changes saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadUserData()
                            populateUserData()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                } else {
                    Toast.makeText(activity, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserData() {
        currentUser?.let { user ->
            val userRef = db.collection("users").document(user.uid)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userData = documentSnapshot
                        populateUserData()
                    } else {
                        val defaultUsername = user.displayName ?: user.email?.split("@")?.get(0) ?: ""
                        val defaultEmail = user.email ?: ""
                        val defaultUserData = hashMapOf(
                            "username" to defaultUsername,
                            "email" to defaultEmail
                        )

                        userRef.set(defaultUserData)
                            .addOnSuccessListener {
                                loadUserData()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun populateUserData() {
        userData?.let { document ->
            val username = document.getString("username")
            val email = document.getString("email")
            val dateOfBirthString = document.getString("date_of_birth")

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateOfBirth = try {
                dateFormat.parse(dateOfBirthString)
            } catch (e: ParseException) {
                null
            }

            txtUsername.text = username
            txtEmail.text = email
            profdobvalues.text = dateOfBirth?.let { dateFormat.format(it) } ?: ""

            profsname.text = username
            profsemail.text = email
        }
    }
}