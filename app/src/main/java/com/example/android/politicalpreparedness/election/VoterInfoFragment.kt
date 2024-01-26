package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

/**
 * @DrStart:    VoterInfoFragment will display the voter info retrieved from the API. I will
 *             need to implement a ViewModel for this fragment to display the voter info. I will
 *             also need to implement a click listener for the "follow election" button. I will
 *             also need to implement a click listener for the "official's website" button.
 * */
class VoterInfoFragment : Fragment() {

    private lateinit var viewModel: VoterInfoViewModel
    private lateinit var binding: FragmentVoterInfoBinding
    private val args: VoterInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_voter_info,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val election = args.argElection
        val viewModelFactory = VoterInfoViewModelFactory(election, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(VoterInfoViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.url.observe(viewLifecycleOwner, Observer {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        })

        return binding.root
    }
}
