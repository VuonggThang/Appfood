package com.example.app1.adaptar

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app1.databinding.BuyAgainItemBinding

class BuyAgainAdapter(  private val buyAgainFoodName:MutableList<String>,
                        private val buyAgainFoodPrice:MutableList<String>,
                        private val buyAgainFoodImage:MutableList<String>,
                        private var requireContext: Context,
                        private val onOrderAgainListener: OnOrderAgainListener,
                        ) :
                        RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    // Tạo interface để xử lý sự kiện đặt hàng lại
    interface OnOrderAgainListener {
        fun onOrderAgain(foodName: String, foodPrice: String,foodImage: String)
    }
    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(buyAgainFoodName[position],buyAgainFoodPrice[position],buyAgainFoodImage[position])
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BuyAgainViewHolder(binding)
    }

    override fun getItemCount(): Int = buyAgainFoodName.size
    inner class BuyAgainViewHolder (private val binding: BuyAgainItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(foodName: String, foodPrice: String, foodImage: String) {
            binding.buyAgainFoodName.text = foodName
            binding.buyAgainFoodPrice.text = foodPrice
            val uriString = foodImage
            val uri = Uri.parse(uriString)
            Glide.with(requireContext).load(uri).into(binding.buyAgainFoodImage)

            //buy again
            binding.buyAgainFoodButton.setOnClickListener {
                onOrderAgainListener.onOrderAgain(foodName, foodPrice, foodImage) // Gọi listener khi nút được nhấn
            }


        }
    }
}