package com.SheeraNergaon.KindNest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    protected open val layoutId: Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navView: BottomNavigationView? = findViewById(R.id.bottom_navigation)
        navView?.itemIconTintList = null

        // Set the correct selected item based on current activity
        navView?.menu?.findItem(
            when (this) {
                is MainActivity -> R.id.nav_home
                is CalendarActivity -> R.id.nav_calendar
                is UploadActivity -> R.id.nav_add
                is ProfileActivity -> R.id.nav_user
                else -> R.id.nav_home
            }
        )?.isChecked = true

        navView?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, UploadActivity::class.java))
                    true
                }
                R.id.nav_user -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }.also {
                // Finish current activity to prevent stacking
                if (it && this !is MainActivity) finish()
            }
        }
    }
}
