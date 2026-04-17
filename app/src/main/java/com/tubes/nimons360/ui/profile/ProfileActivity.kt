package com.tubes.nimons360.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.tubes.nimons360.Nimons360App
import com.tubes.nimons360.databinding.ActivityProfileBinding
import com.tubes.nimons360.ui.auth.LoginActivity
import com.tubes.nimons360.utils.TokenManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        observeViewModel()

        binding.btnEditName.setOnClickListener {
            val currentName = (viewModel.userState.value as? UserUiState.Success)?.user?.fullName ?: ""
            EditNameBottomSheet.newInstance(currentName)
                .setOnSaveCallback { newName -> viewModel.updateName(newName) }
                .show(supportFragmentManager, "edit_name")
        }

        binding.btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadUser()
    }

    private fun observeViewModel() {
        viewModel.userState.observe(this) { state ->
            when (state) {
                is UserUiState.Loading -> setLoading(true)
                is UserUiState.Success -> {
                    setLoading(false)
                    val user = state.user
                    binding.tvAvatarInitial.text = user.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    binding.tvFullName.text = user.fullName
                    binding.tvEmail.text = user.email
                    binding.tvNim.text = "NIM: ${user.nim}"
                }
                is UserUiState.Error -> {
                    setLoading(false)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UpdateUiState.Loading -> setLoading(true)
                is UpdateUiState.Success -> {
                    setLoading(false)
                    viewModel.loadUser()
                    viewModel.resetUpdateState()
                    // Refresh avatar di MainActivity
                    (applicationContext as? Nimons360App)
                    Snackbar.make(binding.root, "Nama berhasil diperbarui", Snackbar.LENGTH_SHORT).show()
                }
                is UpdateUiState.Error -> {
                    setLoading(false)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetUpdateState()
                }
                else -> {}
            }
        }
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Apakah kamu yakin ingin keluar?")
            .setPositiveButton("Keluar") { _, _ ->
                signOut()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun signOut() {
        TokenManager.clearToken(this)
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnEditName.isEnabled = !isLoading
        binding.btnSignOut.isEnabled = !isLoading
    }
}
