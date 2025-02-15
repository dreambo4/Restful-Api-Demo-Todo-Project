package com.yr.restfulapidemo.view.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.yr.restfulapidemo.util.ViewBindingUtil

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    private var _binding: VB? = null
    val binding: VB get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        setContentView(binding.root)
    }
}
