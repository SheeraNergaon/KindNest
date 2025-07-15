package com.SheeraNergaon.KindNest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.SheeraNergaon.KindNest.databinding.ActivityEditEventBinding
import com.google.firebase.database.FirebaseDatabase

class EditEventActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_edit_event

    private lateinit var binding: ActivityEditEventBinding
    private lateinit var currentEvent: CharityEvent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get event from intent
        currentEvent = intent.getSerializableExtra("event") as? CharityEvent
            ?: return finishWithError("Invalid event data")

        // Pre-fill fields
        binding.editEventETTitle.setText(currentEvent.title)
        binding.editEventETDesc.setText(currentEvent.description)
        binding.editEventETLocation.setText(currentEvent.location)
        binding.editEventETDate.setText(currentEvent.date)

        // Save changes
        binding.editEventBTNSubmit.setOnClickListener {
            val updatedTitle = binding.editEventETTitle.text.toString().trim()
            val updatedDesc = binding.editEventETDesc.text.toString().trim()
            val updatedLocation = binding.editEventETLocation.text.toString().trim()
            val updatedDate = binding.editEventETDate.text.toString().trim()

            if (updatedTitle.isEmpty() || updatedDesc.isEmpty() || updatedLocation.isEmpty() || updatedDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedEvent = currentEvent.copy(
                title = updatedTitle,
                description = updatedDesc,
                location = updatedLocation,
                date = updatedDate
            )

            val dbRef = FirebaseDatabase.getInstance().reference
                .child("KindNest")
                .child("charity_events")
                .child(currentEvent.id)

            dbRef.setValue(updatedEvent)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
                }
        }

        // Delete event
        binding.editEventBTNDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Delete") { _, _ ->
                deleteEvent()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteEvent() {
        val dbRef = FirebaseDatabase.getInstance().reference
            .child("KindNest")
            .child("charity_events")
            .child(currentEvent.id)

        dbRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Event deleted!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun finishWithError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
