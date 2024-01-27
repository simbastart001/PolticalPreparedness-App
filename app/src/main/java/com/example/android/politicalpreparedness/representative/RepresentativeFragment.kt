package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.Locale

/**
 * @DrStart:             This class implements a [Fragment] which contains a list of [Representative]s. The
 *                      [Representative]s are displayed in a [RecyclerView] using [RepresentativeListAdapter]. The
 *                      [Representative]s are fetched from the [RepresentativeViewModel]. The [Representative]s are
 *                      filtered by state and address. The user can search for [Representative]s by address or by
 *                      using their current location. The user can click on a [Representative] to view more details. The
 *                      user can click on a [Representative]'s social media link to view the social media page.
 */

class RepresentativeFragment : Fragment() {
    private lateinit var binding: FragmentRepresentativeBinding
    private val viewModel: RepresentativeViewModel by viewModels()

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_representative,
            container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        val representativeAdapter = RepresentativeListAdapter()

        binding.representativesRecycler.adapter = representativeAdapter

        viewModel.locationValidationError.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                // Reset the error after showing it
                viewModel.isLocationValid()
            }
        })

        viewModel.address.observe(viewLifecycleOwner, Observer { address ->
            binding.address = address
        })
        viewModel.representatives.observe(viewLifecycleOwner, Observer { representatives ->
            representativeAdapter.submitList(representatives)
        })

        binding.buttonLocation.setOnClickListener {
            checkLocationPermissions()
        }
        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            searchRepresentatives()
        }

        return binding.root
    }

    /**
     * @DrStart:     Search by state and address
     */
    fun searchRepresentatives() {
        val address = Address(
            binding.addressLine1.text.toString(),
            binding.addressLine2.text.toString(),
            binding.city.text.toString(),
            binding.state.selectedItem.toString(),
            binding.zip.text.toString()
        )
        viewModel.updateAddress(address)
        viewModel.getRepresentatives(address)
    }

    /**
     * @DrStart:     Check if location permissions are granted and if so, fetch the last location
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, enable My Location layer
                    enableMyLocation()
                } else {
                    // Permission denied, show error message
                    showPermissionDeniedError()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * @DrStart:     Show a Snackbar explaining why location permission is required. If the user clicks on the
     *             "Settings" button, take them to the app settings page. If the user clicks on the "OK" button,
     *             request location permission again.
     */
    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, enable My Location layer
                enableMyLocation()
            }

            else -> requestPermission()
        }
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    /**
     * @DrStart:     Check if location services are enabled. If not, show a Snackbar explaining why location
     *             services are required. If the user clicks on the "Settings" button, take them to the
     *             location settings page. If the user clicks on the "OK" button, request location services
     *             again.
     */
    private fun getLocation(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                handleResolvableApiException(exception)
            } else {
                Snackbar.make(
                    binding.root,
                    "Location is required for this feature",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    getLocation()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fetchLastLocation()
            }
        }
    }

    private fun handleResolvableApiException(exception: ResolvableApiException) {
        try {
            startIntentSenderForResult(
                exception.resolution.intentSender,
                REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
            )
        } catch (sendEx: IntentSender.SendIntentException) {
            Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
        }
    }

    /**
     * @DrStart:     Enable My Location layer if the permission has been granted. Otherwise, display a
     *            snackbar explaining that the user needs location permissions in order to play.
     */

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLastLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    /**
     * @DrStart:     Fetch the last location
     */
    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        val locationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                updateLocationUI(location)
            } else {
                // Handle the case when location is null
                Snackbar.make(
                    binding.root,
                    "Location not found, try again later.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * @DrStart:     Update the UI with new location data
     */

    // Inside RepresentativeFragment

    private fun updateLocationUI(location: Location) {
        lifecycleScope.launch {
            val address = geoCodeLocation(location)
            address.let { addr ->
                viewModel.validateAddress(addr) // Validate the address
                if (viewModel.locationValidationError.value == null) {
                    // If there's no error, update the address and UI
                    viewModel.updateAddress(addr)
                    binding.addressLine1.setText(addr.line1)
                    binding.addressLine2.setText(addr.line2)
                    binding.city.setText(addr.city)
                    updateSpinnerSelection(addr.state)
                    binding.zip.setText(addr.zip)
                    viewModel.getRepresentatives(addr)
                }
            }
        }
    }

    /**
     * @DrStart:     Geocode the location. This method is called from a coroutine.
     */
    private suspend fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return withContext(Dispatchers.IO) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                ?.let { address ->
                    Address(
                        address.thoroughfare ?: "",
                        address.subThoroughfare ?: "",
                        address.locality ?: "",
                        address.adminArea ?: "",
                        address.postalCode ?: ""
                    )
                } ?: throw IOException("No address found")
        }
    }

    private fun updateSpinnerSelection(state: String) {
        val states = resources.getStringArray(R.array.states)
        val selectedStateIndex = states.indexOf(state).takeIf { it >= 0 } ?: 0
        binding.state.setSelection(selectedStateIndex)
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    /**
     * @DrStart:     Show a Snackbar explaining why location permission is required
     */
    private fun showPermissionDeniedError() {
        Snackbar.make(
            binding.root,
            "Location permission is required!",
            Snackbar.LENGTH_LONG
        ).show()
    }

    /**
     * @DrStart:
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            getLocation(false)
        }
    }


}

private const val TAG = "RepresentativeFragment"