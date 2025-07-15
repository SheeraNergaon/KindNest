package com.SheeraNergaon.KindNest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DeedAdapter(private var deeds: List<DeedEntry>) :
    RecyclerView.Adapter<DeedAdapter.DeedViewHolder>() {

    inner class DeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deedText: TextView = itemView.findViewById(R.id.deed_text)
        val deedTimestamp: TextView = itemView.findViewById(R.id.deed_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deed_entry, parent, false)
        return DeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeedViewHolder, position: Int) {
        val deed = deeds[position]
        holder.deedText.text = deed.text
        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(deed.timestamp))
        holder.deedTimestamp.text = formattedDate
    }

    override fun getItemCount(): Int = deeds.size

    fun updateList(newDeeds: List<DeedEntry>) {
        deeds = newDeeds.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}
