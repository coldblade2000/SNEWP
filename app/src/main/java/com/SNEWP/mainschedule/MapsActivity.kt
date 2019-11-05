package com.SNEWP.mainschedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import android.util.Log
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mLocationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 4
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap

    private var lat : Double = 0.0
    private var lng : Double = 0.0

    private lateinit var marker : Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        tbMaps.title = "Escoge punto de partida"
        setSupportActionBar(tbMaps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

        val bitmapDraw = BitmapFactory.decodeResource(resources, R.drawable.uniandes)
        mMap.addMarker(MarkerOptions().position(Companion.uniandes).title("U. de Los Andes")
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmapDraw, 75, 75, false))))
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        getDeviceLocation()
        marker = mMap.addMarker(MarkerOptions().position(mMap.cameraPosition.target).draggable(false))
        mMap.setOnCameraMoveListener {
            marker.position = mMap.cameraPosition.target
            lat = marker.position.latitude
            lng = marker.position.longitude
        }

    }


    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted || getLocationPermission()) {
                fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
                    if(task.isSuccessful){
                        if (task.result != null) {
                            val mLastKnownLocation = task.result
                            lat = mLastKnownLocation!!.latitude
                            lng = mLastKnownLocation.longitude
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lat, lng), DEFAULTZOOM))
                            marker.position = mMap.cameraPosition.target
                            // Set the map's camera position to the current location of the device.

                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(uniandes, DEFAULTZOOM))
                            lat = uniandes.latitude
                            lng = uniandes.longitude
                        }
                    }
                }


            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    private fun getLocationPermission() : Boolean {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        mLocationPermissionGranted = false
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
        return mLocationPermissionGranted
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            mLocationPermissionGranted = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.Mconfirmar -> {
                val newIntent = Intent()
                newIntent.putExtra("lat", lat)
                newIntent.putExtra("lng", lng)
                setResult(LOC_SUCCESS, newIntent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        val LOC_SUCCESS: Int = 5
        val uniandes = LatLng(4.602303, -74.064941)
        private val DEFAULTZOOM: Float = 12F
    }
}
