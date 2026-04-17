package com.tubes.nimons360.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tubes.nimons360.data.api.RetrofitClient
import com.tubes.nimons360.data.repository.UserRepository
import com.tubes.nimons360.model.UserDetail
import com.tubes.nimons360.utils.AppResult
import com.tubes.nimons360.utils.NetworkUtils
import com.tubes.nimons360.utils.TokenManager
import kotlinx.coroutines.launch

sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val user: UserDetail) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Loading : UpdateUiState()
    data class Success(val user: UserDetail) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserRepository(RetrofitClient.instance, app)

    private val _userState = MutableLiveData<UserUiState>()
    val userState: LiveData<UserUiState> = _userState

    private val _updateState = MutableLiveData<UpdateUiState>(UpdateUiState.Idle)
    val updateState: LiveData<UpdateUiState> = _updateState

    fun loadUser() {
        if (!NetworkUtils.isConnected(getApplication())) {
            _userState.value = UserUiState.Error("Tidak ada koneksi internet")
            return
        }
        _userState.value = UserUiState.Loading
        viewModelScope.launch {
            when (val result = repo.getMe()) {
                is AppResult.Success -> {
                    _userState.postValue(UserUiState.Success(result.data))
                    TokenManager.saveUserName(getApplication(), result.data.fullName)
                }
                is AppResult.Error -> {
                    _userState.postValue(UserUiState.Error(result.message))
                }
            }
        }
    }

    fun updateName(newName: String) {
        if (!NetworkUtils.isConnected(getApplication())) {
            _updateState.value = UpdateUiState.Error("Tidak ada koneksi internet")
            return
        }
        _updateState.value = UpdateUiState.Loading
        viewModelScope.launch {
            when (val result = repo.updateName(newName)) {
                is AppResult.Success -> {
                    TokenManager.saveUserName(getApplication(), result.data.fullName)
                    _updateState.postValue(UpdateUiState.Success(result.data))
                }
                is AppResult.Error -> {
                    _updateState.postValue(UpdateUiState.Error(result.message))
                }
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateUiState.Idle
    }
}
