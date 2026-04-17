package com.tubes.nimons360.ui.create_family

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tubes.nimons360.data.api.RetrofitClient
import com.tubes.nimons360.data.local.AppDatabase
import com.tubes.nimons360.data.repository.FamilyRepository
import com.tubes.nimons360.utils.AppResult
import com.tubes.nimons360.utils.NetworkUtils
import kotlinx.coroutines.launch

sealed class CreateFamilyUiState {
    object Idle : CreateFamilyUiState()
    object Loading : CreateFamilyUiState()
    data class Success(val familyId: Int) : CreateFamilyUiState()
    data class Error(val message: String) : CreateFamilyUiState()
}

class CreateFamilyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FamilyRepository(
        RetrofitClient.instance,
        AppDatabase.getDatabase(app).pinnedFamilyDao(),
        app
    )

    private val _uiState = MutableLiveData<CreateFamilyUiState>(CreateFamilyUiState.Idle)
    val uiState: LiveData<CreateFamilyUiState> = _uiState

    fun createFamily(name: String, iconUrl: String) {
        if (!NetworkUtils.isConnected(getApplication())) {
            _uiState.value = CreateFamilyUiState.Error("Tidak ada koneksi internet")
            return
        }
        _uiState.value = CreateFamilyUiState.Loading
        viewModelScope.launch {
            when (val result = repo.createFamily(name, iconUrl)) {
                is AppResult.Success -> {
                    _uiState.postValue(CreateFamilyUiState.Success(result.data.id))
                }
                is AppResult.Error -> {
                    _uiState.postValue(CreateFamilyUiState.Error(result.message))
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateFamilyUiState.Idle
    }
}
