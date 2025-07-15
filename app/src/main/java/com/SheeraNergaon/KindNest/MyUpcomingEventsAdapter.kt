package com.SheeraNergaon.KindNest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyUpcomingEventsAdapter(
    private var events: MutableList<CharityEvent>,
    private val onCancelClick: (CharityEvent) -> Unit
) : RecyclerView.Adapter<MyUpcomingEventsAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.cardTitle)
        val date: TextView = itemView.findViewById(R.id.cardDate)
        val location: TextView = itemView.findViewById(R.id.cardLocation)
        val cancelButton: Button = itemView.findViewById(R.id.cardCancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title
        holder.date.text = "📅 ${event.date}"
        holder.location.text = "📍 ${event.location}"

        holder.cancelButton.setOnClickListener {
            onCancelClick(event)
        }
    }

    override fun getItemCount(): Int = events.size

    fun remove(event: CharityEvent) {
        val index = events.indexOfFirst { it.id == event.id }
        if (index != -1) {
            events.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
