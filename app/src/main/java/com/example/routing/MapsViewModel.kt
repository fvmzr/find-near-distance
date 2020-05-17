package com.example.routing

import android.location.Location
import android.util.Log
import com.directions.route.*
import com.example.routing.utils.MarkerPosition
import com.example.routing.utils.SingleLiveEvent
import com.google.android.gms.maps.model.LatLng


class MapsViewModel {
    private val TAG = this::class.java.simpleName
    //    private var locationSelect = Location("Select")
//    private var locationCurrent = Location("current")
    lateinit var closestMarker: MarkerPosition
    var mapsModel: MapsModel? = null
    var shortestDistanceIndex = SingleLiveEvent<MarkerPosition>()


    fun getListLocation(listLocation: ArrayList<MarkerPosition>, currentLocation: Location) {
//        mapsModel = MapsModel(ObservableField(listLocation), ObservableField(currentLocation))

        closestMarker = listLocation.get(0)


        for (i in 0 until 5) {

            val distinationLocation = Location("")
            distinationLocation.latitude = listLocation.get(i).latLng.latitude
            distinationLocation.longitude = listLocation.get(i).latLng.longitude

            var distance = currentLocation.distanceTo(distinationLocation)
            if (distance < closestMarker.distance) {
                closestMarker.latLng = listLocation.get(i).latLng
                closestMarker.distance = distance
            }
        }

        shortestDistanceIndex.value = closestMarker
    }


    fun routng() {
        ////////this method only test routing/////////
        val start = LatLng(18.015365, -77.499382);
        val waypoint = LatLng(18.01455, -77.499333);
        val end = LatLng(18.012590, -77.500659);

        val routing = Routing.Builder()
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .key("AIzaSyALOjiFmMvPQ_GFjMlNb565W81Yun3c4WE")
            .withListener(
                object : RoutingListener {
                    override fun onRoutingCancelled() {
                        log("routng  onRoutingCancelled")
                    }

                    override fun onRoutingStart() {
                        log("routng  onRoutingStart")
                    }

                    override fun onRoutingFailure(p0: RouteException?) {
                        log("routng  onRoutingFailure" + p0?.message)
                    }

                    override fun onRoutingSuccess(p0: java.util.ArrayList<Route>?, p1: Int) {
                        log("routng  onRoutingSuccess")
                    }

                }
            )
            .waypoints(start, waypoint, end)
            .build();
        routing.execute();

    }

    private fun log(message: String) {
        Log.e(TAG, message)
    }

}
