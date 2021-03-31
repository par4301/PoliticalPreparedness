package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import java.util.*

class DetailFragment : Fragment() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val LOCATION_PERMISSION_INDEX = 0
    }

    private lateinit var representativeViewModel: RepresentativeViewModel
    private lateinit var fragmentRepresentativeBinding: FragmentRepresentativeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        representativeViewModel = ViewModelProvider(this).get(RepresentativeViewModel::class.java)

        fragmentRepresentativeBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_representative, container, false
        )
        fragmentRepresentativeBinding.lifecycleOwner = this
        fragmentRepresentativeBinding.viewModel = representativeViewModel

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val representativeAdapter = RepresentativeListAdapter()
        fragmentRepresentativeBinding.foundRepresentativesList.adapter = representativeAdapter
        fragmentRepresentativeBinding.foundRepresentativesList.layoutManager = LinearLayoutManager(requireContext())

        representativeViewModel.representatives.observe(viewLifecycleOwner, { reps ->
            reps.let {
                representativeAdapter.submitList(it)
            }
        })

        representativeViewModel.isSnackShouldBeShown.observe(viewLifecycleOwner, { isShown ->
            if (isShown) {
                Snackbar.make(this.requireView(),R.string.no_address_found,Snackbar.LENGTH_LONG)
                        .setAction("YES") {
                            run {
                                fragmentRepresentativeBinding.addressLine1.text.clear()
                                fragmentRepresentativeBinding.addressLine2.text.clear()
                                fragmentRepresentativeBinding.city.text.clear()
                                fragmentRepresentativeBinding.zip.text.clear()
                            }
                        }.show()
                representativeViewModel.onSnackBarShowed()
            }
        })

        ArrayAdapter.createFromResource(
                requireContext(),
                R.array.states,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            fragmentRepresentativeBinding.state.adapter = adapter
        }

        fragmentRepresentativeBinding.btnLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                getLocation()
            }
        }

        fragmentRepresentativeBinding.btnFindRepresentatives.setOnClickListener {
            hideKeyboard()
            val address = Address(
                fragmentRepresentativeBinding.addressLine1.text.toString(),
                fragmentRepresentativeBinding.addressLine2.text.toString(),
                fragmentRepresentativeBinding.city.text.toString(),
                fragmentRepresentativeBinding.state.selectedItem.toString(),
                fragmentRepresentativeBinding.zip.text.toString()
            )
            representativeViewModel.onSearchRepresentativesByAddress(address)
        }
        return fragmentRepresentativeBinding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
        ) {
            Snackbar.make(
                    requireView(),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
            )
                    .setAction(R.string.settings_title) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
        } else {
            getLocation()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestForegroundPermission()
            false
        }
    }

    private fun isPermissionGranted() : Boolean {
        return (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    private fun requestForegroundPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        val location: Task<Location> = fusedLocationProviderClient.lastLocation
        location.addOnCompleteListener {
            if (it.isSuccessful) {
                val lastKnownLocation = it.result
                if (lastKnownLocation != null) {
                    val address = geoCodeLocation(lastKnownLocation)
                    representativeViewModel.onSearchRepresentativesByAddress(address)
                }
            } else {
                Snackbar.make(requireView(), R.string.error_while_getting_location, Snackbar.LENGTH_LONG)
            }
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}