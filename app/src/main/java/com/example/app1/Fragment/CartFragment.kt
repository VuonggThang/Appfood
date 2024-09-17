package com.example.app1.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app1.CongratsBottomSheet
import com.example.app1.PayOutActivity
import com.example.app1.R
import com.example.app1.adaptar.CartAdapter
import com.example.app1.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames :MutableList<String>
    private lateinit var foodPrices :MutableList<String>
    private lateinit var foodDescriptions :MutableList<String>
    private lateinit var foodImagesUri :MutableList<String>
    private lateinit var foodIngredients :MutableList<String>
    private lateinit var quantity :MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(inflater,container,false)

        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = adapter
        binding.proceedButton.setOnClickListener{
            val intent = Intent(requireActivity(),PayOutActivity::class.java)
            startActivity(intent)
        }


        return binding.root
    }

    companion object {

    }
}