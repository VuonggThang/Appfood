package com.example.app1

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.app1.databinding.ActivityDetailsBinding
import com.example.app1.model.CartItems
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDetailsBinding
    private var foodName :String?= null
    private var foodPrice :String?= null
    private var foodDescription :String?= null
    private var foodImage :String?= null
    private var foodIngredients :String?= null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // initialize Firebase database

        foodName = intent.getStringExtra("MenuItemName")
        foodDescription = intent.getStringExtra("MenuItemDescription")
        foodIngredients = intent.getStringExtra("MenuItemIngredients")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodImage = intent.getStringExtra("MenuItemImage")
        with(binding){
            detailFoodName.text = foodName
            detailDescription.text= foodDescription
            detailIngredients.text = foodIngredients
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into((detailFoodImage))
        }
        binding.imageButton.setOnClickListener{
            finish()
        }
        binding.addItemButton.setOnClickListener{
            addItemToCart()
        }
    }

    private fun addItemToCart() {
        val database= FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid?:""
        // create a cartItems object
        val cartItem = CartItems(foodName.toString(),foodPrice.toString(),foodDescription.toString(),foodImage.toString(),1)
        //save data to cart item to firebase database
        database.child("user").child(userId).child("CatItems").push().setValue(cartItem).addOnSuccessListener {
            Toast.makeText(this,"them items vao gio hang thang cong", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Item khong duoc them vao gio hang",Toast.LENGTH_SHORT).show()
        }
    }
}