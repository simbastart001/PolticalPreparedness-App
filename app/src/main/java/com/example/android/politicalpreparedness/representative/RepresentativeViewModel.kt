package com.example.android.politicalpreparedness.representative

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.election.ElectionsViewModel
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @DrStart:    RepresentativeViewModel.kt - ViewModel for the Representative fragment. The RepresentativeViewModel should include:
 *             - An internal MutableLiveData for holding an address used for fetching representatives
 *               and an external immutable LiveData for observing representatives.
 *             - A function to fetch representatives from the API and refresh the value of the internal MutableLiveData
 *             - A function to get address from geo location
 *             - A function to get address from individual fields
 *             - A function to combine address fields into a single string
 *             - A function to validate fields for completeness and warn users if fields are invalid.
 * */

class RepresentativeViewModel(application: Application) : AndroidViewModel(application) {

    private val _address = MutableLiveData<Address>()
    val address: MutableLiveData<Address>
        get() = _address

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    var client: CivicsApiService = CivicsApi.retrofitService

    fun updateAddress(address: Address) {
        _address.value = address
    }

    fun getRepresentatives(address: Address) {
        viewModelScope.launch {
            try {
                val (offices, officials) = client.getRepresentatives(address.toFormattedString())
                _representatives.postValue(offices.flatMap { office ->
                    office.getRepresentatives(
                        officials
                    )
                })
            } catch (e: Exception) {
                _representatives.value = emptyList()
                Timber.i("Elections", "RepresentativeViewModel $e")
            }
        }
    }

    fun updateCurrentAddress(
        line1: String,
        line2: String?,
        city: String,
        state: String,
        zip: String
    ) {
        _address.value = Address(line1, line2, city, state, zip)
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ElectionsViewModel::class.java)) {
                return ElectionsViewModel(app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}