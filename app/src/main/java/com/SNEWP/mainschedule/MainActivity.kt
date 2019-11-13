package com.SNEWP.mainschedule

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), ScheduleFragment.OnFragmentInteractionListener {

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


        if (user != null) {
            // already signed in
            profileName = if(user.displayName.isNullOrEmpty()) "Cuenta Anonima" else user.displayName!!
            if(user.phoneNumber!=null)
                identity = if(user.phoneNumber.isNullOrEmpty()) "Anonimo" else user.phoneNumber!!


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

        val drawer = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(header)
                .addDrawerItems(PrimaryDrawerItem().withIdentifier(1).withName("Horario"),
                        DividerDrawerItem(),
                        PrimaryDrawerItem().withIdentifier(2).withName("Zonas Escogidas"))
                .withOnDrawerItemClickListener(object: Drawer.OnDrawerItemClickListener{
                    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*, *>?): Boolean {

                        return false
                    }

                })
                .build()


        val fab = findViewById<FloatingActionButton>(R.id.fabEdit)
        fab.setOnClickListener { view ->
            startActivityForResult(Intent(this, NewApunteForm::class.java), Companion.RC_ADD_APUNTE)
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
            AuthUI.getInstance().signOut(this).addOnSuccessListener { Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show() }
            finishAndRemoveTask()
            return true
        }
        return true

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in


            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                // Sign in failed

                /*if (response == null) {
                    // User pressed back button

                    showSnackbar(R.string.sign_in_cancelled)
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection)
                    return
                }
                showSnackbar(R.string.unknown_error)
                Log.e(FragmentActivity.TAG, "Sign-in error: ", response.error)*/
            }
        }else if (requestCode == RC_ADD_APUNTE && resultCode == APUNTE_SUCCESS){
            val extras = data!!.extras!!
            val timeFormat = SimpleDateFormat("K:mm a", Locale.US)
            val time = timeFormat.parse(extras.getString("hora")!!)

            //TODO Finish location implementation
            //https://developers.google.com/places/android-sdk/client-migration
            //https://codelabs.developers.google.com/codelabs/location-places-android/index.html?index=..%2F..index#0
            //https://guides.codepath.com/android/google-maps-fragment-guide
            var apunteNew = Apunte(
                    extras.getString("name"),
                    FirebaseAuth.getInstance().currentUser?.uid,
                    extras.getString("celular"),
                    time.hours, time.minutes,
                    extras.getDouble("lat"),
                    extras.getDouble("lng")
                    )


        }
    }
    override fun onFragmentInteraction(uri: Uri) {

    }


    companion object {
        const val RC_ADD_APUNTE = 5
        const val RC_SPLASH = 42
        const val RC_SIGN_IN = 123
        const val APUNTE_SUCCESS = 1
        const val APUNTE_FAILURE = -1
    }
}
