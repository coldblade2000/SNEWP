package com.SNEWP.mainschedule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_zona_list_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), ScheduleFragment.OnFragmentInteractionListener , ZonasAdapter.zonasAdapInterface{
    private var selectedCount = 0
    override fun onZoneSelection(position: Int, boolean: Boolean) {
        if(boolean){
            selectedCount++
        }else{
            selectedCount--
        }
        updateCounter()
    }
    private fun updateCounter(){
        if(selectedCount!= 1){
            tvZonasCount.text = String.format("%s zonas seleccionadas", selectedCount)
        }else{
            tvZonasCount.text = String.format("%s zona seleccionada", selectedCount)

        }
    }

    lateinit var drawer : Drawer
    lateinit var scheduleFragment: ScheduleFragment
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var zonasAdapter: ZonasAdapter
    private lateinit var zonas : ArrayList<Zona>


    override fun onCreate(savedInstanceState: Bundle?) {
        // https://github.com/mikepenz/MaterialDrawer

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        var profileName = "Guest"
        var identity = "0123@gmail.com"

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val db = FirebaseFirestore.getInstance()

        var zonasID = arrayListOf<String>()

        bZonaConfirm.visibility = View.GONE

        if (user != null) {
            // already signed in
            profileName = if(user.displayName.isNullOrEmpty()) "Cuenta Anonima" else user.displayName!!
            if(user.phoneNumber!=null)
                identity = if(user.phoneNumber.isNullOrEmpty()) "Anonimo" else user.phoneNumber!!
            if(user.isAnonymous){
                fabMain.isVisible = false
            }
            db.collection("usuarios").document(user.uid).get()
                    .addOnSuccessListener { result ->
                        if(result.get("zonasID")!=null)
                            zonasID = result.get("zonasID") as ArrayList<String>
                        val fragmentTransaction = supportFragmentManager.beginTransaction()
                        val fragment = ScheduleFragment.newInstance(zonasID)
                        fragmentTransaction.add(R.id.mainFragmentFrame, fragment)
                        fragmentTransaction.commit()
                    }
            zonas = arrayListOf<Zona>()
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
                            if(zonasID.contains(doc.id)) {
                                selectedCount++
                                zona.checked=true
                            }
                            zonas.add(zona)
                        }
                        tvZonasCount.text = (String.format("%s zonas seleccionadas", selectedCount))
                        zonasAdapter = ZonasAdapter(zonas, this, this)

                        rvZonasSelect.adapter = zonasAdapter
                        rvZonasSelect.setHasFixedSize(true)
                        rvZonasSelect.layoutManager = LinearLayoutManager(applicationContext)
                        zonasAdapter.notifyDataSetChanged()
                    }

            bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(bmsZonas)
            bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {}
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            fabMain.show()
                            fabMain.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
                            bZonaConfirm.visibility = View.GONE
                            bArrow.background = ContextCompat.getDrawable(applicationContext, R.drawable.ic_keyboard_arrow_up_black_24dp)
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            bottomSheetBehavior.state= BottomSheetBehavior.STATE_COLLAPSED
                            bArrow.background = ContextCompat.getDrawable(applicationContext, R.drawable.ic_keyboard_arrow_up_black_24dp)
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            bZonaConfirm.visibility = View.VISIBLE
                            fabMain.hide()
                            bArrow.background = ContextCompat.getDrawable(applicationContext, R.drawable.ic_keyboard_arrow_down_black_24dp)
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                            bZonaConfirm.visibility = View.GONE
                            fabMain.animate().scaleX(0f).scaleY(0f).setDuration(300).start();
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {

                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                        }
                    }            }
            })
            bArrow.setOnClickListener { v ->
                slideUpDownBottomSheet(v)
            }

            bZonaConfirm.setOnClickListener {
                Toast.makeText(this, "Actualizando zonas", Toast.LENGTH_SHORT).show()
                val checkedZonasIDs = arrayListOf<String>()
                val zonasTags = arrayListOf<String>()
                zonasAdapter.checkedZones.forEachIndexed{i,zone ->
                    checkedZonasIDs.add(zone.zonaID)
                    zonasTags.add(zone.nombresAsString)
                }
                zonas = arrayListOf<Zona>()

                var tempMap = mutableMapOf<String, Any>()

                db.collection("usuarios").document(user.uid).get()
                        .addOnSuccessListener { result ->
                            if(result.data!=null)
                                tempMap = result.data!!
                            if(result.get("zonasID")!=null)
                                zonasID = result.get("zonasID") as ArrayList<String>

                        }
                tempMap.set("zonasID", zonasID)
                db.collection("usuarios").document(user.uid).set(tempMap)
                        .addOnSuccessListener {  }

                db.collection("zonas")
                        .get()
                        .addOnSuccessListener { result ->
                            for (doc in result){
                                if (checkedZonasIDs.contains(doc.id)) {
                                    val map = doc.data
                                    val zona = Zona(
                                            map["nombres"] as ArrayList<String>,
                                            doc.id,
                                            arrayListOf(),
                                            map["puntos"] as ArrayList<GeoPoint>,
                                            Zona.getMatColor(applicationContext, "500", 68)
                                    )
                                    zonas.add(zona)
                                }
                            }
                            zonasAdapter.notifyDataSetChanged()
                            val fragment = ScheduleFragment.newInstance(checkedZonasIDs)
                            val fragmentTransaction = supportFragmentManager.beginTransaction()
                                    .replace(R.id.mainFragmentFrame, fragment)
                            fragmentTransaction.commit()
                        }


            }


            fabMain.setOnClickListener {
                startActivityForResult(Intent(this, NewApunteForm::class.java), RC_ADD_APUNTE)
            }

        } else {
            startActivityForResult(Intent(this, PathChoserActivity::class.java), RC_SPLASH)
        }

        val header = AccountHeaderBuilder()
                .withActivity(this)
                .withOnAccountHeaderListener { view: View, iProfile: IProfile<Any>, b: Boolean ->
                    true
                }
                .addProfiles(
                        ProfileDrawerItem().withName(profileName).withEmail(identity)

                ).build()

        drawer = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(header)
                .addDrawerItems(PrimaryDrawerItem().withIdentifier(1).withName("Horario"),
                        DividerDrawerItem())
                .withOnDrawerItemClickListener(object: Drawer.OnDrawerItemClickListener{
                    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*, *>?): Boolean {

                        return false
                    }

                })
                .build()


    }

    private fun slideUpDownBottomSheet(v: View) {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            v.background = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_down_black_24dp)
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            v.background = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_up_black_24dp)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        super.onOptionsItemSelected(item)

        if (id == R.id.action_settings) {
            return true
        } else if (id == R.id.signout){
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser?.uid!!).delete()
                    .addOnSuccessListener {  }

            AuthUI.getInstance().signOut(this).addOnSuccessListener { Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show() }
            finishAndRemoveTask()
            return true
        }
        return true

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_ADD_APUNTE && resultCode == APUNTE_SUCCESS){
            val timeFormat = SimpleDateFormat("K:mm a", Locale.US)
            val name = data?.getStringExtra("name")
            val time = timeFormat.parse(data?.getStringExtra("hora")!!)

            val user = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            val zonasID = data?.getStringArrayListExtra("zonasID")!!

            var apunteNew = Apunte(
                    name,
                    user?.uid,
                    data.getStringExtra("celular"),
                    data.getIntegerArrayListExtra("days"),
                    time.hours, time.minutes,
                    GeoPoint(data.getDoubleExtra("lat", 0.0),
                            data.getDoubleExtra("lng", 0.0)),
                    zonasID)
            apunteNew.route = data.getStringExtra("ruta")
            apunteNew.plates = data.getStringExtra("placa")

            for(zona in zonasID){
                db.collection("zonas").document(zona).collection("apuntes")
                        .add(apunteNew.map)
                        .addOnSuccessListener { doc ->
                            apunteNew.apunteID = doc.id
                            Toast.makeText(this, "Apunte añadido", Toast.LENGTH_SHORT).show()
                        }
            }

            val checkedZonasIDs = arrayListOf<String>()
            val zonasTags = arrayListOf<String>()
            zonasAdapter.checkedZones.forEachIndexed{i,zone ->
                checkedZonasIDs.add(zone.zonaID)
                zonasTags.add(zone.nombresAsString)
            }
            zonas = arrayListOf<Zona>()
            db.collection("zonas")
                    .get()
                    .addOnSuccessListener { result ->
                        for (doc in result){
                            if (checkedZonasIDs.contains(doc.id)) {
                                val map = doc.data
                                val zona = Zona(
                                        map["nombres"] as ArrayList<String>,
                                        doc.id,
                                        arrayListOf(),
                                        map["puntos"] as ArrayList<GeoPoint>,
                                        Zona.getMatColor(applicationContext, "500", 68)
                                )
                                zonas.add(zona)
                            }
                        }
                        zonasAdapter.notifyDataSetChanged()
                        val fragment = ScheduleFragment.newInstance(checkedZonasIDs)
                        val fragmentTransaction = supportFragmentManager.beginTransaction()
                                .replace(R.id.mainFragmentFrame, fragment)
                        fragmentTransaction.commit()
                    }
            //TODO update schedule when a new Apunte is created
//            scheduleFragment.addSchedule(ScheduleFragment.getScheduleFromApunte(apunteNew))



        }else if(requestCode == RC_SPLASH && resultCode == PathChoserActivity.NEW_PROFILE_SUCCESS){
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val db = FirebaseFirestore.getInstance()
            val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(data?.getStringExtra("name"))
                    .build()

            user?.updateProfile(profileUpdates)
            if(data!=null && user!=null){
                assert(user.phoneNumber == data.getStringExtra("celular"))
                val map = mapOf<String, Any>(
                        "nombre" to data.getStringExtra("name") as String,
                        "celular" to user.phoneNumber!!,
                        "partida" to GeoPoint(data.getDoubleExtra("lat", 0.0), data.getDoubleExtra("lng", 0.0)),
                        "placa" to data.getStringExtra("placa"),
                        "ruta" to data.getStringExtra("ruta"),
                        "zonasID" to data.getStringArrayListExtra("zonasID")
                )
                db.collection("usuarios").document(user.uid).set(map)
                        .addOnSuccessListener {
                            fabMain.isVisible = true
                        }
                        .addOnFailureListener { e ->
                            Log.w("PEA_CREATE", "Error writing document", e)
                            Toast.makeText(this, "Se produjo un" +
                                " error al sincronizar el perfil. Vuelve a intentar", Toast.LENGTH_LONG).show() }
            }

        }
    }
    override fun onFragmentInteraction(apunte: Apunte) {
        val ft = supportFragmentManager.beginTransaction()
        val newFragment = ApunteDialogFragment.newInstance(apunte)
        newFragment.show(ft, "dialog")
    }


    companion object {
        const val RC_ADD_APUNTE = 5
        const val RC_SPLASH = 42
        const val RC_SIGN_IN = 123
        const val APUNTE_SUCCESS = 1
        const val APUNTE_FAILURE = -1
    }
}
