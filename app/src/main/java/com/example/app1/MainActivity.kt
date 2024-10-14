package com.example.app1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.app1.databinding.ActivityMainBinding
import com.example.app1.databinding.NotificationItemBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
            var navController = findNavController(R.id.fragmentContainerView)
            var bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomnav.setupWithNavController(navController)
            binding.notificationButton.setOnClickListener{
                val bottomSheetDialog = Notifaction_Bottom_Fragment()
                bottomSheetDialog.show(supportFragmentManager,"Test")
            }
    }
}
