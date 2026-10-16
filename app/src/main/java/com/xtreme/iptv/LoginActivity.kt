package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.xtreme.iptv.api.XtreamApi
import com.xtreme.iptv.utils.Prefs
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Prefs.isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val hostEt = findViewById<EditText>(R.id.hostInput)
        val userEt = findViewById<EditText>(R.id.userInput)
        val passEt = findViewById<EditText>(R.id.passInput)
        val btn = findViewById<Button>(R.id.loginBtn)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val error = findViewById<TextView>(R.id.errorTxt)

        btn.setOnClickListener {
            val h = hostEt.text.toString().trim().trimEnd('/')
            val u = userEt.text.toString().trim()
            val p = passEt.text.toString().trim()

            if (h.isEmpty() || u.isEmpty() || p.isEmpty()) {
                error.text = getString(R.string.login_error_empty)
                error.visibility = View.VISIBLE
                return@setOnClickListener
            }

            error.visibility = View.GONE
            btn.visibility = View.INVISIBLE
            progress.visibility = View.VISIBLE

            thread {
                Prefs.host = h
                Prefs.user = u
                Prefs.pass = p
                val ok = XtreamApi.login()

                runOnUiThread {
                    btn.visibility = View.VISIBLE
                    progress.visibility = View.GONE

                    if (ok) {
                        Prefs.isLoggedIn = true
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        error.text = getString(R.string.login_error_invalid)
                        error.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
