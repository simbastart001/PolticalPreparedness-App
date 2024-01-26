package com.example.android.politicalpreparedness.election

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import com.example.android.politicalpreparedness.utils.Result

/**
 * @DrStart:    ElectionRepository is the single source of truth for the ElectionViewModel and
 *             the ElectionsFragment. It will be responsible for refreshing the data from the
 *             network and storing it in the database. It will also be responsible for serving
 *             the data from the database to the ViewModel. It will also be responsible for
 *             serving the data from the database to the ElectionsFragment. Enables for offline caching.
 *             It will also be responsible for deleting the data from the database. It will also
 *             be responsible for serving the data from the database to various fragments.
 * */
class ElectionsRepository(private val database: ElectionDatabase) {

    private var service: CivicsApiService = CivicsApi.retrofitService
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    val voterInfo = MutableLiveData<VoterInfoResponse>()

    fun getSaveElections(): LiveData<List<Election>> = database.electionDao.getSavedElections()

    suspend fun getElection(id: Long): Election? = withContext(dispatcher) {
        return@withContext database.electionDao.get(id)
    }

    suspend fun insert(election: Election) {
        withContext(dispatcher) {
            database.electionDao.insert(election)
        }
    }

    suspend fun getVoterInfo(address: String, electionId: Long): Result<VoterInfoResponse> {
        return try {
            val result = service.getVoterInfo(address, electionId)
            voterInfo.postValue(result)
            Result.Success(service.getVoterInfo(address, electionId))
        } catch (e: Exception) {
            Log.d(TAG, "Error getVoterInfo: " + e.message)
            Result.Error(e.localizedMessage)
        }
    }

    suspend fun insertElection(election: Election) {
        withContext(Dispatchers.IO) {
            database.electionDao.insert(election)
        }
    }

    suspend fun refreshElections(): Result<ElectionResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val result = service.getUpcomingElections()
                database.electionDao.insertElections(result.elections)
                Result.Success(service.getUpcomingElections())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error refreshElections: " + e.message)
            Result.Error(e.localizedMessage)
        }
    }

    suspend fun delete(election: Election) {
        withContext(dispatcher) {
            database.electionDao.delete(election)
        }
    }

}

private const val TAG = "ElectionsRepository"