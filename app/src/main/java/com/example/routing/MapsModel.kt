package com.example.routing

import android.location.Location
import androidx.databinding.ObservableField
import com.google.android.gms.maps.model.LatLng

data class MapsModel(
    val listLocation: ObservableField<ArrayList<LatLng>>,
    val currentlocation: ObservableField<Location>
) {
}