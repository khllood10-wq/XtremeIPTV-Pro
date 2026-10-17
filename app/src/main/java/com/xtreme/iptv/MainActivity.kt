package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xtreme.iptv.utils.Prefs

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_live -> replaceFragment(LiveFragment())
                R.id.nav_movies -> replaceFragment(MoviesFragment())
                R.id.nav_series -> replaceFragment(SeriesFragment())
            }
            true
        }

        // فتح القائمة الجانبية عند الضغط على الأيقونة اليمنى
        findViewById<android.widget.ImageView>(R.id.openDrawerBtn).setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.END)
        }

        // أزرار القائمة الجانبية
        findViewById<android.widget.TextView>(R.id.menu_settings).setOnClickListener {
            Toast.makeText(this, "صفحة الإعدادات قريباً...", Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.END)
        }

        findViewById<android.widget.TextView>(R.id.menu_about).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("حول التطبيق")
                .setMessage("Xtreme IPTV Pro\nالإصدار 1.0.0\n\nمشغل IPTV احترافي.")
                .setPositiveButton("حسناً", null)
                .show()
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.END)
        }

        findViewById<android.widget.TextView>(R.id.menu_logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("تسجيل الخروج")
                .setMessage("هل أنت متأكد من تسجيل الخروج؟")
                .setPositiveButton("نعم") { _, _ ->
                    Prefs.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
