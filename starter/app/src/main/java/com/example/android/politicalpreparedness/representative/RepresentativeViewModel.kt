package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _isSnackShouldBeShown = MutableLiveData<Boolean>()
    val isSnackShouldBeShown: LiveData<Boolean>
        get() = _isSnackShouldBeShown

    init {
        _isSnackShouldBeShown.value = false
    }

    fun onSearchRepresentativesByAddress(address: Address) {
        _isSnackShouldBeShown.value = false
        _address.value = address
        searchRepresentatives()
    }

    private fun searchRepresentatives() {
        if (_address.value != null) {
            viewModelScope.launch {
                try {
                    val addressString = _address.value!!.toFormattedString()
                    val representativeResponse =
                            CivicsApi.retrofitService.getListOfRepresentatives(addressString)
                    _representatives.value = representativeResponse.offices.flatMap { office ->
                        office.getRepresentatives(representativeResponse.officials)
                    }
                } catch (ex: Exception) {
                    _isSnackShouldBeShown.value = true
                    _representatives.value = ArrayList()

                }
            }
        } else {
            _representatives.value = ArrayList()
        }

    }

    fun onSnackBarShowed(){
        _isSnackShouldBeShown.value = false
    }

}
