package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class ElectionsViewModel(
        application: Application,
        electionDao: ElectionDao
): AndroidViewModel(application) {

    val upcomingElections = MutableLiveData<List<Election>>()
    val savedElections = electionDao.getAllElections()

    private val _navigateToSelectedElection = MutableLiveData<Election>()
    val navigateToSelectedElection: LiveData<Election>
        get() = _navigateToSelectedElection

    init {
        bringListOfUpcomingElections()
    }

    private fun bringListOfUpcomingElections() {
        viewModelScope.launch {
            try {
                val listResult = CivicsApi.retrofitService.getListOfElections().elections
                if (listResult.isNotEmpty()) {
                    upcomingElections.value = listResult
                }
            } catch (e: Exception) {
                upcomingElections.value = ArrayList()
            }
        }
    }

    fun showElectionDetails(election: Election) {
        _navigateToSelectedElection.value = election
    }

    fun isElectionComplete() {
        _navigateToSelectedElection.value = null
    }
}