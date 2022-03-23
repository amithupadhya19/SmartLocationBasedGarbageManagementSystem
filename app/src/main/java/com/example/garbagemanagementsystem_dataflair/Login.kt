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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth= FirebaseAuth.getInstance()
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            updateUI(currentUser);
        }
        //initialize login button,email field, password field and the text that takes us to signup page
        val mGoToSignUpPage: TextView =findViewById(R.id.goToSignUpPage)
        val mLoginEmailField: EditText =findViewById(R.id.loginEmailField)
        val mLoginPasswordField: EditText =findViewById(R.id.loginPasswordField)
        val mLoginButton: Button = findViewById(R.id.loginButton)

        //set onclick Listener for LOgin Button
        mLoginButton.setOnClickListener {
            if(mLoginEmailField.text.toString().isEmpty()){
                mLoginEmailField.error="please enter email"
                mLoginEmailField.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(mLoginEmailField.text.toString()).matches()){
                mLoginEmailField.error="enter valid email address"
                mLoginEmailField.requestFocus()
                return@setOnClickListener
            }
            if(mLoginPasswordField.text.toString().isEmpty()){
                mLoginPasswordField.error="please enter password"
                mLoginPasswordField.requestFocus()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(mLoginEmailField.text.toString(),
                                            mLoginPasswordField.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)

                        updateUI(null)
                    }
                }
        }
        //textview on click listener(go back to signup page)
        mGoToSignUpPage.setOnClickListener {
            val intent=Intent(this,SignUp::class.java)
            startActivity(intent)
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
            if (currentUser!=null){
                startActivity(Intent(this,MainActivity::class.java))
            }
            else{
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
    }
}