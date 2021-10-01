package app.web.gourav_khunger.textrecognizer

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.view.animation.Animation
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import app.web.gourav_khunger.textrecognizer.R
import android.content.Intent
import android.view.animation.AnimationUtils
import android.widget.ImageView
import app.web.gourav_khunger.textrecognizer.Home

class Splash : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var title: TextView
    private lateinit var slogan: TextView
    private lateinit var fade: Animation
    private lateinit var zoom: Animation
    private lateinit var preferences: SharedPreferences
    private var editor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isDark = preferences.getBoolean("theme", false)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setContentView(R.layout.activity_splash)
        image = findViewById(R.id.image_splash)
        title = findViewById(R.id.appTitle)
        slogan = findViewById(R.id.appSlogan)
        fade = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.fade_in
        )
        zoom = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_in)
        fade.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                try {
                    Thread.sleep(1000)
                    startActivity(Intent(this@Splash, Home::class.java))
                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
                    finish()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        image.startAnimation(fade)
        title.startAnimation(zoom)
        slogan.startAnimation(zoom)
    }

    val isDarkTheme: Boolean
        get() = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
}