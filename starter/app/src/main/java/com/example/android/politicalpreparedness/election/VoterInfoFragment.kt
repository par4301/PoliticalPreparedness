package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val application = requireActivity().application
        val database = ElectionDatabase.getInstance(application).electionDao

        val args = VoterInfoFragmentArgs.fromBundle(requireArguments())
        val electionId = args.argElectionId
        val division = args.argDivision
        val viewModelFactory = VoterInfoViewModelFactory(electionId, division, database)
        val viewModel =
                ViewModelProvider(this, viewModelFactory).get(VoterInfoViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.address.observe(viewLifecycleOwner, { address ->
            binding.addressGroup.isInvisible = address == null
        })
        viewModel.ballotInfo.observe(viewLifecycleOwner, { ballotInfo ->
            binding.stateBallot.isInvisible = ballotInfo == null
        })
        viewModel.votingLocations.observe(viewLifecycleOwner, { votingLocations ->
            binding.stateLocations.isInvisible = votingLocations == null
        })

        binding.stateBallot.setOnClickListener {
            val url = viewModel.ballotInfo.value
            if (url != null) {
                fetchWebsite(url)
            }
        }
        binding.stateLocations.setOnClickListener {
            val url = viewModel.votingLocations.value
            if (url != null) {
                fetchWebsite(url)
            }
        }

        viewModel.saved.observe(viewLifecycleOwner, {
            when (it) {
                true -> {
                    binding.followUnfollowButton.apply {
                        text = getString(R.string.unfollow_election_button)
                        setOnClickListener {
                            viewModel.removeElectionById()
                        }
                    }
                }
                false -> {
                    binding.followUnfollowButton.apply {
                        text = getString(R.string.follow_election_button)
                        setOnClickListener {
                            viewModel.saveElectionInDatabase()
                        }
                    }
                }
            }
        })
        return binding.root
    }

    private fun fetchWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

}