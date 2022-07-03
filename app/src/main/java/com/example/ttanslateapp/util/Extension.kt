package com.example.ttanslateapp.util

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.example.ttanslateapp.presentation.TranslateApp
import com.example.ttanslateapp.di.ApplicationComponent
import com.google.android.material.textfield.TextInputEditText

/**
 * Do exactly the same as a lazy, but without synchronization. Designed mostly
 * to use in activity, fragments, view models etc, where more then 1 thread doesn't
 * expected.
 */
fun <T> lazySimple(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

fun Fragment.getAppComponent(): ApplicationComponent =
    (requireActivity().application as TranslateApp).component

internal inline fun TextInputEditText.setOnTextChange(crossinline block: (p0: CharSequence?) -> Unit) =
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        override fun afterTextChanged(p0: Editable?) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = block(p0)
    })

fun View.t(resourceId: Int) = this.context.getString(resourceId)

