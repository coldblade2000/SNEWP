package com.SNEWP.mainschedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import android.util.Log
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_maps.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    lateinit var db :FirebaseFirestore

    private var mLocationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 4
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap

    private var lat : Double = 0.0
    private var lng : Double = 0.0

    private lateinit var marker : Marker

    private val cedritos170 = arrayOf(LatLng(4.748306, -74.023144), LatLng(4.711548, -74.029041),LatLng(4.718317, -74.054795), LatLng(4.751400, -74.046395))
    private val sanjosedebavaria = arrayOf(  LatLng(4.770971, -74.082612), LatLng(4.752712, -74.052672), LatLng(4.765274, -74.053387), LatLng(4.772080, -74.062873))
    private val colinamazuren = arrayOf(LatLng(4.751158, -74.045376),LatLng(4.719025, -74.052082),LatLng(4.727821, -74.074787),LatLng(4.758871, -74.073957))
//    private val polys = arrayOf(cedritos170, sanjosedebavaria, colinamazuren)
    private val polysFake = arrayOf(cedritos170, sanjosedebavaria, colinamazuren)
//    private var polys : ArrayList<Zona> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        tbMaps.title = "Escoge punto de partida"
        setSupportActionBar(tbMaps)

        db = FirebaseFirestore.getInstance()
        db.collection("zonas")
                .get()
                .addOnSuccessListener { result ->
                    for (doc in result){
                        val map = doc.data
                        val zona = Zona(
                                map["nombres"] as ArrayList<String>,
                                doc.id,
                                arrayListOf(),
                                map["puntos"] as ArrayList<GeoPoint>,
                                Zona.getMatColor(applicationContext, "500", 68)
                        )

                        mMap.addPolygon(with(PolygonOptions()){
                            fillColor(zona.color)
                            strokeWidth(2F)
                            clickable(true)
                            addAll(zona.latLngPoints)
                        }).tag=zona.nombresAsString
                    }
                }

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
        getDeviceLocation()
        //TODO fix crash
        val bitmapDraw = BitmapFactory.decodeResource(resources, R.drawable.uniandes)
        mMap.addMarker(MarkerOptions().position(Companion.uniandes).title("U. de Los Andes")
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmapDraw, 75, 75, false))))
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        marker = mMap.addMarker(MarkerOptions().position(mMap.cameraPosition.target).draggable(false))


        mMap.setOnPolygonClickListener{polygon: Polygon? ->
            Toast.makeText(this, ""+polygon?.tag, Toast.LENGTH_SHORT).show()
        }
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
