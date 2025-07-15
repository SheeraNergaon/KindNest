package com.SheeraNergaon.KindNest

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private var events: List<CharityEvent>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.itemEventTitle)
        val date: TextView = itemView.findViewById(R.id.itemEventDate)
        val location: TextView = itemView.findViewById(R.id.itemEventLocation)
        val description: TextView = itemView.findViewById(R.id.itemEventDescription)
        val signUpButton: Button = itemView.findViewById(R.id.signUpButton)
        val editButton: Button = itemView.findViewById(R.id.editButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        holder.title.text = event.title
        holder.date.text = "Date: ${event.date}"
        holder.location.text = "Location: ${event.location}"
        holder.description.text = event.description

        holder.signUpButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EventSignupActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            intent.putExtra("EVENT_TITLE", event.title)
            context.startActivity(intent)
        }

        if (event.createdBy == currentUserId) {
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, EditEventActivity::class.java)
                intent.putExtra("event", event) // assuming CharityEvent implements Serializable
                context.startActivity(intent)
            }
        } else {
            holder.editButton.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = events.size

    fun updateList(newEvents: List<CharityEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
