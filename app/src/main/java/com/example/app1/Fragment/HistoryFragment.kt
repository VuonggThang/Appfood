package com.example.app1.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.app1.R
import com.example.app1.RecentOrderItems
import com.example.app1.adaptar.BuyAgainAdapter
import com.example.app1.databinding.FragmentHistoryBinding
import com.example.app1.model.CartItems
import com.example.app1.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.navigation.fragment.findNavController





class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private var listOfOrderItem: MutableList<OrderDetails> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Retrieve and display the user order history
        retrieveBuyHistory()

        binding.recentbuyitem.setOnClickListener {
            seeItemsRecentBuy()
        }

        binding.receivedButton.setOnClickListener {
            updateOrderStatus()
            Toast.makeText(requireContext(), "Bạn đã thanh toán tiền", Toast.LENGTH_SHORT).show()
        }

        binding.cancelButton.setOnClickListener {
            Log.d("HistoryFragment", "Cancel button clicked") // Thêm log
            if (listOfOrderItem.isNotEmpty()) {
                val currentOrder = listOfOrderItem.first()  // Lấy đơn hàng đầu tiên
                val bundle = Bundle().apply {
                    putSerializable("currentOrder", currentOrder)  // Truyền đối tượng đơn hàng
                }

                // Sử dụng NavController để điều hướng đến CancelFragment
                findNavController().navigate(R.id.cancelFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Không có đơn hàng để hủy", Toast.LENGTH_SHORT).show()
            }
        }



        return binding.root
    }

    private fun updateOrderStatus() {
        if (listOfOrderItem.isNotEmpty()) {
            val itemPushKey = listOfOrderItem[0].itemPushKey
            if (itemPushKey != null) {
                val completeOrderReference = database.reference.child("CompletedOrder").child(itemPushKey)
                completeOrderReference.child("paymentReceived").setValue(true)
            } else {
                Toast.makeText(requireContext(), "Không có đơn hàng để cập nhật", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Danh sách đơn hàng trống, không thể cập nhật trạng thái.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seeItemsRecentBuy() {
        listOfOrderItem.firstOrNull()?.let {
            val intent = Intent(requireContext(), RecentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem", ArrayList(listOfOrderItem))
            startActivity(intent)
        }
    }

    private fun retrieveBuyHistory() {
        binding.recentbuyitem.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid ?: ""
        val buyItemReference: DatabaseReference = database.reference.child("user").child(userId).child("BuyHistory")
        buyItemReference.orderByChild("currentTime").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (buySnapshot in snapshot.children) {
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                    buyHistoryItem?.let {
                        listOfOrderItem.add(it)
                    }
                }
                listOfOrderItem.reverse()
                if (listOfOrderItem.isNotEmpty()) {
                    setDataInRecentBuyItem()
                    setPreviousBuyItemsRecyclerView()
                } else {
                    Toast.makeText(requireContext(), "Không có lịch sử mua hàng.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu từ Firebase.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setDataInRecentBuyItem() {
        if (listOfOrderItem.isNotEmpty()) {
            binding.recentbuyitem.visibility = View.VISIBLE
            val recentOrderItem = listOfOrderItem.firstOrNull()
            recentOrderItem?.let {
                val orderId = it.itemPushKey // Sử dụng orderId để lắng nghe thay đổi
                val orderRef = database.reference.child("OrderDetails").child(orderId!!)
                val cancelOrderRef = database.reference.child("CancelOrders").child(orderId)
                val completedOrderRef = database.reference.child("CompletedOrder").child(orderId)

                // Hiển thị thông tin đơn hàng từ OrderDetails
                orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Nếu đơn hàng vẫn tồn tại trong OrderDetails
                            val isCancelled = snapshot.child("cancelled").getValue(Boolean::class.java) ?: false
                            val foodNames = snapshot.child("foodNames").children.map { it.value.toString() }
                            val foodPrices = snapshot.child("foodPrices").children.map { it.value.toString() }
                            val foodImages = snapshot.child("foodImages").children.map { it.value.toString() }

                            // Hiển thị thông tin đơn hàng
                            binding.buyAgainFoodName.text = foodNames.firstOrNull() ?: "Không có thông tin"
                            binding.buyAgainFoodPrice.text = foodPrices.firstOrNull() ?: "0"
                            if (foodImages.isNotEmpty()) {
                                val uri = Uri.parse(foodImages.first())
                                Glide.with(requireContext()).load(uri).into(binding.buyAgainFoodImage)
                            }

                            // Cập nhật UI dựa trên trạng thái đơn hàng
                            updateUIBasedOnOrderStatus(isCancelled, recentOrderItem)

                        } else {
                            // Kiểm tra trong CompletedOrders
                            completedOrderRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(completedSnapshot: DataSnapshot) {
                                    if (completedSnapshot.exists()) {
                                        // Nếu đơn hàng đã chuyển vào CompletedOrders
                                        val isCancelled = completedSnapshot.child("cancelled").getValue(Boolean::class.java) ?: false
                                        updateUIBasedOnOrderStatus(isCancelled, recentOrderItem, completedSnapshot)
                                    } else {
                                        // Nếu đơn hàng không tồn tại trong cả OrderDetails và CompletedOrders
                                        cancelOrderRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(cancelSnapshot: DataSnapshot) {
                                                if (cancelSnapshot.exists()) {
                                                    // Nếu đơn hàng tồn tại trong CancelOrders
                                                    val isCancelled = cancelSnapshot.child("cancelled").getValue(Boolean::class.java) ?: true
                                                    // Hiển thị thông tin từ CancelOrders
                                                    val foodNames = cancelSnapshot.child("foodNames").children.map { it.value.toString() }
                                                    val foodPrices = cancelSnapshot.child("foodPrices").children.map { it.value.toString() }
                                                    val foodImages = cancelSnapshot.child("foodImages").children.map { it.value.toString() }

                                                    // Hiển thị thông tin đơn hàng bị hủy
                                                    binding.buyAgainFoodName.text = foodNames.firstOrNull() ?: "Không có thông tin"
                                                    binding.buyAgainFoodPrice.text = foodPrices.firstOrNull() ?: "0"
                                                    if (foodImages.isNotEmpty()) {
                                                        val uri = Uri.parse(foodImages.first())
                                                        Glide.with(requireContext()).load(uri).into(binding.buyAgainFoodImage)
                                                    }

                                                    // Cập nhật UI cho đơn hàng bị hủy
                                                    updateUIBasedOnOrderStatus(isCancelled, recentOrderItem)

                                                } else {
                                                    // Đơn hàng không tồn tại ở đâu cả
                                                    Toast.makeText(requireContext(), "Đơn hàng không tồn tại.", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Toast.makeText(requireContext(), "Không thể tải dữ liệu đơn hàng.", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(requireContext(), "Không thể tải dữ liệu đơn hàng.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Không thể tải dữ liệu đơn hàng.", Toast.LENGTH_SHORT).show()
                    }
                })

                binding.recentbuyitem.setOnClickListener {
                    if (recentOrderItem.isCancelled) {
                        // TODO: Chuyển sang màn hình thông báo đơn hàng bị hủy
                    } else {
                        seeItemsRecentBuy()
                        Toast.makeText(requireContext(), "Chi tiết đơn hàng", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Không có lịch sử mua hàng", Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateUIBasedOnOrderStatus(
        isCancelled: Boolean,
        recentOrderItem: OrderDetails,
        completedSnapshot: DataSnapshot? = null
    ) {
        if (isCancelled) {
            // Hiển thị thông báo đơn hàng bị hủy
            binding.orderStatus.setImageResource(R.drawable.sademoji)
            binding.receivedButton.visibility = View.GONE // Ẩn nút thanh toán
            Toast.makeText(requireContext(), "Đơn hàng của bạn đã bị hủy.", Toast.LENGTH_SHORT).show()
        } else {
            // Kiểm tra xem completedSnapshot có khác null không
            if (completedSnapshot != null) {
                // Đơn hàng đã hoàn thành
                val foodName = completedSnapshot.child("foodNames").children.firstOrNull()?.value.toString()
                val foodPrice = completedSnapshot.child("foodPrices").children.firstOrNull()?.value.toString()
                val foodImage = completedSnapshot.child("foodImages").children.firstOrNull()?.value.toString()

                binding.buyAgainFoodName.text = foodName
                binding.buyAgainFoodPrice.text = foodPrice
                val uri = Uri.parse(foodImage)
                Glide.with(requireContext()).load(uri).into(binding.buyAgainFoodImage)

                binding.orderStatus.setImageResource(R.drawable.illustration)
                binding.receivedButton.visibility = View.VISIBLE // Hiển thị nút thanh toán

                // Xử lý sự kiện nhấn vào nút receivedButton
                binding.receivedButton.setOnClickListener {
                    val orderId = recentOrderItem.itemPushKey // Lấy ID của đơn hàng
                    if (orderId != null) { // Kiểm tra orderId không phải null
                        val orderRef = database.reference.child("CompletedOrder").child(orderId)

                        // Cập nhật giá trị paymentReceived thành true
                        orderRef.child("paymentReceived").setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Đơn hàng đã được thanh toán.", Toast.LENGTH_SHORT).show()
                                // Ẩn nút sau khi thanh toán
                                binding.receivedButton.visibility = View.GONE
                            } else {
                                Toast.makeText(requireContext(), "Lỗi khi thanh toán. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "ID đơn hàng không hợp lệ.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Hiển thị thông tin từ OrderDetails
                binding.buyAgainFoodName.text = recentOrderItem.foodNames?.firstOrNull() ?: ""
                binding.buyAgainFoodPrice.text = recentOrderItem.foodPrices?.firstOrNull() ?: ""
                val image = recentOrderItem.foodImages?.firstOrNull() ?: ""
                val uri = Uri.parse(image)
                Glide.with(requireContext()).load(uri).into(binding.buyAgainFoodImage)

                // Kiểm tra trạng thái orderAccepted
                if (recentOrderItem.orderAccepted) {
                    binding.orderStatus.setImageResource(R.drawable.illustration)
                    binding.receivedButton.visibility = View.VISIBLE
                } else {
                    binding.orderStatus.setImageResource(R.drawable.truck)
                    binding.receivedButton.visibility = View.GONE // Ẩn nút nếu orderAccepted là false
                }
            }
        }
    }


    private fun setPreviousBuyItemsRecyclerView() {
        val buyAgainFoodName = mutableListOf<String>()
        val buyAgainFoodPrice = mutableListOf<String>()
        val buyAgainFoodImage = mutableListOf<String>()

        for (i in 1 until listOfOrderItem.size) {
            listOfOrderItem[i].foodNames?.firstOrNull()?.let {
                buyAgainFoodName.add(it)
                listOfOrderItem[i].foodPrices?.firstOrNull()?.let { price ->
                    buyAgainFoodPrice.add(price)
                    listOfOrderItem[i].foodImages?.firstOrNull()?.let { image ->
                        buyAgainFoodImage.add(image)
                    }
                }
            }
        }

        val rv = binding.BuyAgainRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())

        buyAgainAdapter = BuyAgainAdapter(
            buyAgainFoodName,
            buyAgainFoodPrice,
            buyAgainFoodImage,
            requireContext(),
            object : BuyAgainAdapter.OnOrderAgainListener {
                override fun onOrderAgain(foodName: String, foodPrice: String, foodImage: String) {
                    addToCart(foodName, foodPrice, foodImage)
                }
            }
        )

        rv.adapter = buyAgainAdapter
    }

    private fun addToCart(foodName: String, foodPrice: String, foodImage: String) {
        userId = auth.currentUser?.uid ?: ""
        val cartItemsReference = database.reference.child("user").child(userId).child("CartItems")

        cartItemsReference.orderByChild("foodName").equalTo(foodName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val existingItem = snapshot.children.firstOrNull()
                    val currentQuantity = existingItem?.child("foodQuantity")?.getValue(Int::class.java) ?: 0
                    val itemKey = existingItem?.key

                    cartItemsReference.child(itemKey!!).child("foodQuantity").setValue(currentQuantity + 1).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Đã tăng số lượng món ăn trong giỏ hàng", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val cartItem = CartItems(
                        foodName = foodName,
                        foodPrice = foodPrice,
                        foodImage = foodImage,
                        foodQuantity = 1
                    )

                    cartItemsReference.push().setValue(cartItem).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Có lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
