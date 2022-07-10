package com.bluethunder.tar2.ui.splash

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.databinding.ActivitySplashBinding
import com.bluethunder.tar2.ui.home.MainActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed({
            openHomeActivity()
        }, 2000)

    }

    private fun openHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(
            intent,
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
        finish()
    }
}