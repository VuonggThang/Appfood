package com.example.app1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.app1.databinding.ActivityLoginBinding
import com.example.app1.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import java.lang.ref.Reference

class LoginActivity : AppCompatActivity() {
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var auth:FirebaseAuth
    private lateinit var database:DatabaseReference
    private lateinit var googleSignInClient:GoogleSignInClient

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // initialize Firebase Auth
        auth = Firebase.auth
        // initialize Firebase database
        database = Firebase.database.reference
        // initialize of google
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)

        //login with email and password

        binding.loginButton.setOnClickListener {
            // get data from text field
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()
            if(email.isBlank()||password.isBlank()){
                Toast.makeText(this,"Nhap thong tin chi tiet",Toast.LENGTH_SHORT).show()
            }else{
                createUser()
                Toast.makeText(this,"Login Thang cong",Toast.LENGTH_SHORT).show()

            }
        }
        binding.donthaveaccount.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createUser() {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task->
            if(task.isSuccessful){
                val user = auth.currentUser
                updateUi(user)
            }else{
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        saveUserdata()
                        val user = auth.currentUser
                        updateUi(user)
                    }else{
                        Toast.makeText(this,"Login That bai",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveUserdata() {
        // get data from text field
        email = binding.emailAddress.text.toString().trim()
        password = binding.password.text.toString().trim()
        val user = UserModel(email,password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        //save data
        database.child("user").child(userId).setValue(user)
    }

    private fun updateUi(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}