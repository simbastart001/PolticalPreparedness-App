package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener


/**
 * @DrStart:    In this fragment, I will implement the RecyclerView, Adapter and ViewModel. I will also
 *             create the click listeners for navigating to the voter info fragment. I will also
 *             need to create the observer for the saved elections. I will also need to create
 *             the click listener for the upcoming elections. Again, I will need to create the
 *             observer for the upcoming elections.
 * */

class ElectionsFragment : Fragment() {

    // Declare ViewModel
    private lateinit var viewModel: ElectionsViewModel
    private lateinit var binding: FragmentElectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireNotNull(this.activity).application
        val viewModelFactory = ElectionsViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_election,
            container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val upcomingElectionListAdapter = ElectionListAdapter(ElectionListener {
            findNavController().navigate(
                ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it)
            )
        })
        binding.upcomingElectionsRecyclerView.adapter = upcomingElectionListAdapter

        viewModel.upcomingElections.observe(viewLifecycleOwner, Observer { elections ->
            // Update data
            upcomingElectionListAdapter.submitList(elections)
        })

        val savedElectionListAdapter = ElectionListAdapter(ElectionListener {
            findNavController().navigate(
                ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it)
            )
        })
        binding.savedElectionsRecyclerView.adapter = savedElectionListAdapter
        viewModel.savedElections.observe(viewLifecycleOwner, Observer { elections ->
            // Update data
            savedElectionListAdapter.submitList(elections)
        })

        return binding.root
    }
}