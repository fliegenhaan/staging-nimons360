package com.tubes.nimons360.ui.create_family

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tubes.nimons360.databinding.ActivityCreateFamilyBinding

class CreateFamilyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateFamilyBinding
    private val viewModel: CreateFamilyViewModel by viewModels()
    private lateinit var iconAdapter: IconAdapter

    private val iconUrls = (1..8).map {
        "https://mad.labpro.hmif.dev/assets/family_icon_$it.png"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFamilyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupIconGrid()
        setupCreateButton()
        observeViewModel()
    }

    private fun setupIconGrid() {
        iconAdapter = IconAdapter(iconUrls) { /* selection handled internally */ }
        binding.rvIcons.apply {
            layoutManager = GridLayoutManager(this@CreateFamilyActivity, 4)
            adapter = iconAdapter
        }
    }

    private fun setupCreateButton() {
        binding.btnCreate.setOnClickListener {
            val name = binding.etFamilyName.text?.toString()?.trim() ?: ""
            val iconUrl = iconAdapter.getSelectedIconUrl()

            if (name.isEmpty()) {
                binding.tilFamilyName.error = "Nama keluarga tidak boleh kosong"
                return@setOnClickListener
            } else {
                binding.tilFamilyName.error = null
            }

            if (iconUrl == null) {
                Snackbar.make(binding.root, "Pilih ikon keluarga terlebih dahulu", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createFamily(name, iconUrl)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is CreateFamilyUiState.Loading -> setLoading(true)
                is CreateFamilyUiState.Success -> {
                    setLoading(false)
                    val resultIntent = Intent().apply {
                        putExtra("familyId", state.familyId)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is CreateFamilyUiState.Error -> {
                    setLoading(false)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreate.isEnabled = !isLoading
        binding.etFamilyName.isEnabled = !isLoading
    }
}
