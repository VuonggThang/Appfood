package com.example.app1.adaptar

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app1.databinding.CartItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class CartAdapter (
    private val context:Context,
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private val cartDescriptions:MutableList<String>,
    private val cartImages: MutableList<String>,
    private val cartQuantity:MutableList<Int>,
    private val cartIngredient:MutableList<String>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    //instance Firebase
    private val auth = FirebaseAuth.getInstance()
    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid?:""
        val cartTimeNumber = cartItems.size
        itemQuantities = IntArray(cartTimeNumber){1}
        cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
    }
    companion object{
        private var itemQuantities: IntArray = intArrayOf()
        private lateinit var cartItemsReference: DatabaseReference
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }
    override fun getItemCount(): Int = cartItems.size
    //get update quantities
    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val itemQuantity = mutableListOf<Int>()
        itemQuantity.addAll(cartQuantity)
        return itemQuantity
    }
    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val quantity = itemQuantities[position]
                cartFoodName.text = cartItems[position]
                cartItemPrice.text = cartItemPrices[position]
                //load image using glide
                val uriString = cartImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImage)
                catItemQuantity.text = quantity.toString()
                minusbutton.setOnClickListener {
                    deceaseQuantity(position)
                }
                plusbutton.setOnClickListener {
                    increaseQuantity(position)
                }
                deleteButton.setOnClickListener {
                    val itemPosition = adapterPosition
                    if(itemPosition != RecyclerView.NO_POSITION){
                        deleteItems(itemPosition)
                    }

                }
            }
        }
        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                cartQuantity[position] = itemQuantities[position]
                binding.catItemQuantity.text = itemQuantities[position].toString()
            }
        }
        private fun deceaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                cartQuantity[position] = itemQuantities[position]
                binding.catItemQuantity.text = itemQuantities[position].toString()
            }
        }

        private fun deleteItems(position: Int) {
            val positionRetrieve = position
            getUniqueKeyAtPosition(positionRetrieve){uniqueKey ->
                if (uniqueKey !=null){
                    removeItem(position,uniqueKey)
                }
            }
        }
        private fun removeItem(position: Int, uniqueKey: String) {
            if (uniqueKey!= null){

                cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {

                    cartItems.removeAt(position)
                    cartImages.removeAt(position)
                    //cartDescriptions.removeAt(position)
                    cartQuantity.removeAt(position)
                    cartItemPrices.removeAt(position)
                    //cartIngredient.removeAt(position)
                    Toast.makeText(context,"Xoa Item",Toast.LENGTH_SHORT).show()
                    // update itemQuantities
                    itemQuantities = itemQuantities.filterIndexed{index, i -> index!= position }.toIntArray()
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position,cartItems.size)

                }.addOnFailureListener {
                    Toast.makeText(context,"Xoa that bai", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete:(String?)->Unit) {
            cartItemsReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey:String?=null
                    //loop for snapshot children
                    snapshot.children.forEachIndexed{index, dataSnapshot ->
                        if (index == positionRetrieve){
                            uniqueKey = dataSnapshot.key
                            return@forEachIndexed
                        }
                    }
                    onComplete(uniqueKey)
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}
//
//package com.example.app1.adaptar
//
//import android.app.Activity
//import android.content.Context
//import android.net.Uri
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.app1.databinding.CartItemBinding
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.ValueEventListener
//
//class CartAdapter(
//    private val context: Context,
//    private val cartItems: MutableList<String>,
//    private val cartItemPrices: MutableList<String>,
//    private val cartDescriptions: MutableList<String>,
//    private val cartImages: MutableList<String>,
//    private val cartQuantity: MutableList<Int>,
//    private val cartIngredient: MutableList<String>
//) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
//
//    // Firebase authentication
//    private val auth = FirebaseAuth.getInstance()
//    private var itemQuantities: IntArray
//    private lateinit var cartItemsReference: DatabaseReference
//
//    init {
//        val database = FirebaseDatabase.getInstance()
//        val userId = auth.currentUser?.uid ?: ""
//        val cartTimeNumber = cartItems.size
//        itemQuantities = IntArray(cartTimeNumber) { 1 }
//        cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
//        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return CartViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
//        holder.bind(position)
//    }
//
//    override fun getItemCount(): Int = cartItems.size
//
//    // Get updated quantities
//    fun getUpdatedItemsQuantities(): MutableList<Int> {
//        return cartQuantity.toMutableList()
//    }
//
//    inner class CartViewHolder(private val binding: CartItemBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(position: Int) {
//            if (position < cartItems.size) {
//                binding.apply {
//                    val quantity = itemQuantities[position]
//                    cartFoodName.text = cartItems[position]
//                    cartItemPrice.text = cartItemPrices[position]
//                    val uri = Uri.parse(cartImages[position])
//                    Glide.with(context).load(uri).into(cartImage)
//                    catItemQuantity.text = quantity.toString()
//
//                    minusbutton.setOnClickListener { decreaseQuantity(position) }
//                    plusbutton.setOnClickListener { increaseQuantity(position) }
//                    deleteButton.setOnClickListener {
//                        if (adapterPosition != RecyclerView.NO_POSITION) {
//                            deleteItems(adapterPosition)
//                        }
//                    }
//                }
//            } else {
//                Log.e("CartAdapter", "Position $position is out of bounds for list size ${cartItems.size}")            }
//        }
//
//        private fun increaseQuantity(position: Int) {
//            if (itemQuantities[position] < 10) {
//                itemQuantities[position]++
//                cartQuantity[position] = itemQuantities[position]
//                binding.catItemQuantity.text = itemQuantities[position].toString()
//            }
//        }
//
//        private fun decreaseQuantity(position: Int) {
//            if (itemQuantities[position] > 1) {
//                itemQuantities[position]--
//                cartQuantity[position] = itemQuantities[position]
//                binding.catItemQuantity.text = itemQuantities[position].toString()
//            }
//        }
//
//        private fun deleteItems(position: Int) {
//            getUniqueKeyAtPosition(position) { uniqueKey ->
//                if (!uniqueKey.isNullOrEmpty()) {
//                    removeItem(position, uniqueKey)
//                } else {
//                    Toast.makeText(context, "Không tìm thấy khóa duy nhất cho mục này", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
////        private fun removeItem(position: Int, uniqueKey: String) {
////            cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
////                if (position < cartItems.size) {
////                    cartItems.removeAt(position)
////                    cartImages.removeAt(position)
////                    cartDescriptions.removeAt(position)
////                    cartQuantity.removeAt(position)
////                    cartItemPrices.removeAt(position)
////                    cartIngredient.removeAt(position)
////
////                    // Update itemQuantities array safely
////                    if (itemQuantities.size > position) {
////                        itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()
////                    }
////
////                    Toast.makeText(context, "Đã xóa mục", Toast.LENGTH_SHORT).show()
////
////                    // Notify RecyclerView of item removal
////                    notifyItemRemoved(position)
////                    notifyItemRangeChanged(position, cartItems.size)
////                } else {
////                    Toast.makeText(context, "Vị trí không hợp lệ để xóa", Toast.LENGTH_SHORT).show()
////                }
////            }.addOnFailureListener {
////                Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
////            }
////        }
//private fun removeItem(position: Int, uniqueKey: String) {
//    cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
//        // Kiểm tra xem context có còn hợp lệ trước khi tiếp tục thao tác
//        if (context is Activity && !(context as Activity).isFinishing && !(context as Activity).isDestroyed) {
//            if (position < cartItems.size) {
//                cartItems.removeAt(position)
//                cartImages.removeAt(position)
//                cartDescriptions.removeAt(position)
//                cartQuantity.removeAt(position)
//                cartItemPrices.removeAt(position)
//                cartIngredient.removeAt(position)
//
//                // Update itemQuantities array safely
//                if (itemQuantities.size > position) {
//                    itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()
//                }
//
//                Toast.makeText(context, "Đã xóa mục", Toast.LENGTH_SHORT).show()
//
//                // Cập nhật RecyclerView
//                notifyItemRemoved(position)
//                notifyItemRangeChanged(position, cartItems.size)
//            } else {
//                Toast.makeText(context, "Vị trí không hợp lệ để xóa", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }.addOnFailureListener {
//        if (context is Activity && !(context as Activity).isFinishing && !(context as Activity).isDestroyed) {
//            Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
//
//
//        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
//            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    var uniqueKey: String? = null
//                    snapshot.children.forEachIndexed { index, dataSnapshot ->
//                        if (index == positionRetrieve) {
//                            uniqueKey = dataSnapshot.key
//                            return@forEachIndexed
//                        }
//                    }
//                    onComplete(uniqueKey)
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("CartAdapter", "Database error: ${error.message}")
//                }
//            })
//        }
//    }
//}

