package com.SheeraNergaon.KindNest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.SheeraNergaon.KindNest.databinding.ActivityCalendarBinding
import com.SheeraNergaon.KindNest.utilities.Constants.getUserLevel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CalendarActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_calendar

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var db: DatabaseReference
    private lateinit var adapter: EventAdapter
    private val events = mutableListOf<CharityEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.bind(findViewById(R.id.calendar_root))
        binding.calendarFABViewMap.setOnClickListener {
            startActivity(Intent(this, MapEventsActivity::class.java))
        }


        db = FirebaseDatabase.getInstance().reference

        adapter = EventAdapter(events)
        binding.calendarRecycler.layoutManager = LinearLayoutManager(this)
        binding.calendarRecycler.adapter = adapter

        loadUserLevelAndToggleCreate()
        loadEvents()

        binding.calendarBTNCreateEvent.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserLevelAndToggleCreate()
    }

    private fun loadEvents() {
        deletePastEvents()
        db.child("KindNest").child("charity_events")
            .orderByChild("date")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    events.clear()
                    for (child in snapshot.children) {
                        val event = child.getValue(CharityEvent::class.java)
                        if (event != null && child.key != null) {
                            event.id = child.key!!
                            events.add(event)
                        }
                    }
                    adapter.updateList(events)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CalendarActivity, "Failed to load events", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadUserLevelAndToggleCreate() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) return

        val userId = currentUser.uid

        db.child("KindNest").child("users").child(userId).child("points")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val points = snapshot.getValue(Int::class.java) ?: 0
                    val level = getUserLevel(points)

                    if (level == "Tree 🌳") {
                        binding.calendarBTNCreateEvent.visibility = View.VISIBLE
                    } else {
                        binding.calendarBTNCreateEvent.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun deletePastEvents() {
        db.child("KindNest").child("charity_events")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val today = System.currentTimeMillis()

                    for (child in snapshot.children) {
                        val event = child.getValue(CharityEvent::class.java)
                        if (event != null && event.date != null) {
                            try {
                                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val eventDate = format.parse(event.date)

                                if (eventDate != null && eventDate.time < today) {
                                    child.ref.removeValue()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CalendarActivity, "Failed to clean past events", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
