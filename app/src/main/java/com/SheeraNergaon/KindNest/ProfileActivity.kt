package com.SheeraNergaon.KindNest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.SheeraNergaon.KindNest.databinding.ActivityProfileBinding
import com.firebase.ui.auth.AuthUI
import com.SheeraNergaon.KindNest.utilities.Constants.getUserLevel
import com.SheeraNergaon.KindNest.utilities.Constants.getUnlockedFeatures
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_profile

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: DatabaseReference
    private var level = "Seed"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.bind(findViewById(android.R.id.content))

        binding.profileBTNSignOut.setOnClickListener {
            Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show()
            AuthUI.getInstance().signOut(this)
                .addOnCompleteListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        }

        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid

        // Real-time listener for level updates
        db = FirebaseDatabase.getInstance().reference.child("KindNest")
        db.child("users").child(userId).child("points").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val points = snapshot.getValue(Int::class.java) ?: 0
                val newLevel = getUserLevel(points)
                if (newLevel != level) {
                    level = newLevel
                    val features = getUnlockedFeatures(level)
                    binding.profileLBLLevel.text = "Your level: $level"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val myEventsButton = findViewById<Button>(R.id.profile_BTN_my_events)
        myEventsButton.setOnClickListener {
            startActivity(Intent(this, MyEventsActivity::class.java))
        }
        val donateButton = findViewById<Button>(R.id.profile_BTN_donate)
        donateButton.setOnClickListener {
            val intent = Intent(this, DonatePointsActivity::class.java)
            startActivity(intent)
        }

    }
}
