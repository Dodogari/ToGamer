package com.example.to_gammer_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.to_gammer_app.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpBinding.root)

        auth = Firebase.auth

        //Log in Button : Back to Log in Activity
        signUpBinding.buttonLogIn.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            if (!isFinishing) finish()
        }

        signUpBinding.buttonSignUp.setOnClickListener {
            //check writing
            //email
            if(!signUpBinding.username.text.contains("@"))
                signUpBinding.errorEmail.visibility = View.VISIBLE
            //password
            else if(signUpBinding.password.text.toString() != signUpBinding.passwordCheck.text.toString())
                signUpBinding.errorPassword.visibility = View.VISIBLE
            //sign up
            else {
                signUpBinding.errorEmail.visibility = View.GONE
                signUpBinding.errorPassword.visibility = View.GONE

                signUp(signUpBinding.username.text.toString(), signUpBinding.password.text.toString())
            }
        }

    }

    //Sign Up Method
    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                //if the account don't save in Firebase DB : enroll this
                successLogIn(it.result?.user!!)
            }
            else if (it.exception?.message.isNullOrEmpty()) {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
            }
            else {
                //the account is already exist in Firebase DB : Sign up
                signIn(email, password)
            }
        }
    }

    //go to profileIntent
    private fun successLogIn(user: FirebaseUser) {
        val profileIntent = Intent(this, SettingProfileActivity::class.java)
        startActivity(profileIntent)
        if(!isFinishing) finish()
    }

    //Login Method
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                //log in success
                successLogIn(it.result?.user!!)
            } else {
                //fail to log in
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}