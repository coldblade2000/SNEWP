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
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_zona_list_dialog.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ZonasAdapter.zonasAdapInterface{

    override fun onZoneSelection(position: Int, boolean: Boolean) {
        if(boolean){
            selectedCount++
        }else{
            selectedCount--
        }
        updateCounter()
    }


    lateinit var db :FirebaseFirestore

    var requestCode = 0

    private var mLocationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 4
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap

    private var lat : Double = 0.0
    private var lng : Double = 0.0
    private var selectedCount : Int = 0

    private lateinit var marker : Marker

    private val cedritos170 = arrayOf(LatLng(4.748306, -74.023144), LatLng(4.711548, -74.029041),LatLng(4.718317, -74.054795), LatLng(4.751400, -74.046395))
    private val sanjosedebavaria = arrayOf(  LatLng(4.770971, -74.082612), LatLng(4.752712, -74.052672), LatLng(4.765274, -74.053387), LatLng(4.772080, -74.062873))
    private val colinamazuren = arrayOf(LatLng(4.751158, -74.045376),LatLng(4.719025, -74.052082),LatLng(4.727821, -74.074787),LatLng(4.758871, -74.073957))
//    private val polys = arrayOf(cedritos170, sanjosedebavaria, colinamazuren)
//    private val polysFake = arrayOf(cedritos170, sanjosedebavaria, colinamazuren)
    private var polys : ArrayList<Zona> = arrayListOf()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var zonasAdapter: ZonasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        requestCode = intent.getIntExtra("requestcode", ProfileEditActivity.PEA_REQUEST_MAP)
        tbMaps.title = "Escoge punto de partida"
        setSupportActionBar(tbMaps)

        tvZonasCount.text = (String.format("%s zonas seleccionadas", selectedCount))

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

                        val poly = mMap.addPolygon(with(PolygonOptions()){
                            fillColor(zona.color)
                            strokeWidth(0.5f)
                            clickable(true)
                            addAll(zona.latLngPoints)
                        })
                        poly.tag=zona.nombresAsString
                        zona.polygon = poly

                        polys.add(zona)
                    }
                    zonasAdapter.notifyDataSetChanged()
                }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // ==================

        bArrow.setOnClickListener { v ->
            slideUpDownBottomSheet(v)
        }

        bottomSheetBehavior = from<ConstraintLayout>(bmsZonas)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    STATE_COLLAPSED -> {
                        bArrow.background = ContextCompat.getDrawable(applicationContext, R.drawable.ic_keyboard_arrow_up_black_24dp)
                    }
                    STATE_HIDDEN -> {
                        bArrow.background = ContextCompat.getDrawable(applicationContext, R.drawable.ic_keyboard_arrow_up_black_24dp)
                    }
                    STATE_EXPANDED -> {
                        bArrow.background = ContextCompat.getDrawable(applicationContext, R.drawable.ic_keyboard_arrow_down_black_24dp)
                    }
                    STATE_DRAGGING -> {

                    }
                    STATE_SETTLING -> {

                    }
                    STATE_HALF_EXPANDED -> {

                    }
                }            }

        })
        zonasAdapter = ZonasAdapter(polys, this, this)

        rvZonasSelect.adapter = zonasAdapter

        rvZonasSelect.setHasFixedSize(true)

        rvZonasSelect.layoutManager = LinearLayoutManager(applicationContext)

        bZonaConfirm.setOnClickListener {
            val zonasIDs = arrayListOf<String>()
            val zonasTags = arrayListOf<String>()
            zonasAdapter.checkedZones.forEachIndexed{i,zone ->
                zonasIDs.add(zone.zonaID)
                zonasTags.add(zone.nombresAsString)
            }
            val newIntent = Intent()
            newIntent.putExtra("lat", lat)
            newIntent.putExtra("lng", lng)
            newIntent.putExtra("zonasID",zonasIDs)
            newIntent.putExtra("zonasTags", zonasTags)
            setResult(LOC_SUCCESS, newIntent)
            finish()


        }

    }

    private fun slideUpDownBottomSheet(v: View) {
        if (bottomSheetBehavior.state != STATE_EXPANDED) {
            bottomSheetBehavior.state = STATE_EXPANDED
            v.background = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_down_black_24dp)
        } else {
            bottomSheetBehavior.state = STATE_COLLAPSED
            v.background = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_up_black_24dp)

        }
    }

    private fun updateCounter(){
        if(selectedCount!= 1){
            tvZonasCount.text = String.format("%s zonas seleccionadas", selectedCount)
        }else{
            tvZonasCount.text = String.format("%s zona seleccionada", selectedCount)

        }
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
        val bitmapDraw = BitmapFactory.decodeResource(resources, R.drawable.uniandes)
        mMap.addMarker(MarkerOptions().position(Companion.uniandes).title("U. de Los Andes")
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmapDraw, 75, 75, false))))
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true


        mMap.setOnPolygonClickListener{polygon: Polygon? ->
            polys.forEachIndexed{i, zone ->
                if(zone.nombresAsString == polygon?.tag){
                    zonasAdapter.notifyItemChanged(i, listOf(true))
                    Toast.makeText(this, ""+polygon?.tag, Toast.LENGTH_SHORT).show()

                }
            }

        }
        if (requestCode == NewApunteForm.APUNTE_REQUEST_MAP) {
            marker = mMap.addMarker(MarkerOptions().position(mMap.cameraPosition.target).draggable(false))
            mMap.setOnCameraMoveListener {
                marker.position = mMap.cameraPosition.target
                lat = marker.position.latitude
                lng = marker.position.longitude
            }
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
                            if(requestCode == NewApunteForm.APUNTE_REQUEST_MAP)
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

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
    }*/

    companion object {
        val LOC_SUCCESS: Int = 5
        val uniandes = LatLng(4.602303, -74.064941)
        private val DEFAULTZOOM: Float = 12F
    }
}
