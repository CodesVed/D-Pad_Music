package com.example.mediaplayer_project

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(val context: Context, val arrayList: ArrayList<Song>) : RecyclerView.Adapter<MyAdapter.MediaViewHolder>() {
    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemCount = itemView.findViewById<TextView>(R.id.txtNumber)
        val title = itemView.findViewById<TextView>(R.id.txtTitle)
        val artist = itemView.findViewById<TextView>(R.id.txtArtist)
        val duration = itemView.findViewById<TextView>(R.id.txtDuration)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.media_item, parent, false)
        return MediaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val currentItem = arrayList[position]

        holder.itemCount.text = "${(position+1)}"
        holder.title.text = currentItem.title
        holder.artist.text = currentItem.artist
        holder.artist.isSelected = true
        holder.duration.text = formatDuration(currentItem.duration)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    internal fun formatDuration(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("${hours}:${minutes}:${seconds}")
        } else {
            String.format("${minutes}:${seconds}")
        }
    }
}