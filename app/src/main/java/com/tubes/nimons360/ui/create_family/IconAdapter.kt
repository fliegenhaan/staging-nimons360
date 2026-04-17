package com.tubes.nimons360.ui.create_family

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.tubes.nimons360.R

class IconAdapter(
    private val iconUrls: List<String>,
    private val onIconSelected: (String) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    private var selectedPosition = -1

    fun getSelectedIconUrl(): String? {
        return if (selectedPosition in iconUrls.indices) iconUrls[selectedPosition] else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family_icon, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(iconUrls[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedPosition)
            onIconSelected(iconUrls[selectedPosition])
        }
    }

    override fun getItemCount() = iconUrls.size

    inner class IconViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        private val ivCheckmark: ImageView = view.findViewById(R.id.ivCheckmark)

        fun bind(url: String, isSelected: Boolean) {
            ivIcon.load(url)
            ivIcon.isSelected = isSelected
            ivCheckmark.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
}
