package com.SheeraNergaon.KindNest

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.SheeraNergaon.KindNest.databinding.ActivityMainBinding
import com.SheeraNergaon.KindNest.utilities.Constants.getUserLevel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_main

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseReference
    private var currentPoints = 0
    private var currentDeed: DeedEntry? = null

    private lateinit var adapter: DeedAdapter
    private val completedDeeds = mutableListOf<DeedEntry>()
    private var level = "Seed"

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.bind(findViewById(android.R.id.content))

        db = FirebaseDatabase.getInstance().reference

        adapter = DeedAdapter(completedDeeds)
        binding.mainRecyclerDone.layoutManager = LinearLayoutManager(this)
        binding.mainRecyclerDone.adapter = adapter

        loadUserData()
        loadRandomDeed()
        loadCompletedDeeds()

        binding.mainBTNComplete.setOnClickListener {
            markDeedAsCompleted()
        }

        binding.mainBTNRefresh.setOnClickListener {
            loadRandomDeed()
        }

    }

    private fun loadUserData() {
        val uid = currentUser?.uid ?: return
        db.child("KindNest").child("users").child(uid).child("points")
            .get()
            .addOnSuccessListener {
                currentPoints = it.getValue(Int::class.java) ?: 0
                level = getUserLevel(currentPoints)
                binding.mainLBLImpact.text = "Your impact: $currentPoints points"
                binding.mainLBLLevel.text = "Your level: $level"
            }
    }

    private fun loadRandomDeed() {
        db.child("KindNest").child("daily_deeds")
            .get()
            .addOnSuccessListener { snapshot ->
                val deeds = snapshot.children.mapNotNull { it.getValue(DeedEntry::class.java) }
                if (deeds.isNotEmpty()) {
                    currentDeed = deeds.random()
                    binding.mainLBLDeed.text = "${currentDeed?.text} (+${currentDeed?.points} points)"
                }
            }
    }

    private fun loadCompletedDeeds() {
        val uid = currentUser?.uid ?: return
        db.child("KindNest").child("users").child(uid).child("completed_deeds")
            .get()
            .addOnSuccessListener { snapshot ->
                completedDeeds.clear()
                for (child in snapshot.children) {
                    val deed = child.getValue(DeedEntry::class.java)
                    if (deed != null) {
                        completedDeeds.add(deed)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun markDeedAsCompleted() {
        val original = currentDeed ?: return
        val deed = if (original.timestamp == 0L) {
            original.copy(timestamp = System.currentTimeMillis())
        } else {
            original
        }

        completedDeeds.add(deed)
        adapter.notifyItemInserted(completedDeeds.size - 1)
        currentPoints += deed.points
        binding.mainLBLImpact.text = "Your impact: $currentPoints points"

        level = getUserLevel(currentPoints)
        binding.mainLBLLevel.text = "Your level: $level"

        val uid = currentUser?.uid ?: return
        val userRef = db.child("KindNest").child("users").child(uid)
        userRef.child("points").setValue(currentPoints)
        userRef.child("completed_deeds").push().setValue(deed)

        Toast.makeText(this, "Deed completed!", Toast.LENGTH_SHORT).show()
        loadRandomDeed()

    }


}
