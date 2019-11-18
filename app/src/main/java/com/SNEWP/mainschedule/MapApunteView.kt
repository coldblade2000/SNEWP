package com.SNEWP.mainschedule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapApunteView : AppCompatActivity(), OnMapReadyCallback {

    private val DEFAULTZOOM: Float = 15F
    private lateinit var mMap: GoogleMap
    private lateinit var point:LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_apunte_view)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        point = LatLng(intent?.getDoubleExtra("lat", 0.0)!!, intent?.getDoubleExtra("lng", 0.0)!!)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a point in Sydney and move the camera
        mMap.addMarker(MarkerOptions().position(point).title("Lugar de Partida"))
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(point, DEFAULTZOOM))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
    }
}
