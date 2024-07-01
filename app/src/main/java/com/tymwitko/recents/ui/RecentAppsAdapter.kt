package com.tymwitko.recents.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.tymwitko.recents.R
import com.tymwitko.recents.accessors.IconAccessor
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.dataclasses.App
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecentAppsAdapter(
    private val context: Context,
    private val dataset: List<App>,
    private val kill: (String) -> Unit
) : RecyclerView.Adapter<RecentAppsAdapter.ItemViewHolder>(), KoinComponent {

    private val intentSender: IntentSender by inject()
    private val iconAccessor: IconAccessor by inject()

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameView: TextView = view.findViewById(R.id.item_title)
        val packageNameView: TextView = view.findViewById(R.id.package_name)
        val cardView: MaterialCardView = view.findViewById(R.id.card_view)
        val iconView: ImageView = view.findViewById(R.id.icon)
        val killButton: ImageButton = view.findViewById(R.id.killButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.recent_app_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.appNameView.text = item.name
        holder.packageNameView.text = item.packageName
        holder.cardView.setOnClickListener {
            if (!intentSender.launchSelectedApp(context, item.packageName))
                Toast.makeText(context, R.string.failed_to_launch, Toast.LENGTH_LONG).show()
        }
        holder.iconView.setImageDrawable(iconAccessor.getAppIcon(context, item.packageName))
        holder.killButton.setOnClickListener {
            (holder.packageNameView.text as? String)?.let { it1 -> kill.invoke(it1) }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}
