package com.example.expensetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.email_login)
        val passwordEditText = findViewById<EditText>(R.id.password_login)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val signuptext = findViewById<TextView>(R.id.signup_reg)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
        signuptext.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgot_password)

        forgotPasswordTextView.setOnClickListener {
            showResetPasswordDialog()
        }
    }
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun showResetPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")

        val input = EditText(this)
        input.hint = "Enter your email"
        builder.setView(input)

        builder.setPositiveButton("Reset") { dialog, which ->
            val email = input.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            sendPasswordResetEmail(email)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}