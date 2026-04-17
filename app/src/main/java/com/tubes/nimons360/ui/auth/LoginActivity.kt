package com.tubes.nimons360.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tubes.nimons360.MainActivity
import com.tubes.nimons360.data.api.RetrofitClient
import com.tubes.nimons360.databinding.ActivityLoginBinding
import com.tubes.nimons360.model.LoginRequest
import com.tubes.nimons360.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (TokenManager.isLoggedIn(this)) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            handleLogin()
        }

        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            handleLogin()
            true
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        setLoading(true)
        hideError()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(email = email, password = password)
                )

                if (response.isSuccessful) {
                    val loginData = response.body()?.data
                    val token = loginData?.token
                    if (token != null) {
                        TokenManager.saveToken(this@LoginActivity, token)
                        loginData.user.fullName.let { name ->
                            TokenManager.saveUserName(this@LoginActivity, name)
                            com.tubes.nimons360.utils.LocationState.cachedUserName = name
                        }
                        goToMain()
                    } else {
                        showError("Login gagal, coba lagi.")
                    }
                } else {
                    when (response.code()) {
                        401, 409 -> showError("Email atau password salah.")
                        500 -> showError("Server sedang bermasalah, coba lagi nanti.")
                        else -> showError("Login gagal (${response.code()}).")
                    }
                }
            } catch (e: Exception) {
                showError("Tidak dapat terhubung ke server. Periksa koneksi internet.")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}