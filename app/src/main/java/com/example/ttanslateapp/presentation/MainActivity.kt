package com.example.ttanslateapp.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.exam.ExamReminder
import com.example.ttanslateapp.presentation.modify_word.ModifyWordModes
import com.example.ttanslateapp.presentation.word_list.WordListFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber
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
        setupNavigation()
        examReminder.setInitialReminderIfNeeded()


        // get text from selected items
        val text = intent
            .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        if(text != null) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            navController.navigate(
                WordListFragmentDirections.actionWordListFragmentToModifyWordFragment(
                    mode = ModifyWordModes.MODE_ADD, wordValue = text.toString())
            )
        }

        Timber.d("TEXT IS: ${text}")
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomBar = findViewById(R.id.bottom_app_bar)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.wordListFragment -> showBottomNav()
                R.id.examKnowledgeWordsFragment -> showBottomNav()
                R.id.settingsFragment -> showBottomNav()
                else -> hideBottomNav()
            }
        }

        bottomBar.setupWithNavController(navController)
//        // we can use bottomBar.setupWithNavController(navController) for base behavior but I want to reset the viewmodel state when user go out from exam tab
//        bottomBar
//            .setOnItemSelectedListener { menuItem ->
//                // clear state only fro exam tab
//                val isRestoreState =
//                    menuItem.itemId != R.id.examKnowledgeWordsFragment || navController.currentDestination?.id == R.id.examKnowledgeWordsFragment
//
//                val builder =
//                    NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(isRestoreState)
//
//                if (menuItem.order and Menu.CATEGORY_SECONDARY == 0) {
//                    builder.setPopUpTo(
//                        navController.graph.findStartDestination().id,
//                        inclusive = false,
//                        saveState = true
//                    )
//                }
//                val options = builder.build()
//
//                val weakReference = WeakReference(bottomBar)
//                navController.addOnDestinationChangedListener(
//                    object : NavController.OnDestinationChangedListener {
//                        override fun onDestinationChanged(
//                            controller: NavController,
//                            destination: NavDestination,
//                            arguments: Bundle?
//                        ) {
//                            navHostFragment.childFragmentManager.fragments.firstOrNull()
//                                ?.let { it as? ExamKnowledgeWordsFragment }?.run {
//                                    lolKek()
//                                }
//
//                            val view = weakReference.get()
//                            if (view == null) {
//                                navController.removeOnDestinationChangedListener(this)
//                                return
//                            }
//                            view.menu.forEach { item ->
//                                if (destination.id == item.itemId) {
//                                    item.isChecked = true
//                                }
//                            }
//                        }
//                    })
//                return@setOnItemSelectedListener try {
//                    navController.navigate(menuItem.itemId, null, options)
//                    navController.currentDestination?.id == menuItem.itemId
//                } catch (e: IllegalArgumentException) {
//                    false
//                }
//            }
    }

    private fun showBottomNav() {
        bottomBar.visibility = View.VISIBLE
    }

    private fun hideBottomNav() {
        bottomBar.visibility = View.GONE
    }
}
