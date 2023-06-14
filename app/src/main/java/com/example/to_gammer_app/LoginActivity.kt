package com.example.to_gammer_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.to_gammer_app.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Show Splash Screen
        installSplashScreen()

        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        //Sign in Button : sign in and start MainActivity
        loginBinding.login.setOnClickListener {
            signIn(loginBinding.username.text.toString(), loginBinding.password.text.toString())
        }

        //Sign up Button : start Sign Up Activity
        loginBinding.buttonSignUp.setOnClickListener {
            val mainIntent = Intent(this, SignUpActivity::class.java)
            startActivity(mainIntent)
            if (!isFinishing) finish()
        }

        auth = Firebase.auth

        //Social Login
        //Google
        //Login instance build / request with Token / build
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("81231536716-c0hog5p7g3tbcna5bo66jjmi2nm5vstc.apps.googleusercontent.com")
            .requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //resultLauncher 초기화
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            }
        }

        //Google Login Button Listener
        loginBinding.buttonGoogle.setOnClickListener {
            signInGoogle()
        }
    }

    //Request to client, play Main
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                }
                else
                    updateUI(null)
            }
    }

    //update UI
    private fun updateUI(user: FirebaseUser?) {
        if(user != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(EXTRA_NAME, user.displayName)
            startActivity(intent)
        }
    }

    //사용자 이름 키 값 상수화
    companion object{
        const val EXTRA_NAME = "EXTRA_NAME"
    }

    //Login Method
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                //log in success
                sucessLogIn(it.result?.user!!)
            } else {
                //fail to log in
                Toast.makeText(this, "이메일, 비밀번호를 다시 한번 확인해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Start MainActivity
    private fun sucessLogIn(user: FirebaseUser) {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        if(!isFinishing) finish()
    }
}