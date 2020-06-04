package com.demo.ktfb

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001


    private lateinit var callbackManager: CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        sign_in_button.setOnClickListener(this)
        facebook_login_button.setOnClickListener(this)
        this.googleSignSetting()

    }



    /*
    *  login with exist account
    * */

    fun createUser(view:View){
        auth.createUserWithEmailAndPassword("abcd@gmail.com", "123321")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    getUserInfo()
                    val user = auth.currentUser

                    val i = Intent(this, LoginSuccessActivity::class.java).apply {
                        putExtra("userName", user?.displayName?:user?.email)
                    }
                    startActivity(i)
                } else {
                   println("error")
                }

                // ...
            }
    }
    fun loginWithAccount(view:View) {
        auth.signInWithEmailAndPassword("abc@gmail.com", "123456")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    getUserInfo()
                    val user = auth.currentUser
                    val i = Intent(this, LoginSuccessActivity::class.java).apply {
                        putExtra("userName", user?.displayName?:user?.email)
                    }
                    startActivity(i)

                } else {
                    // If sign in fails, display a message to the user.
                    println("error")

                    // ...
                }
            }
    }


    /*facebook
    *
    * */

    fun facebookLogin() {
        // Initialize Facebook Login button

        callbackManager = CallbackManager.Factory.create()

        facebook_login_button.setReadPermissions("email");
        facebook_login_button.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
//                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
//                Log.d(TAG, "facebook:onCancel")
                println("facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
//                Log.d(TAG, "facebook:onError", error)
                println("facebook:onCancel")
                // ...
            }
        })// ...
    }

    private fun handleFacebookAccessToken(token: AccessToken) {


        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    getUserInfo()
                    println(user)
                    val i = Intent(this, LoginSuccessActivity::class.java).apply {
                        putExtra("userName", user?.displayName)
                    }
                    startActivity(i)
//                    updateUI(user)
                } else {
                    println("exception")

                }

                // ...
            }
    }

    /*
    * google
    *
    * */

    fun googleSignSetting() {


        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        try {
            googleSignInClient.signOut()

        } catch (e: Exception) {
            println(e)
        }

    }

    fun googleSignIn() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } catch (e: Exception) {
            println(e)
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                println(e)
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    fun getUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {


        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    val user = auth.currentUser
                    getUserInfo()
                    println(user)
                    val i = Intent(this, LoginSuccessActivity::class.java).apply {
                        putExtra("userName", user?.displayName)
                    }
                    startActivity(i)
                } else {
                    println("exception")
                }

                // ...
            }
    }

    public override fun onStart() {
        super.onStart()
        try {
            val currentUser = auth.currentUser
            if (currentUser?.displayName != null) {

                val i = Intent(this, LoginSuccessActivity::class.java).apply {
                    putExtra("userName", currentUser?.displayName)
                }
                startActivity(i)
            }
            println(currentUser?.email)

        } catch (e: Exception) {
            println(e)
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
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in_button -> googleSignIn()
            R.id.facebook_login_button -> facebookLogin()
        }
    }
}