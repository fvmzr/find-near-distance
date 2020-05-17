package com.example.routing.utils

import com.google.android.gms.maps.model.LatLng


data class MarkerPosition(var latLng : LatLng, var distance : Float = Float.MAX_VALUE)