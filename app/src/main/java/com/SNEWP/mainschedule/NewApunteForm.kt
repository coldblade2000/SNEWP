package com.SNEWP.mainschedule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
            if()//TODO finish entry send
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
