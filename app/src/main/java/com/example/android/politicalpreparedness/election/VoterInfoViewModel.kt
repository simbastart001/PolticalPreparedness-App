package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.election.ElectionsRepository
import com.example.android.politicalpreparedness.utils.Result
import com.example.android.politicalpreparedness.utils.SingleLiveEvent
import kotlinx.coroutines.launch

/**
 * @DrStart:    ElectionViewModel will be responsible for preparing and managing the data for
 *            the UI. It will expose the data to the UI through LiveData objects. It will also
 *            be responsible for handling the events from the UI and updating the UI as required.
 *            It will also be responsible for handling the navigation events from the UI.
 * */
class VoterInfoViewModel(val election: Election, app: Application) : AndroidViewModel(app) {

    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val openUrl: SingleLiveEvent<String> = SingleLiveEvent()

    private val database = ElectionDatabase.getInstance(app)
    private val electionsRepository = ElectionsRepository(database)

    var url = MutableLiveData<String>()

    private var _isVote = MutableLiveData<Boolean>()
    val isVote: LiveData<Boolean>
        get() = _isVote

    private var _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    init {
        viewModelScope.launch {
            if (election.division.state.isNotEmpty()) {
                _isVote.value = electionsRepository.getElection(election.id.toLong()) != null
                val address = "${election.division.country},${election.division.state}"
                val result = electionsRepository.getVoterInfo(address, election.id.toLong())
                when (result) {
                    is Result.Success -> {
                        _voterInfo.value = result.data
                    }

                    else -> {
                        showToast.value = app.getString(R.string.error_upcoming_election)
                    }
                }
            }
        }
    }

    fun saveElection(election: Election) {
        viewModelScope.launch {
            val isElectionSaved = electionsRepository.getElection(election.id.toLong()) != null
            if (isElectionSaved) {
                electionsRepository.delete(election)
            } else {
                electionsRepository.insert(election)
            }
            _isVote.value = !isElectionSaved
        }
    }


    fun onUrlClick(url: String) {
        Log.e("HiepNCH", "Test")
        this.url.value = url
        openUrl.value = url
    }

}
