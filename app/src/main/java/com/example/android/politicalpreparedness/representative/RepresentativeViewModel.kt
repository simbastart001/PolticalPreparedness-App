package com.example.android.politicalpreparedness.representative

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Address
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

class RepresentativeViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) :
    AndroidViewModel(application) {

    private val _addressline1 = savedStateHandle.getLiveData<String>("line1")
    val addressline1: LiveData<String>
        get() = _addressline1

    private val _addressline2 = savedStateHandle.getLiveData<String>("line2")
    val addressline2: LiveData<String>
        get() = _addressline2

    private val _city = savedStateHandle.getLiveData<String>("city")
    val city: LiveData<String>
        get() = _city

    private val _state = savedStateHandle.getLiveData<String>("state")
    val state: LiveData<String>
        get() = _state

    private val _zip = savedStateHandle.getLiveData<String>("zip")
    val zip: LiveData<String>
        get() = _zip

    fun updateAddressLine1(line1: String) {
        _addressline1.value = line1
        savedStateHandle.set("line1", line1)
    }

    fun updateAddressLine2(line2: String) {
        _addressline2.value = line2
        savedStateHandle.set("line2", line2)
    }

    fun updateCity(city: String) {
        _city.value = city
        savedStateHandle.set("city", city)
    }

    fun updateState(state: String) {
        _state.value = state
        savedStateHandle.set("state", state)
    }

    fun updateZip(zip: String) {
        _zip.value = zip
        savedStateHandle.set("zip", zip)
    }


    private val _address = MutableLiveData<Address>()
    val address: MutableLiveData<Address>
        get() = _address

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    fun gettRepresentativesList(): List<Representative>? {
        return _representatives.value
    }

    var client: CivicsApiService = CivicsApi.retrofitService

    // LiveData to hold validation errors
    private val _locationValidationError = MutableLiveData<String?>()
    val locationValidationError: LiveData<String?>
        get() = _locationValidationError

    fun restoreRepresentatives(representatives: List<Representative>) {
        _representatives.value = representatives
    }

    // Call this function to validate the address
    fun validateAddress(address: Address) {
        if (address.line1!!.isBlank() || address.zip.isBlank() || address.city.isBlank()) {
            _locationValidationError.value = "Location is not valid. Please try again!."
        } else {
            _locationValidationError.value = null // No errors, location is valid
        }
    }

    fun isLocationValid(): Boolean {
        return _locationValidationError.value == null
    }

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

}

class Factory(val app: Application, private val savedStateHandle: SavedStateHandle) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepresentativeViewModel::class.java)) {
            return RepresentativeViewModel(app, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}