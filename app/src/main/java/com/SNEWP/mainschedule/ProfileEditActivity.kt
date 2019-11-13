package com.SNEWP.mainschedule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_profile_edit.*

class ProfileEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").get()
                .addOnSuccessListener { result ->
                    for(doc in result){
                        if(doc.id == user?.uid){
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(doc["nombre"] as String)
                                    .build()

                            user.updateProfile(profileUpdates)
                            etENombre.setText(""+ doc["nombre"])
                            etECelular.setText(user.phoneNumber)
                            etECelular.isEnabled = false
                            etEPlaca.setText(""+doc["placa"])
                            etERuta.setText(""+doc["ruta"])
                            val geoPoint = doc["partida"] as GeoPoint
                            etEPartida.setText("" + String.format("%s, %s", geoPoint.latitude, geoPoint.longitude))
                            //TODO Finish ProfileEditActivity
                            //TODO Add location functionality
                            //TODO Add validation
                            //TODO Add updating firebase info
                            break
                        }
                    }
                }
    }
}
