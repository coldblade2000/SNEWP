package com.SNEWP.mainschedule

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.AccountHeader
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

        val auth = FirebaseAuth.getInstance()

        var profileName = "Guest"
        var identity = "0123@gmail.com"
        val user = FirebaseAuth.getInstance().currentUser


        if (user != null) {
            // already signed in
            profileName = user.displayName!!
            if(user.phoneNumber!=null)
                identity = user.phoneNumber!!
            if(user.email!=null)
                identity = user.email!!


        } else {
            // not signed in
            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(listOf(
                                    AuthUI.IdpConfig.GoogleBuilder().build(),
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    AuthUI.IdpConfig.PhoneBuilder().build()
                            ))
                            .build(), Companion.RC_SIGN_IN)
        }

        val header = AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(
                        ProfileDrawerItem().withName(profileName).withEmail(identity)
                )
                .withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
                    override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                        return false
                    }
                }).build()

        DrawerBuilder()
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

        val fab = findViewById<FloatingActionButton>(R.id.fab)
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


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in


            if (resultCode == Activity.RESULT_OK) {
                /*startActivity(SignedInActivity.createIntent(this, response))
                finish()*/
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
            var apunteNew = Apunte(extras.getString("name"),
                    accountid,
                    extras.getString("celular"),
                    time.hours, time.minutes
                    )
        }
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    companion object {
        const val RC_ADD_APUNTE = 5
        const val RC_SIGN_IN = 123
        const val APUNTE_SUCCESS = 1
        const val APUNTE_FAILURE = -1
    }
}
