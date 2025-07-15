package com.SheeraNergaon.KindNest

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DonatePointsActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_donate_points

    private lateinit var db: DatabaseReference
    private var currentPoints = 0
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val balanceText = findViewById<TextView>(R.id.donate_points_balance)
        val recycler = findViewById<RecyclerView>(R.id.donationRecycler)
        recycler.layoutManager = GridLayoutManager(this, 2)

        val causes = listOf(
            DonationCause("Shelter", R.drawable.icon_shelter),
            DonationCause("Sick Children", R.drawable.icon_kids),
            DonationCause("Animal Rescue", R.drawable.icon_dog),
            DonationCause("Food Drive", R.drawable.icon_food),
            DonationCause("Mental Health", R.drawable.mental_health),
            DonationCause("Hostages Family Support", R.drawable.hostages_family),
            DonationCause("IDF Soldiers", R.drawable.idf_soldiers),
            DonationCause("Holocaust Survivors Support", R.drawable.holocaust_support)
        )

        val adapter = DonationAdapter(causes) { cause ->
            showDonationPrompt(cause, balanceText)
        }

        recycler.adapter = adapter

        db = FirebaseDatabase.getInstance().reference.child("KindNest").child("users").child(userId)
        db.child("points").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentPoints = snapshot.getValue(Int::class.java) ?: 0
                balanceText.text = "Your Points: $currentPoints"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showDonationPrompt(cause: DonationCause, balanceText: TextView) {
        val input = EditText(this).apply {
            hint = "Enter points to donate"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Donate to ${cause.name}")
            .setMessage("How many points would you like to donate?")
            .setView(input)
            .setPositiveButton("Donate") { dialog, _ ->
                val enteredText = input.text.toString()
                val pointsToDonate = enteredText.toIntOrNull()

                if (pointsToDonate == null || pointsToDonate <= 0) {
                    Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (pointsToDonate > currentPoints) {
                    Toast.makeText(this, "Not enough points.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Update points
                currentPoints -= pointsToDonate
                db.child("points").setValue(currentPoints)
                db.child("donated_points").push().setValue(
                    mapOf(
                        "cause" to cause.name,
                        "points" to pointsToDonate,
                        "date" to System.currentTimeMillis()
                    )
                )

                balanceText.text = "Your Points: $currentPoints"
                Toast.makeText(this, "Thank you for donating $pointsToDonate points to ${cause.name}!", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
