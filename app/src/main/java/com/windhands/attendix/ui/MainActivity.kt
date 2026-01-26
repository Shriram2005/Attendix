package com.windhands.attendix.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - hosts the Navigation Component
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var backPressedTime: Long = 0
    private var backToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle Android 12+ splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupEdgeToEdge()
        setupBackNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, 0) // Let fragments handle padding
            insets
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestination = navController.currentDestination?.id
                
                // If we're at the root containers (employee/admin dashboard), implement double press to exit
                if (currentDestination == R.id.employee_nav_graph || 
                    currentDestination == R.id.admin_nav_graph) {
                    
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        // Second press within 2 seconds, exit the app
                        backToast?.cancel()
                        finish()
                    } else {
                        // First press, show toast
                        backToast?.cancel()
                        backToast = Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT)
                        backToast?.show()
                    }
                    backPressedTime = System.currentTimeMillis()
                    return
                }
                
                // For all other cases, let the navigation component handle it
                // This allows nested navigation graphs to handle their own back navigation
                if (!navController.popBackStack()) {
                    // If there's nothing to pop, we're at the start destination, so exit
                    finish()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    override fun onDestroy() {
        backToast?.cancel()
        super.onDestroy()
    }
}
