package com.example.android.politicalpreparedness.representative

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
    private val viewModel by viewModels<RepresentativeViewModel>()
    private lateinit var representativeAdapter: RepresentativeListAdapter


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

        binding.lifecycleOwner = this

        representativeAdapter = RepresentativeListAdapter()
        binding.representativesRecycler.adapter = representativeAdapter
        binding.address = Address("", "", "", "", "")

        binding.buttonLocation.setOnClickListener {
            checkLocationPermissions()
        }

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            viewModel.getRepresentatives()
        }

        viewModel.representatives.observe(viewLifecycleOwner, Observer { representatives ->
            representativeAdapter.submitList(representatives)
        })

        return binding.root

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkLocationPermissions()
            } else {
                // In case they previously chose "Deny & don't ask again",
                // Reference https://github.com/google-developer-training/advanced-android-kotlin-geo-fences/blob/master/app/src/main/java/com/example/android/treasureHunt/HuntMainActivity.kt
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.location_permission_explain,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        requestPermissions(
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_PERMISSION
                        )
                    }.show()

            }
        }
    }

    private fun checkLocationPermissions() {
        when {
            isPermissionGranted() -> {
                getLocation()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.location_permission_explain,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        requestPermissions(
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_PERMISSION
                        )
                    }.show()
            }

            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.location_permission_explain, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    getLocation()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                val locationClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())
                locationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val address = geoCodeLocation(location)
                            viewModel.address.value = address
                            val states = resources.getStringArray(R.array.states)
                            val selectedStateIndex = states.indexOfFirst { it == address.state }
                            binding.state.setSelection(selectedStateIndex)
                            viewModel.getRepresentatives()
                        }
                    }
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context!!, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
            .map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}

private const val TAG = "RepresentativeFragment"