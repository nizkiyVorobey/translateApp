package com.example.ttanslateapp.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.exam.ExamReminder
import com.google.android.material.bottomnavigation.BottomNavigationView
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var examReminder: ExamReminder
    lateinit var listener: NavController.OnDestinationChangedListener
    private lateinit var bottomBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as TranslateApp).component.inject(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomBar = findViewById(R.id.bottom_app_bar)
        bottomBar.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.wordListFragment -> showBottomNav()
                R.id.examKnowledgeWordsFragment -> showBottomNav()
                R.id.settingsFragment -> showBottomNav()
                else -> hideBottomNav()
            }
        }
    }

    private fun showBottomNav() {
        bottomBar.visibility = View.VISIBLE
    }

    private fun hideBottomNav() {
        bottomBar.visibility = View.GONE
    }
}
