package com.example.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.databinding.ItemElectionBinding
import com.example.android.politicalpreparedness.network.models.Election

/**
 * @DrStart:    ElectionListAdapter.kt - RecyclerView Adapter for displaying the saved elections.
 *              This class uses Data Binding to efficiently display data.
 * */
class ElectionListAdapter(private val electionListener: ElectionListener) :
    ListAdapter<Election, ElectionListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), electionListener)
    }

    class ViewHolder private constructor(private val binding: ItemElectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(election: Election, electionListener: ElectionListener) {
            binding.election = election
            binding.executePendingBindings()
            binding.clickListener = electionListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemElectionBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Election>() {
    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem.id == newItem.id
    }
}

class ElectionListener(var block: (election: Election) -> Unit) {
    fun onElectionClicked(election: Election) = block(election)
}
