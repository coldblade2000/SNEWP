package com.SNEWP.mainschedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_profile_edit.*
import java.lang.StringBuilder

class ProfileEditActivity : AppCompatActivity() {
    lateinit var lugarPartida : GeoPoint
    lateinit var zonasID : ArrayList<String>
    lateinit var zonasTags : ArrayList<String>
    var requestCode = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        setSupportActionBar(editToolbar)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser


        when(intent.getIntExtra("requestcode", -1)){
            PEA_EDIT -> {
                editToolbar.setTitle("Editar perfil")
                supportActionBar?.setHomeButtonEnabled(true)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                requestCode = PEA_EDIT
            }
            PEA_CREATE -> {
                editToolbar.setTitle("Crear perfil")
                requestCode = PEA_CREATE

            }
        }
        etECelular.setText(String.format("%s", user?.phoneNumber))
        fabEdit.setOnClickListener{ confirmEdit()}
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
                            lugarPartida = doc["partida"] as GeoPoint
                            etEPartida.setText(String.format("%s, %s",
                                    lugarPartida.latitude.toString().substring(0,6),
                                    lugarPartida.longitude.toString().substring(0,7)))
                            break
                        }
                    }
                }

        clEPartida.setOnClickListener{
            val newIntent = Intent(this, MapsActivity::class.java)
            newIntent.putExtra("requestcode", PEA_REQUEST_MAP)
            startActivityForResult(newIntent, PEA_REQUEST_MAP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PEA_REQUEST_MAP && resultCode == MapsActivity.LOC_SUCCESS){
            if(data != null){
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
                etEPartida.setText(strBuild.toString())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mEditConfirm -> confirmEdit()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun confirmEdit(){
        var exception = false
        if(etENombre.text.isNullOrBlank()){
            exception = true
            tiENombre.error = "Te falto poner un nombre"
        }
        if(etECelular.text.isNullOrBlank()){
            exception = true
            tiECelular.error = "Te falto poner un celular"
        }

        if(!this::lugarPartida.isInitialized){
            exception = true
            tiEPartida.error = "Te falto poner un punto de partida"
        }

        if(exception){
            Snackbar.make(clEdit, "Te falto completar los campos requeridos", Snackbar.LENGTH_SHORT)
        }else{
            val intentResult = Intent()
            val bundleResult = Bundle()

            bundleResult.putString("name", etENombre.text.toString())
            bundleResult.putString("celular", etECelular.text.toString())
            bundleResult.putDouble("lat", lugarPartida.latitude)
            bundleResult.putDouble("lng", lugarPartida.longitude)
            bundleResult.putStringArrayList("zonasID", zonasID)
            if(etEPlaca.text != null)
                bundleResult.putString("placa", etEPlaca.text.toString())
            if(etERuta.text != null)
                bundleResult.putString("ruta", etERuta.text.toString())
            intentResult.putExtras(bundleResult)
            var resultCode = -1
            if(requestCode == PEA_EDIT) {
                resultCode = PEA_EDIT_SUCCESS
            }else if(requestCode == PEA_CREATE){
                resultCode = PEA_CREATE_SUCCESS
            }
            setResult(resultCode, intentResult)
            finish()
        }
    }
    companion object{
        const val PEA_EDIT = 23
        const val PEA_EDIT_SUCCESS = 24
        const val PEA_CREATE = 34
        const val PEA_CREATE_SUCCESS = 35

        const val PEA_REQUEST_MAP = 65

    }
}
