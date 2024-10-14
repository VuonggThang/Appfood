package com.example.app1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.app1.databinding.ActivityLoginBinding
import com.example.app1.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class LoginActivity : AppCompatActivity() {
    private  var userName: String ?= null
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
        //auth = Firebase.auth
        auth = FirebaseAuth.getInstance()
        // initialize Firebase database
        //database = Firebase.database.reference
        database = FirebaseDatabase.getInstance().reference
        // initialize of google
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)

        //login with email and password
        binding.loginButton.setOnClickListener {
            // get data from text field
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()
            if(email.isBlank()||password.isBlank()){
                Toast.makeText(this,"Nhập thông tin chi tiết",Toast.LENGTH_SHORT).show()
            }else{
                loginUser(email, password)
                //createUser()
                //Toast.makeText(this,"Đăng nhập thành công",Toast.LENGTH_SHORT).show()
            }
        }
        binding.donthaveaccount.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }
        //google sign in
        binding.googleButton.setOnClickListener{
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)
        }
    }
    ///
    //launcher google signin
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account : GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken,null)
                    auth.signInWithCredential(credential).addOnCompleteListener{
                            task ->
                        if (task.isSuccessful){
                            //successfully sign in with google
//                            Toast.makeText(this,"Đăng nhập thành công với google",Toast.LENGTH_SHORT).show()
//                            updateUi(authTask.result?.user)
                            saveFcmTokenToFirebase()
                            startActivity(Intent(this,MainActivity::class.java))
                            finish()
                        }else{
                            Toast.makeText(this,"Đăng ký không thành công với google",Toast.LENGTH_SHORT).show()
                            Log.e("GoogleSignIn", "signInResult:failed code=" + task.exception)
                        }
                    }
                }
                else{
                    Toast.makeText(this,"Đăng ký không thành công với google",Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun saveFcmTokenToFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM Token", "Token: $token")

                // Lưu token vào Firebase Database dưới userId
                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                database.child("user").child(userId).child("fcmToken").setValue(token)
            } else {
                Log.e("FCM Token", "Failed to get FCM token", task.exception)
            }
        }
    }

//    private fun createUser() {
//        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task->
//            if(task.isSuccessful){
//                val user = auth.currentUser
//                saveFcmTokenToFirebase()
//                updateUi(user)
//            }else{
//                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
//                    if (task.isSuccessful){
//                        saveUserdata()
//                        val user = auth.currentUser
//                        saveFcmTokenToFirebase()
//                        updateUi(user)
//                    }else{
//                        Toast.makeText(this,"Đăng nhập thất bại",Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                saveFcmTokenToFirebase()
                updateUi(user)
            } else {
                Toast.makeText(this, "Tài khoản không tồn tại, vui lòng đăng ký", Toast.LENGTH_SHORT).show()
                // Chuyển đến màn hình đăng ký nếu không có tài khoản
                val intent = Intent(this, SignActivity::class.java)
                startActivity(intent)
            }
        }
    }
//    private fun saveUserdata() {
//        // get data from text field
//        email = binding.emailAddress.text.toString().trim()
//        password = binding.password.text.toString().trim()
//        val user = UserModel(userName,email,password)
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid
//        //save data
//        database.child("user").child(userId).setValue(user)
//    }
    override fun onStart(){
        super.onStart()
        val currentUser =auth.currentUser
        if(currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun updateUi(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}