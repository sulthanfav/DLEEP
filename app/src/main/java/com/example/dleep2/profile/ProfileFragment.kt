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
import androidx.fragment.app.Fragment
import com.example.dleep2.LoginActivity
import com.example.dleep2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Dapatkan user saat ini dari Firebase Authentication
        currentUser = mAuth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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

        // Load data pengguna dari Firestore
        loadUserData()

        buttonLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Toast.makeText(activity, "Logout Successful!", Toast.LENGTH_SHORT).show()
        }

        buttonEdit.setOnClickListener {
            // Ketika tombol "Edit" diklik
            buttonEdit.visibility = View.GONE // Sembunyikan tombol "Edit"
            buttonSave.visibility = View.VISIBLE // Tampilkan tombol "Save"
            enableEditText() // Aktifkan EditText untuk mengedit
        }

        buttonSave.setOnClickListener {
            // Ketika tombol "Save" diklik
            saveChanges() // Simpan perubahan
            buttonSave.visibility = View.GONE // Sembunyikan tombol "Save"
            buttonEdit.visibility = View.VISIBLE // Tampilkan tombol "Edit"
            disableEditText() // Nonaktifkan EditText setelah penyimpanan
        }

        return view
    }

    private fun enableEditText() {
        // Aktifkan EditText untuk pengeditan
        editUsername.visibility = View.VISIBLE
        editUsername.setText(txtUsername.text)
        editEmail.visibility = View.VISIBLE
        editEmail.setText(txtEmail.text)
        txtUsername.visibility = View.GONE
        txtEmail.visibility = View.GONE
    }

    private fun disableEditText() {
        // Nonaktifkan EditText setelah penyimpanan
        editUsername.visibility = View.GONE
        txtUsername.visibility = View.VISIBLE
        editEmail.visibility = View.GONE
        txtEmail.visibility = View.VISIBLE
    }

    private fun saveChanges() {
        // Implementasi untuk menyimpan perubahan
        val newUsername = editUsername.text.toString()
        val newEmail = editEmail.text.toString()

        // Dapatkan referensi dokumen untuk pengguna saat ini
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userRef = db.collection("users").document(currentUser.uid)

            // Periksa apakah dokumen sudah ada sebelum memperbarui datanya
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Dokumen sudah ada, lanjutkan dengan pembaruan
                    val updates = hashMapOf(
                        "username" to newUsername,
                        "email" to newEmail
                    )

                    userRef.update(updates as Map<String, Any>)
                        .addOnSuccessListener {
                            // Jika penyimpanan berhasil
                            Toast.makeText(
                                activity,
                                "Changes saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadUserData() // Load data terbaru dari Firestore
                            populateUserData() // Perbarui tampilan dengan data terbaru
                        }
                        .addOnFailureListener { e ->
                            // Jika penyimpanan gagal
                            Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                } else {
                    // Dokumen tidak ditemukan, tidak perlu melakukan apa-apa
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
                        // Dokumen belum ada, buat dokumen baru dengan data default
                        val defaultUsername = user.displayName ?: user.email?.split("@")?.get(0) ?: ""
                        val defaultEmail = user.email ?: ""
                        val defaultUserData = hashMapOf(
                            "username" to defaultUsername,
                            "email" to defaultEmail
                        )

                        userRef.set(defaultUserData)
                            .addOnSuccessListener {
                                loadUserData() // Panggil kembali loadUserData() untuk memuat data baru
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
            profdobvalues.text = dateFormat.format(dateOfBirth)

            // Isi nilai-nilai lainnya seperti nama, email di header, dll.
            profsname.text = username
            profsemail.text = email
        }
    }

}