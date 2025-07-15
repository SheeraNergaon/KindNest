package com.SheeraNergaon.KindNest

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import com.SheeraNergaon.KindNest.databinding.ActivityCreateEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CreateEventActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_create_event

    private lateinit var binding: ActivityCreateEventBinding

    private var selectedLat: Double? = null
    private var selectedLon: Double? = null

    companion object {
        const val LOCATION_PICKER_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make location field open location picker
        binding.createEventETLocation.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val intent = Intent(this@CreateEventActivity, PickLocationActivity::class.java)
                startActivityForResult(intent, LOCATION_PICKER_REQUEST)
            }
        }

        // Make date field open date picker
        binding.createEventETDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDatePicker()
            }
        }

        binding.createEventBTNSubmit.setOnClickListener {
            val title = binding.createEventETTitle.text.toString().trim()
            val desc = binding.createEventETDesc.text.toString().trim()
            val locationText = binding.createEventETLocation.text.toString().trim()
            val date = binding.createEventETDate.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || locationText.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedLat == null || selectedLon == null) {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val createdBy = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
            val db = FirebaseDatabase.getInstance().reference
            val eventId = db.child("KindNest").child("charity_events").push().key ?: return@setOnClickListener

            val event = CharityEvent(
                id = eventId,
                title = title,
                description = desc,
                date = date,
                location = locationText,
                createdBy = createdBy,
                latitude = selectedLat,
                longitude = selectedLon
            )

            db.child("KindNest").child("charity_events").child(eventId)
                .setValue(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create event", Toast.LENGTH_SHORT).show()
                }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showDatePicker() {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Event Date")
            .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            // selection is a timestamp in UTC millis
            val datePart = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(selection))
            // After selecting date, show time picker
            showTimePicker(datePart)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val lat = data?.getDoubleExtra("latitude", 0.0)
            val lon = data?.getDoubleExtra("longitude", 0.0)
            if (lat != null && lon != null) {
                selectedLat = lat
                selectedLon = lon

                // Reverse geocoding to get address
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addressLine = addresses[0].getAddressLine(0)
                        binding.createEventETLocation.setText(addressLine)
                    } else {
                        binding.createEventETLocation.setText("Lat: %.5f, Lon: %.5f".format(lat, lon))
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error getting address: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.createEventETLocation.setText("Lat: %.5f, Lon: %.5f".format(lat, lon))
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    private fun showTimePicker(datePart: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = android.app.TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // Format hour and minute to always be 2 digits
                val timePart = String.format("%02d:%02d", selectedHour, selectedMinute)
                val dateTime = "$datePart $timePart"
                binding.createEventETDate.setText(dateTime)
            },
            hour,
            minute,
            true // is24HourView
        )
        timePickerDialog.setTitle("Select Event Time")
        timePickerDialog.show()
    }

}
