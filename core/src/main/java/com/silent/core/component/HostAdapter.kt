package com.silent.core.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.silent.core.R
import com.silent.core.databinding.HostCardBinding
import com.silent.core.databinding.HostCardReverseBinding
import com.silent.core.databinding.NewHostLayoutBinding
import com.silent.core.podcast.Host
import com.silent.core.podcast.NEW_HOST
import com.silent.core.utils.ImageUtils


class HostAdapter(
    val hosts: ArrayList<Host>,
    val isEdit: Boolean = false,
    val hostSelected: (Host) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NORMAL_VIEW = 2
    private val REVERSE_VIEW = 1
    val NEW_HOST_VIEW = 3

    init {
        if (isEdit) hosts.add(Host.NEWHOST)
    }

    inner class NewHostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            NewHostLayoutBinding.bind(itemView).run {
                Glide.with(itemView.context).load(ImageUtils.getRandomHostPlaceHolder())
                    .into(hostPlaceHolder)
                itemView.setOnClickListener {
                    hostSelected(hosts[bindingAdapterPosition])
                }
            }
        }
    }

    inner class HostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            HostCardBinding.bind(itemView).run {
                val context = itemView.context
                val host = hosts[bindingAdapterPosition]
                itemView.setOnClickListener {
                    hostSelected(host)
                }
                Glide.with(context)
                    .load(host.profilePic)
                    .error(ImageUtils.getRandomIcon())
                    .into(hostPhoto)
                hostName.text = host.name
                hostDescription.text = "@${host.user}"
            }

        }
    }

    inner class ReverseHostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            HostCardReverseBinding.bind(itemView).run {
                val context = itemView.context
                val host = hosts[adapterPosition]
                Glide.with(context)
                    .load(host.profilePic)
                    .error(ImageUtils.getRandomIcon())
                    .into(hostPhoto)
                hostName.text = host.name
                hostDescription.text = "@${host.user}"
            }


        }

    }

    override fun getItemViewType(position: Int): Int {
        return when {
            hosts[position].name == NEW_HOST -> NEW_HOST_VIEW
            position % 2 == 0 -> REVERSE_VIEW
            else -> NORMAL_VIEW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NEW_HOST_VIEW -> NewHostViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.new_host_layout, parent, false)
            )
            REVERSE_VIEW -> ReverseHostViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.host_card_reverse, parent, false)
            )
            else -> HostViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.host_card, parent, false)
            )
        }
    }

    override fun getItemCount() = hosts.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewHostViewHolder -> {
                holder.bind()
            }
            is HostViewHolder -> {
                holder.bind()
            }
            is ReverseHostViewHolder -> {
                holder.bind()
            }
        }
    }

    fun updateHost(host: Host) {
        hosts.add(host)
        notifyItemInserted(itemCount)
    }

    fun refresh(hosts: ArrayList<Host>) {
        hosts.clear()
        hosts.addAll(hosts)
        if (isEdit) hosts.add(Host.NEWHOST)
        notifyDataSetChanged()
    }

}