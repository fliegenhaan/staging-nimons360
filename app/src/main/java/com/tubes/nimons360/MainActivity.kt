package com.tubes.nimons360

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.tubes.nimons360.databinding.ActivityMainBinding
import com.tubes.nimons360.ui.create_family.CreateFamilyActivity
import com.tubes.nimons360.ui.profile.ProfileActivity
import com.tubes.nimons360.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var offlineSnackbar: Snackbar? = null

    private val createFamilyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val familyId = result.data?.getIntExtra("familyId", -1) ?: -1
            if (familyId != -1) {
                val bundle = android.os.Bundle().apply { putInt("familyId", familyId) }
                navController.navigate(R.id.familyDetailFragment, bundle)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupNavigation()
        setupFab()
        setupNetworkObserver()
        setupToolbarAvatar()
    }

    private fun setupNavigation() {
        NavigationUI.setupWithNavController(binding.bottomNav, navController)
        setupActionBarWithNavController(navController)

        // Sembunyikan FAB dan bottom nav saat di FamilyDetail
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.familyDetailFragment -> {
                    binding.fabCreateFamily.hide()
                }
                else -> {
                    binding.fabCreateFamily.show()
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabCreateFamily.setOnClickListener {
            val intent = Intent(this, CreateFamilyActivity::class.java)
            createFamilyLauncher.launch(intent)
        }
    }

    private fun setupToolbarAvatar() {
        // Update initial dari TokenManager
        val userName = TokenManager.getUserName(this) ?: ""
        updateAvatarInitial(userName)
    }

    private fun updateAvatarInitial(name: String) {
        val initial = if (name.isNotEmpty()) name.first().uppercaseChar().toString() else "?"
        // Akan diupdate saat menu di-inflate
        binding.toolbar.tag = initial
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        val avatarItem = menu.findItem(R.id.action_profile)
        val actionView = avatarItem?.actionView
        val tvInitial = actionView?.findViewById<TextView>(R.id.tvAvatarInitial)
        val userName = TokenManager.getUserName(this) ?: ""
        tvInitial?.text = if (userName.isNotEmpty()) userName.first().uppercaseChar().toString() else "?"
        actionView?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNetworkObserver() {
        val app = application as Nimons360App
        lifecycleScope.launch {
            app.networkMonitor.isOnline.collect { isOnline ->
                if (!isOnline) {
                    if (offlineSnackbar == null || offlineSnackbar?.isShown == false) {
                        offlineSnackbar = Snackbar.make(
                            binding.root,
                            "Tidak ada koneksi internet",
                            Snackbar.LENGTH_INDEFINITE
                        ).apply {
                            setAnchorView(binding.bottomNav)
                            show()
                        }
                    }
                } else {
                    offlineSnackbar?.dismiss()
                    offlineSnackbar = null
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (application as Nimons360App).networkMonitor.start()
    }

    override fun onStop() {
        super.onStop()
        (application as Nimons360App).networkMonitor.stop()
    }

    fun refreshAvatarInitial() {
        invalidateOptionsMenu()
    }
}
