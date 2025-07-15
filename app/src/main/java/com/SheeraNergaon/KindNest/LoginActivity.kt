package com.SheeraNergaon.KindNest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            signIn()
        } else {
            createUserNodeIfNeeded()
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.kindnest_logo)
            .setTheme(R.style.Theme_KindNest)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            createUserNodeIfNeeded()
        } else {
            Toast.makeText(this, "Error: Failed Logging in.", Toast.LENGTH_LONG).show()
            signIn()
        }
    }

    private fun createUserNodeIfNeeded() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid
        val db = FirebaseDatabase.getInstance().reference.child("KindNest")

        val userRef = db.child("users").child(userId)
        userRef.child("points").get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                userRef.child("points").setValue(0)
            }
            transactToMainActivity()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to check user record", Toast.LENGTH_SHORT).show()
            transactToMainActivity() // still allow entry but with caution
        }
    }


    private fun transactToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
