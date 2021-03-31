package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment : Fragment() {

    private lateinit var electionsViewModel: ElectionsViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val application = requireActivity().application
        val database = ElectionDatabase.getInstance(application).electionDao
        val viewModelFactory = ElectionsViewModelFactory(application, database)
        electionsViewModel =
                ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)

        val binding = FragmentElectionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.electionsViewModel = electionsViewModel

        val upcomingElectionListAdapter = ElectionListAdapter(ElectionListener {
            electionsViewModel.showElectionDetails(it)
        })
        binding.upcomingElectionsList.adapter = upcomingElectionListAdapter
        binding.upcomingElectionsList.layoutManager = LinearLayoutManager(requireContext())
        electionsViewModel.upcomingElections.observe(viewLifecycleOwner, { elections ->
            elections?.let {
                upcomingElectionListAdapter.submitList(elections)
            }
        })

        val savedElectionListAdapter = ElectionListAdapter(ElectionListener {
            electionsViewModel.showElectionDetails(it)
        })
        binding.savedElectionsList.adapter = savedElectionListAdapter
        binding.savedElectionsList.layoutManager = LinearLayoutManager(requireContext())
        electionsViewModel.savedElections.observe(viewLifecycleOwner, { elections ->
            elections?.let {
                savedElectionListAdapter.submitList(elections)
            }
        })

        electionsViewModel.navigateToSelectedElection.observe(viewLifecycleOwner, { election ->
            if (election != null) {
                this.findNavController().navigate(ElectionsFragmentDirections.actionShowVoterInfo(election.id, election.division))
                electionsViewModel.isElectionComplete()
            }
        })
        return binding.root
    }
}