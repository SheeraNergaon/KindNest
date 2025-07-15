package com.SheeraNergaon.KindNest

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EventSignupActivity : AppCompatActivity() {

    private lateinit var eventId: String
    private lateinit var eventTitle: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_signup)

        // Extract event details from intent
        eventId = intent.getStringExtra("EVENT_ID") ?: return
        eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Untitled Event"
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val firstName = findViewById<EditText>(R.id.firstNameEditText)
        val lastName = findViewById<EditText>(R.id.lastNameEditText)
        val idField = findViewById<EditText>(R.id.idEditText)
        val age = findViewById<EditText>(R.id.ageEditText)
        val phone = findViewById<EditText>(R.id.phoneEditText)
        val submit = findViewById<Button>(R.id.submitButton)
        val db = FirebaseDatabase.getInstance().reference

        submit.setOnClickListener {
            val signupData = mapOf(
                "firstName" to firstName.text.toString().trim(),
                "lastName" to lastName.text.toString().trim(),
                "id" to idField.text.toString().trim(),
                "age" to age.text.toString().trim(),
                "phone" to phone.text.toString().trim()
            )

            if (signupData.values.any { it.isEmpty() }) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Write user signup data to: events/<eventId>/signups/<userId>
            val signupPath = db.child("events").child(eventId).child("signups").child(userId)
            signupPath.setValue(signupData).addOnSuccessListener {
                // Mirror event title under: KindNest/users/<userId>/signedUpEvents/<eventId>
                val userSignupRef = db
                    .child("KindNest")
                    .child("users")
                    .child(userId)
                    .child("signedUpEvents")
                    .child(eventId)

                userSignupRef.setValue(mapOf("title" to eventTitle))
                    .addOnSuccessListener {
                        // Increment: events/<eventId>/participantsCount
                        val countRef = db.child("events").child(eventId).child("participantsCount")
                        countRef.get().addOnSuccessListener { snapshot ->
                            val current = snapshot.getValue(Int::class.java) ?: 0
                            countRef.setValue(current + 1)
                        }

                        Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save signup: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to sign up", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
