package com.SheeraNergaon.KindNest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyEventsActivity : BaseActivity() {
    override val layoutId: Int
        get() = R.layout.activity_my_events

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyUpcomingEventsAdapter
    private val eventList = mutableListOf<CharityEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView = findViewById(R.id.upcomingEventsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyUpcomingEventsAdapter(eventList) { event ->
            cancelEventSignup(event)
        }
        recyclerView.adapter = adapter
        loadSignedUpEvents()
    }

    private fun loadSignedUpEvents() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val db = FirebaseDatabase.getInstance().reference.child("KindNest")

        val userEventsRef = db
            .child("users")
            .child(userId)
            .child("signedUpEvents")

        userEventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@MyEventsActivity, "No upcoming events found", Toast.LENGTH_SHORT).show()
                    return
                }

                for (eventSnapshot in snapshot.children) {
                    val eventId = eventSnapshot.key
                    if (!eventId.isNullOrEmpty()) {
                        val eventDetailRef = db
                            .child("charity_events")
                            .child(eventId)

                        eventDetailRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(eventDetailSnapshot: DataSnapshot) {
                                val event = eventDetailSnapshot.getValue(CharityEvent::class.java)
                                event?.let {
                                    eventList.add(it)
                                    adapter.notifyItemInserted(eventList.size - 1)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("MyEventsActivity", "Error loading event details: ${error.message}")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyEventsActivity", "Error loading user events: ${error.message}")
            }
        })
    }

    private fun cancelEventSignup(event: CharityEvent) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid

        val db = FirebaseDatabase.getInstance().reference.child("KindNest")

        val userEventRef = db
            .child("users")
            .child(userId)
            .child("signedUpEvents")
            .child(event.id!!)

        userEventRef.removeValue().addOnSuccessListener {
            adapter.remove(event)
            Toast.makeText(this, "Event cancelled successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to cancel event", Toast.LENGTH_SHORT).show()
        }
    }
}
