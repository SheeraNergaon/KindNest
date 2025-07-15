package com.SheeraNergaon.KindNest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapEventsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var db: DatabaseReference

    private var hasCenteredOnUser = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableUserLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_events)

        db = FirebaseDatabase.getInstance().reference

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.eventsMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        val defaultLatLng = LatLng(32.0853, 34.7818)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 8f))

        // Request or enable location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        loadEventsAndShowPins()
    }

    private fun enableUserLocation() {
        try {
            map.isMyLocationEnabled = true

            // Auto-center on user location once
            map.setOnMyLocationChangeListener { location ->
                if (!hasCenteredOnUser) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                    hasCenteredOnUser = true
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun loadEventsAndShowPins() {
        db.child("KindNest").child("charity_events")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(this@MapEventsActivity, "No events found", Toast.LENGTH_SHORT).show()
                        return
                    }

                    for (eventSnap in snapshot.children) {
                        val event = eventSnap.getValue(CharityEvent::class.java)
                        if (event != null && event.latitude != null && event.longitude != null) {
                            val latLng = LatLng(event.latitude, event.longitude)
                            map.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(event.title)
                            )
                        }
                    }

                    map.setOnMarkerClickListener { marker ->
                        val latLng = marker.position
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        marker.showInfoWindow()
                        true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MapEventsActivity,
                        "Error loading events: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
