package com.SNEWP.mainschedule;

import android.content.Context
import android.content.Intent
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment;
import kotlinx.android.synthetic.main.fragment_dialog_apunte.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class ApunteDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_apunte, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tbZoneDiag.setOnMenuItemClickListener{item ->
            if (item.itemId == R.id.whatsapp){
                if(item!=null){
                    val icon = item.icon
                    val filter = PorterDuffColorFilter(ResourcesCompat.getColor(resources, R.color.colorPrimary, context?.theme), PorterDuff.Mode.SRC_IN)
                    icon.colorFilter = filter as ColorFilter?
                    item.icon=icon
                }

                var phoneNumber = arguments?.getString("celular")
                phoneNumber = phoneNumber?.toCharArray()
                        ?.filter { it.isLetterOrDigit() }
                        ?.joinToString(separator = "")
                val msg = "Hola! Tienes cupo para las XX:XX XX ?"
                val link = String.format("https://wa.me/%s/?text=%s", phoneNumber, encodeURIComponent(msg))
                Log.i("ApunteDiagFrag", link)
                openNewTabWindow(link, context!!)
            }
            true
        }

        etDNombre.setText(arguments?.getString("name"), TextView.BufferType.NORMAL)
        etDHora.setText(arguments?.getString("time"), TextView.BufferType.NORMAL)

        val ruta = arguments?.getString("ruta")
        etDRuta.setText(ruta ?: "---", TextView.BufferType.NORMAL)
        val placa = arguments?.getString("placa")
        etDPlaca.setText(placa ?: "---", TextView.BufferType.NORMAL)



        bPartidaDialog.setOnClickListener {
            val newIntent = Intent(context, MapApunteView::class.java)
            newIntent.putExtra("lat", arguments?.getDouble("lat")!!)
            newIntent.putExtra("lng", arguments?.getDouble("lng")!!)
            startActivity(newIntent)
        }
    }

    fun openNewTabWindow(urls: String, context: Context) {
        val uris = Uri.parse(urls)
        val intents = Intent(Intent.ACTION_VIEW, uris)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        context.startActivity(intents)
    }
    fun encodeURIComponent(s: String): String {
        var result: String

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replace("\\+", "%20")
                    .replace("\\%21", "!")
                    .replace("\\%27", "'")
                    .replace("\\%28", "(")
                    .replace("\\%29", ")")
                    .replace("\\%7E", "~")
        } catch (e: UnsupportedEncodingException) {
            result = s
        }

        return result
    }
    companion object {

        /**
         * Create a new instance of CustomDialogFragment, providing "num" as an
         * argument.
         */
        fun newInstance(apunte : Apunte): ApunteDialogFragment {
            val f = ApunteDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putString("name", apunte.name)
            args.putString("celular", apunte.phoneNumber)
            args.putDouble("lat", apunte.location.latitude)
            args.putDouble("lng", apunte.location.longitude)
            args.putString("time", apunte.time)
            if(!apunte.route.isNullOrEmpty())
                args.putString("ruta", apunte.route)
            if(!apunte.plates.isNullOrEmpty())
                args.putString("placa", apunte.plates)
            f.arguments = args
            return f
        }


    }


}
