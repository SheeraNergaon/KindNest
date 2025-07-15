package com.SheeraNergaon.KindNest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class PickLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private var selectedLatLng: LatLng? = null
    private lateinit var map: GoogleMap
    private lateinit var searchEditText: EditText

    companion object {
        const val AUTOCOMPLETE_REQUEST_CODE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_location)

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyC-A7N8jFvQWc0u4Hvrb2ugTYlDahWgvLc")
        }

        searchEditText = findViewById(R.id.searchEditText)
        val confirmButton = findViewById<android.widget.Button>(R.id.confirmLocationButton)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Launch autocomplete when the search field is tapped
        searchEditText.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountries(listOf("IL")) // Restrict to Israel
                    .build(this@PickLocationActivity)
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
            }
        }

        confirmButton.setOnClickListener {
            selectedLatLng?.let { latLng ->
                val resultIntent = Intent().apply {
                    putExtra("latitude", latLng.latitude)
                    putExtra("longitude", latLng.longitude)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        val defaultLocation = LatLng(32.0853, 34.7818) // Tel Aviv
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng))
            selectedLatLng = latLng
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    val latLng = place.latLng
                    if (latLng != null) {
                        map.clear()
                        map.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(place.name)
                        )
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        selectedLatLng = latLng

                        // Show the address in the EditText
                        searchEditText.setText(place.address ?: "${latLng.latitude}, ${latLng.longitude}")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_CANCELED -> {
                    // User canceled
                }
            }
        }
    }
}
