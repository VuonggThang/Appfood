
package com.example.app1.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.app1.MenuBootomSheetFragment
import com.example.app1.R
import com.example.app1.adaptar.MenuAdapter
import com.example.app1.databinding.FragmentHomeBinding
import com.example.app1.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewAllMenu.setOnClickListener {
            val bottomSheetDialog = MenuBootomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "Test")
        }

        // Retrieve and display popular menu items
        retrieveAndDisplayPopularItems()
        return binding.root
    }

    private fun retrieveAndDisplayPopularItems() {
        // get reference to the database
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("menu")
        menuItems = mutableListOf()

        // retrieve menu items from the database
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let { menuItems.add(it) }
                }
                // display a random popular items
                randomPopularItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load menu items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun randomPopularItems() {
        // create a shuffled list of menu items
        val index = menuItems.indices.toList().shuffled()
        val numItemToShow = 6
        val subsetMenuItems = index.take(numItemToShow).map { menuItems[it] }
        setPopularItemsAdapter(subsetMenuItems)
    }

    private fun setPopularItemsAdapter(subsetMenuItems: List<MenuItem>) {
        val adapter = MenuAdapter(subsetMenuItems, requireContext())
        binding.PopulerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.PopulerRecyclerView.adapter = adapter
    }

    private fun retrieveAndDisplayBanners() {
        val bannerRef = database.reference.child("banners")
        val imageList = ArrayList<SlideModel>()

        bannerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imageList.clear()  // Xóa các banner trước đó nếu có
                for (bannerSnapshot in snapshot.children) {
                    val imageUrl = bannerSnapshot.child("imageUrl").value.toString()
                    imageList.add(SlideModel(imageUrl, ScaleTypes.FIT))  // Thêm URL vào SlideModel
                }
                // Thiết lập danh sách ảnh cho ImageSlider
                binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu có
                Toast.makeText(requireContext(), "Không tải được banner", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveAndDisplayBanners()

        // Uncomment this if you want to add click listener for images
        binding.imageSlider.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {
                // Handle double click if needed
            }

            override fun onItemSelected(position: Int) {
                val itemMessage = "Selected Image $position"
                Toast.makeText(requireContext(), itemMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
