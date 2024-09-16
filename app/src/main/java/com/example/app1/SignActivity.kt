package com.example.app1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.app1.databinding.ActivitySignBinding
import com.example.app1.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database


class SignActivity : AppCompatActivity() {
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var username:String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClint: GoogleSignInClient


    private val binding : ActivitySignBinding by lazy {
        ActivitySignBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // initialize Firebase Auth
        auth = Firebase.auth
        // initialize Firebase database
        database = Firebase.database.reference

        // initialize google sign in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClint = GoogleSignIn.getClient(this, googleSignInOptions)
        binding.creatAccountButton.setOnClickListener{
            username = binding.userName.text.toString()
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()
            if (email.isEmpty()||password.isBlank()||username.isBlank()){
                Toast.makeText(this,"Vui lòng điền đầy đủ thông tin",Toast.LENGTH_SHORT).show()
            }else{
                createAccount(email,password)
            }
        }
        binding.alreadyhavebutton.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.googleButton.setOnClickListener{
            val signIntent = googleSignInClint.signInIntent
            launcher.launch(signIntent)
        }
    }
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
    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            task->
            if (task.isSuccessful){
                Toast.makeText(this,"Tạo tài khoản thành công",Toast.LENGTH_SHORT).show()
                saveUserData()
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }else{
                Toast.makeText(this,"Tạo tài khoản thất bại",Toast.LENGTH_SHORT).show()
                Log.d("Account", "Tạo tài khoản : that bai", task.exception)

            }
        }
    }

    private fun saveUserData() {
        username = binding.userName.text.toString()
        email = binding.emailAddress.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(username, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        //save data to firebase
        database.child("user").child(userId).setValue(user)

    }
}