package com.tubes.nimons360.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tubes.nimons360.Nimons360App
import com.tubes.nimons360.data.api.RetrofitClient
import com.tubes.nimons360.data.local.AppDatabase
import com.tubes.nimons360.data.repository.FamilyRepository
import com.tubes.nimons360.model.DiscoverFamily
import com.tubes.nimons360.model.MyFamily
import com.tubes.nimons360.utils.AppResult
import com.tubes.nimons360.utils.NetworkUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.distinctUntilChanged

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val myFamilies: List<MyFamily>,
        val discoverFamilies: List<DiscoverFamily>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FamilyRepository(
        RetrofitClient.instance,
        AppDatabase.getDatabase(app).pinnedFamilyDao(),
        app
    )

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val app = app.applicationContext as Nimons360App
        viewModelScope.launch {
            app.connectionRestored.filter {
                _uiState.value is HomeUiState.Error
            }.collect {
                loadData()
            }
        }
        loadData()
    }

    fun loadData() {
        if (!NetworkUtils.isConnected(getApplication())) {
            _uiState.value = HomeUiState.Error("Tidak ada koneksi internet")
            return
        }
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            val myFamiliesDeferred = async { repo.getMyFamilies() }
            val discoverDeferred = async { repo.discoverFamilies() }

            val myResult = myFamiliesDeferred.await()
            val discoverResult = discoverDeferred.await()

            if (myResult is AppResult.Error) {
                _uiState.value = HomeUiState.Error(myResult.message)
                return@launch
            }
            if (discoverResult is AppResult.Error) {
                _uiState.value = HomeUiState.Error(discoverResult.message)
                return@launch
            }

            _uiState.value = HomeUiState.Success(
                myFamilies = (myResult as AppResult.Success).data,
                discoverFamilies = (discoverResult as AppResult.Success).data
            )
        }
    }
}
