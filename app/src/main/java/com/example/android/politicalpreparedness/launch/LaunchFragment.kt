package com.example.android.politicalpreparedness.launch

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.databinding.FragmentLaunchBinding

/**
 * @DrStart:    LaunchFragment.kt - Launch Fragment to display the loading screen and check for
 *             internet connection. This fragment will navigate to the next screen if the device
 *             has internet connection or display an error message. This fragment will also check
 *             if there are elections saved in the database and navigate to the appropriate screen.
 * */
class LaunchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLaunchBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.representativeButton.setOnClickListener { navToRepresentatives() }
        binding.upcomingButton.setOnClickListener { navToElections() }

        return binding.root
    }

    private fun navToElections() {
        this.findNavController()
            .navigate(LaunchFragmentDirections.actionLaunchFragmentToElectionsFragment())
    }

    private fun navToRepresentatives() {
        this.findNavController()
            .navigate(LaunchFragmentDirections.actionLaunchFragmentToRepresentativeFragment())
    }

}
