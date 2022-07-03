package com.example.ttanslateapp.presentation.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.ViewModelFactory
import com.example.ttanslateapp.util.lazySimple
import javax.inject.Inject

typealias BindingInflater<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

/** Simple wrapper of all base functionality and improve quality of life =) */
abstract class BaseFragment<T : ViewBinding> : Fragment() {

    @Inject
    protected lateinit var viewModelFactory: ViewModelFactory

    private var _binding: T? = null
    protected val binding get() = _binding!!

    abstract val bindingInflater: BindingInflater<T>

    // FIXME make it automaticly
    fun goBack() {
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Makes obtaining process of view models much easier */
    protected fun <T> viewModels(getter: ViewModelProvider.() -> T): Lazy<T> =
        lazySimple { ViewModelProvider(this, viewModelFactory).getter() }

}