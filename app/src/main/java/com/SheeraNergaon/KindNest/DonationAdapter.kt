package com.SheeraNergaon.KindNest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class DonationAdapter(
    private val causes: List<DonationCause>,
    private val onDonateClick: (DonationCause) -> Unit
) : RecyclerView.Adapter<DonationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.donation_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cause = causes[position]
        holder.image.setImageResource(cause.imageResId)
        holder.itemView.setOnClickListener { onDonateClick(cause) }
    }

    override fun getItemCount(): Int = causes.size
}
