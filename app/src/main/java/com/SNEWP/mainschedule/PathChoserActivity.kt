package com.SNEWP.mainschedule

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_path_choser.*

class PathChoserActivity : AppCompatActivity(), View.OnClickListener {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_choser)
        bConductor.setOnClickListener(this)
        bUsuario.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.bConductor -> {
                startActivityForResult(
                        // Get an instance of AuthUI based on the default app
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .enableAnonymousUsersAutoUpgrade()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(listOf(
                                        AuthUI.IdpConfig.PhoneBuilder()
                                                .setDefaultCountryIso("co")
                                                .build()
                                ))
                                .build(), MainActivity.RC_SIGN_IN)
            }
            R.id.bUsuario -> {
                val auth = FirebaseAuth.getInstance()
                Log.i("PathChoserActivity", "Gottem 1")
                auth.signInAnonymously()
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Log.i("PathChoserActivity", "Gottem 2")

                                // Sign in success, update UI with the signed-in user's information
                                val user = auth.currentUser
                                val intent = Intent()
                                intent.putExtra("user", user)
                                setResult(ANON_SUCCESS, intent)
                                finish()
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(baseContext, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                setResult(ANON_FAILURE)
                            }

                        }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {

                val newIntent = Intent(this, ProfileEditActivity::class.java)
                newIntent.putExtra("requestcode", ProfileEditActivity.PEA_CREATE)
                newIntent.putExtra("celular", response?.phoneNumber)


                startActivityForResult(newIntent, ProfileEditActivity.PEA_CREATE)
            } else {
                // Sign in failed

                if (response == null) {
                    // User pressed back button

                    Toast.makeText(this, "Cancelaste la sesion", Toast.LENGTH_SHORT).show()
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No hay conexion al internet", Toast.LENGTH_SHORT).show()
                    return
                }

                Log.e("PathChoserActivity", "Sign-in error: ", response.error)
                val intent = Intent()
                setResult(LOGGED_IN_FAILURE, intent)

            }
        }else if (requestCode == ProfileEditActivity.PEA_CREATE){
            if(resultCode == ProfileEditActivity.PEA_CREATE_SUCCESS && data != null){
                val intentResult = Intent()
                intentResult.putExtra("name", data.getStringExtra("name"))
                intentResult.putExtra("celular", data.getStringExtra("celular"))
                intentResult.putExtra("lat", data.getDoubleExtra("lat", -1.0))
                intentResult.putExtra("lng", data.getDoubleExtra("lng", -1.0))
                intentResult.putExtra("placa", data.getStringExtra("placa"))
                intentResult.putExtra("ruta", data.getStringExtra("ruta"))
                intentResult.putExtra("zonasID", data.getStringArrayListExtra("zonasID"))


                setResult(NEW_PROFILE_SUCCESS, intentResult)
                finish()
            }
        }
    }
    companion object{
        const val NEW_PROFILE_SUCCESS = 1
        const val LOGGED_IN_SUCCESS = 8
        const val LOGGED_IN_FAILURE = 9
        const val ANON_SUCCESS = 5
        const val ANON_FAILURE = 6
    }

}
