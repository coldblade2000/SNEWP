package com.SNEWP.mainschedule

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog

import kotlinx.android.synthetic.main.activity_new_apunte_form.*
import java.lang.Exception
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*


class NewApunteForm : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var lugarPartida: GeoPoint
    lateinit var zonasID : ArrayList<String>
    lateinit var zonasTags : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_apunte_form)
        apunteToolbar.setTitleTextColor(Color.WHITE)
        apunteToolbar.title = "Añadir apunte"
        setSupportActionBar(apunteToolbar)


        supportActionBar?.setHomeButtonEnabled(true);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").get()
                .addOnSuccessListener { result ->
                    for(doc in result){
                        if(doc.id == user?.uid){

                            etANombre.setText(""+ doc["nombre"])
                            etACelular.setText(user.phoneNumber)
                            etAPlaca.setText(""+doc["placa"])
                            etARuta.setText(""+doc["ruta"])
                            lugarPartida = doc["partida"] as GeoPoint
                            etAPartida.setText(String.format("%s, %s",
                                    lugarPartida.latitude.toString().substring(0,6),
                                    lugarPartida.longitude.toString().substring(0,7)))
                            break
                        }
                    }
                }

        etAHora.setOnClickListener {
            val picker = TimePickerDialog.newInstance(this, 8, 0,  false)
            picker.accentColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, theme)
            picker.show(supportFragmentManager, "Timepickerdialog")
        }
        clAPartida.setOnClickListener{
            val newIntent = Intent(this, MapsActivity::class.java)
            newIntent.putExtra("requestcode", APUNTE_REQUEST_MAP)
            startActivityForResult(newIntent, APUNTE_REQUEST_MAP)
        }

        fabApunte.setOnClickListener {
            addApunte()
        }
    }

    private fun addApunte(){
        var exception = false
        if(etANombre.text.isNullOrBlank()){
            exception = true
            tiANombre.error = "Te falto poner un nombre"
        }
        if(etACelular.text.isNullOrBlank()){
            exception = true
            tiACelular.error = "Te falto poner un celular"
        }
        if(etAHora.text.isNullOrBlank()){
            exception = true
            tiAHora.error = "Te falto poner una hora valida"
        }
        if(!this::lugarPartida.isInitialized){
            exception = true
            tiAPartida.error = "Te falto poner un punto de partida"
        }

        if(exception){
            Snackbar.make(clApunte, "Te falto completar los campos requeridos", Snackbar.LENGTH_SHORT)
        }else{
            val intentResult = Intent()
            intentResult.putExtra("name", etANombre.text)
            intentResult.putExtra("celular", etACelular.text)
            intentResult.putExtra("hora", etAHora.text)
            intentResult.putExtra("partida", etAPartida.text)
            intentResult.putExtra("lat", lugarPartida.latitude)
            intentResult.putExtra("lng", lugarPartida.longitude)
            intentResult.putStringArrayListExtra("zonasID", zonasID)

            if(etAPlaca.text != null)
                intentResult.putExtra("placa", etAPlaca.text)
            if(etARuta.text != null)
                intentResult.putExtra("ruta", etARuta.text)
            setResult(MainActivity.APUNTE_SUCCESS, intent)
            finish()
        }
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minuteOfDay: Int, second: Int) {
        var hour : String = hourOfDay.toString()
        var minute : String = minuteOfDay.toString()
        if(hour.length==1)
            hour = "0".plus(hour)
        if(minute.length==1)
            minute = "0".plus(minute)
        val time:String = String.format("%s:%s", hour, minute )
        try {
            val sdf = SimpleDateFormat("H:mm", Locale.US)
            val dateObj = sdf.parse(time)
            etAHora.setText(SimpleDateFormat("K:mm a", Locale.US).format(dateObj))

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.apunte_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mApunteAdd -> {
                addApunte()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            APUNTE_REQUEST_MAP -> {
                if(resultCode == MapsActivity.LOC_SUCCESS && data != null){
                    lugarPartida = GeoPoint(data.getDoubleExtra("lat", 0.0),
                    data.getDoubleExtra("lng", 0.0))

                    zonasID = data.getStringArrayListExtra("zonasID")!!
                    zonasTags = data.getStringArrayListExtra("zonasTags")!!

                    val strBuild = StringBuilder()

                    strBuild.append(String.format("%s, %s \n",
                            lugarPartida.latitude.toString().substring(0,6),
                            lugarPartida.longitude.toString().substring(0,7)))
                    for(tag in zonasTags){
                        strBuild.appendln(tag)
                    }
                    etAPartida.setText(strBuild.toString())
                }
            }
        }
    }
    companion object{
        const val APUNTE_REQUEST_MAP = 2
    }

}
