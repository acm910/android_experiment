package com.example.experiment

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.experiment.data.session.SessionManager

/**
 * 登录页：当前使用本地模拟登录，成功后写入 SessionManager。
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        val root = findViewById<android.view.View>(R.id.loginRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername = findViewById<EditText>(R.id.etLoginUsername)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val cbRememberUsername = findViewById<CheckBox>(R.id.cbRememberUsername)
        val cbRememberPassword = findViewById<CheckBox>(R.id.cbRememberPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnCancel = findViewById<Button>(R.id.btnCancelLogin)

        // 进入页面时回填上次输入，密码是否回填由“记住密码”开关控制。
        cbRememberUsername.isChecked = sessionManager.isRememberUsernameEnabled()
        cbRememberPassword.isChecked = sessionManager.isRememberPasswordEnabled()
        if (cbRememberUsername.isChecked) {
            etUsername.setText(sessionManager.getLastUsername())
        }
        if (cbRememberPassword.isChecked) {
            etPassword.setText(sessionManager.getSavedPassword())
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val rememberUsername = cbRememberUsername.isChecked
            val rememberPassword = cbRememberPassword.isChecked

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, R.string.login_input_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sessionManager.saveLogin(username, "ic_launcher_round")

            sessionManager.setRememberUsernameEnabled(rememberUsername)
            if (rememberUsername) {
                sessionManager.setLastUsername(username)
            } else {
                sessionManager.setLastUsername("")
            }

            sessionManager.setRememberPasswordEnabled(rememberPassword)
            if (rememberPassword) {
                sessionManager.setSavedPassword(password)
            } else {
                sessionManager.clearSavedPassword()
            }

            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}

