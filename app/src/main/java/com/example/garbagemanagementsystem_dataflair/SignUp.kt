package com.example.garbagemanagementsystem_dataflair

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase


class SignUp : AppCompatActivity() {
    //firebase authentication
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        //initialize firebase auth instance
        auth = FirebaseAuth.getInstance()
        //initialize signup button,email field, password field and the text that takes us to login page
        val mGoToLoginPage:TextView=findViewById(R.id.goToLoginPage)
        val mSignupEmailField:EditText=findViewById(R.id.signupEmailField)
        val mSignupPasswordField:EditText=findViewById(R.id.signupPasswordField)
        val msignupButton:Button=findViewById(R.id.signupButton)
        //set onclick Listener for signupButton
        msignupButton.setOnClickListener {
            if(mSignupEmailField.text.toString().isEmpty()){
                mSignupEmailField.error="please enter email"
                mSignupEmailField.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(mSignupEmailField.text.toString()).matches()){
                mSignupEmailField.error="enter valid email address"
                mSignupEmailField.requestFocus()
                return@setOnClickListener
            }
            if(mSignupPasswordField.text.toString().isEmpty()){
                mSignupPasswordField.error="please enter password"
                mSignupPasswordField.requestFocus()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(mSignupEmailField.text.toString(),
                                                mSignupPasswordField.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //if signup successfull go to login page
                        startActivity(Intent(this,Login::class.java))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Error occured",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
        //set on click listener to text that takes to loginpage
        mGoToLoginPage.setOnClickListener {
            startActivity(Intent(this,Login::class.java))
            finish()
        }

    }



}