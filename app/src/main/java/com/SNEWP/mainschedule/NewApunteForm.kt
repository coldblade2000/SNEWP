package com.SNEWP.mainschedule

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog

import kotlinx.android.synthetic.main.activity_new_apunte_form.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class NewApunteForm : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_apunte_form)

        setSupportActionBar(apunteToolbar)
        Glide.with(this)
                .load(R.mipmap.drivinghead)
                .into(ivApuntesHead)

        etAHora.setOnClickListener {
            TimePickerDialog.newInstance(this, 8, 0,  false)
                    .show(supportFragmentManager, "Timepickerdialog")

        }
        fab.setOnClickListener { view ->
            var exception = false
            if(etANombre.text == null){
                exception = true
                tiANombre.error = "Te falto poner un nombre"
            }
            if(etACelular.text == null){
                exception = true
                tiACelular.error = "Te falto poner un celular"
            }
            if(etANombre.text == null){
                exception = true
                tiAHora.error = "Te falto poner una hora valida"
            }
            if(etANombre.text == null){
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

                if(etAPlaca.text != null)
                    intentResult.putExtra("placa", etAPlaca.text)
                if(etARuta.text != null)
                    intentResult.putExtra("ruta", etARuta.text)
                setResult(MainActivity.APUNTE_SUCCESS, intent)
                finish()
            }


            setResult(MainActivity.APUNTE_SUCCESS)
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


}
